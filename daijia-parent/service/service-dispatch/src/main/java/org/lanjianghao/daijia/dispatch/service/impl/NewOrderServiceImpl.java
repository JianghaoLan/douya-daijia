package org.lanjianghao.daijia.dispatch.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.dispatch.mapper.OrderJobMapper;
import org.lanjianghao.daijia.dispatch.service.NewOrderService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.dispatch.xxl.client.XxlJobClient;
import org.lanjianghao.daijia.map.client.LocationFeignClient;
import org.lanjianghao.daijia.map.client.MapFeignClient;
import org.lanjianghao.daijia.model.entity.dispatch.OrderJob;
import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import org.lanjianghao.daijia.model.enums.OrderStatus;
import org.lanjianghao.daijia.model.form.map.SearchNearByDriverForm;
import org.lanjianghao.daijia.model.vo.dispatch.NewOrderTaskVo;
import org.lanjianghao.daijia.model.vo.map.NearByDriverVo;
import org.lanjianghao.daijia.model.vo.order.NewOrderDataVo;
import org.lanjianghao.daijia.order.client.OrderInfoFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class NewOrderServiceImpl extends ServiceImpl<OrderJobMapper, OrderJob> implements NewOrderService {

    @Autowired
    private XxlJobClient xxlJobClient;

    @Autowired
    private LocationFeignClient locationFeignClient;

    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private OrderJob getByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderJob::getOrderId, orderId);
        return this.getOne(wrapper);
    }

    private OrderJob getByJobId(Long jobId) {
        LambdaQueryWrapper<OrderJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderJob::getJobId, jobId);
        return this.getOne(wrapper);
    }

    @Override
    public Long addAndStartTask(NewOrderTaskVo newOrderTaskVo) {
        Long orderId = newOrderTaskVo.getOrderId();
        OrderJob orderJob = this.getByOrderId(newOrderTaskVo.getOrderId());

        //没有启动，进行操作
        if (orderJob == null) {
            int curSec = Calendar.getInstance().get(Calendar.SECOND);
            String corn = String.format("%d/30 * * * * ?", curSec);
            Long jobId = xxlJobClient.addAndStart("newOrderTaskHandler",
                    "", corn, "订单" + orderId + "任务调度");

            //添加数据库任务记录
            orderJob = new OrderJob();
            orderJob.setOrderId(orderId);
            orderJob.setJobId(jobId);
            orderJob.setParameter(JSON.toJSONString(newOrderTaskVo));
            this.save(orderJob);
        }

        return orderJob.getJobId();
    }

    @Override
    public void executeTask(long jobId) {
        //判断当前任务是否已经创建
        OrderJob orderJob = this.getByJobId(jobId);
        if (orderJob == null) {
            return;
        }

        //判断订单是否是等待接单状态
        Integer orderStatus = orderInfoFeignClient.getOrderStatus(orderJob.getOrderId()).getData();
        if (orderStatus != OrderStatus.WAITING_ACCEPT.getStatus().intValue()) {
            //停止任务调度
            xxlJobClient.stopJob(jobId);
            return;
        }

        //搜索附近满足条件的司机
        NewOrderTaskVo newOrderTaskVo = JSON.parseObject(orderJob.getParameter(), NewOrderTaskVo.class);
        SearchNearByDriverForm form = new SearchNearByDriverForm();
        form.setLatitude(newOrderTaskVo.getStartPointLatitude());
        form.setLongitude(newOrderTaskVo.getStartPointLongitude());
        form.setMileageDistance(newOrderTaskVo.getExpectDistance());
        List<NearByDriverVo> nearByDrivers = locationFeignClient.searchNearByDriver(form).getData();

        //保存订单附近司机的集合
        String repeatKey = RedisConstant.DRIVER_ORDER_REPEAT_LIST + newOrderTaskVo.getOrderId();
        List<String> driverIds = nearByDrivers.stream().map(d -> d.getDriverId().toString()).toList();
        redisTemplate.opsForSet().add(repeatKey, driverIds.toArray(new String[0]));
        //TODO 修改过期逻辑
        redisTemplate.expire(repeatKey, RedisConstant.DRIVER_ORDER_REPEAT_LIST_EXPIRES_TIME, TimeUnit.MINUTES);
//        nearByDrivers.forEach(nearByDriver -> {
//            //记录司机id，防止重复推送订单信息
//            String driverIdStr = nearByDriver.getDriverId().toString();
//            Boolean isMember = redisTemplate.opsForSet().isMember(repeatKey, driverIdStr);
//            if (Boolean.FALSE.equals(isMember)) {
//                redisTemplate.opsForSet().add(repeatKey, driverIdStr);
//                //设置过期时间
//                redisTemplate.expire(repeatKey, RedisConstant.DRIVER_ORDER_REPEAT_LIST_EXPIRES_TIME, TimeUnit.MINUTES);
//            }
//        });

        //为每个司机创建临时队列，保存新订单信息
        nearByDrivers.forEach(driver -> {
            NewOrderDataVo newOrderDataVo = new NewOrderDataVo();
            BeanUtils.copyProperties(newOrderTaskVo, newOrderDataVo);
            newOrderDataVo.setDistance(driver.getDistance());
            String key = RedisConstant.DRIVER_ORDER_TEMP_LIST + driver.getDriverId();
            String value = JSON.toJSONString(newOrderDataVo);
            redisTemplate.opsForList().leftPush(key, value);

            //TODO 修改过期逻辑
            //队列1分钟没消费自动过期
            redisTemplate.expire(key, RedisConstant.DRIVER_ORDER_TEMP_LIST_EXPIRES_TIME, TimeUnit.MINUTES);
        });
    }

    @Override
    public List<NewOrderDataVo> findNewOrderQueueData(Long driverId) {
        String key = RedisConstant.DRIVER_ORDER_TEMP_LIST + driverId;
        Long size = redisTemplate.opsForList().size(key);
        assert size != null;
        if (size > 0) {
            List<String> values = redisTemplate.opsForList().leftPop(key, size);
            assert values != null;
            return values.stream().map(
                    value -> JSON.parseObject(value, NewOrderDataVo.class)).toList();
        }
        return Collections.emptyList();
    }

    @Override
    public Boolean clearNewOrderQueueData(Long driverId) {
        String key = RedisConstant.DRIVER_ORDER_TEMP_LIST + driverId;
        redisTemplate.delete(key);
        return true;
    }
}
