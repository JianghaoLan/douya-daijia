package org.lanjianghao.daijia.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.coupon.mapper.CouponInfoMapper;
import org.lanjianghao.daijia.coupon.mapper.CustomerCouponMapper;
import org.lanjianghao.daijia.coupon.service.CouponInfoService;
import org.lanjianghao.daijia.coupon.utils.CouponHelper;
import org.lanjianghao.daijia.model.entity.coupon.CouponInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lanjianghao.daijia.model.entity.coupon.CustomerCoupon;
import org.lanjianghao.daijia.model.form.coupon.UseCouponForm;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.lanjianghao.daijia.model.vo.coupon.AvailableCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.NoReceiveCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.NoUseCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.UsedCouponVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Autowired
    private CouponInfoMapper couponInfoMapper;

    @Autowired
    private CustomerCouponMapper customerCouponMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageVo<NoReceiveCouponVo> findNoReceivePage(Page<CouponInfo> pageParam, Long customerId) {
        IPage<NoReceiveCouponVo> pageInfo = couponInfoMapper.findNoReceivePage(pageParam, customerId);
        return new PageVo(pageInfo.getRecords(), pageInfo.getPages(), pageInfo.getTotal());
    }

    @Override
    public PageVo<NoUseCouponVo> findNoUsePage(Page<CouponInfo> pageParam, Long customerId) {
        IPage<NoUseCouponVo> pageInfo = couponInfoMapper.findNoUsePage(pageParam, customerId);
        return new PageVo(pageInfo.getRecords(), pageInfo.getPages(), pageInfo.getTotal());
    }

    @Override
    public PageVo<UsedCouponVo> findUsedPage(Page<CouponInfo> pageParam, Long customerId) {
        IPage<UsedCouponVo> pageInfo = couponInfoMapper.findUsedPage(pageParam, customerId);
        return new PageVo(pageInfo.getRecords(), pageInfo.getPages(), pageInfo.getTotal());
    }

    @Override
    public Boolean receive(Long customerId, Long couponId) {
        CouponInfo couponInfo = couponInfoMapper.selectById(couponId);
        //判断优惠券是否存在
        if (couponInfo == null) {
            throw new BusinessException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        //判断优惠券是否过期
        if (couponInfo.getExpireTime().before(new Date())) {
            throw new BusinessException(ResultCodeEnum.COUPON_EXPIRE);
        }
        //检查发行数量和领取数量是否合法
        if (couponInfo.getPublishCount() != 0 && couponInfo.getReceiveCount() >= couponInfo.getPublishCount()) {
            throw new BusinessException(ResultCodeEnum.COUPON_LESS);
        }

        RLock lock = redissonClient.getLock(RedisConstant.COUPON_LOCK + customerId);
        boolean lockFlag = false;
        try {
            lockFlag = lock.tryLock(RedisConstant.COUPON_LOCK_WAIT_TIME,
                    RedisConstant.COUPON_LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (lockFlag) {

                //判断客户领取的数量是否超过上限
                if (couponInfo.getPerLimit() > 0) {
                    Long count = customerCouponMapper.selectCount(
                            new LambdaQueryWrapper<CustomerCoupon>()
                                    .eq(CustomerCoupon::getCustomerId, customerId)
                                    .eq(CustomerCoupon::getCouponId, couponInfo.getId()));
                    if (count >= couponInfo.getPerLimit()) {
                        throw new BusinessException(ResultCodeEnum.COUPON_USER_LIMIT);
                    }
                }

                //扣除数量
                int row = couponInfoMapper.takeOne(couponId);
                if (row == 0) {
                    throw new BusinessException(ResultCodeEnum.ILLEGAL_REQUEST);
                }

                //插入用户优惠券
                giveCoupon(customerId, couponId, couponInfo.getExpireTime());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lockFlag) {
                lock.unlock();
            }
        }

        return true;
    }

    @Override
    public List<AvailableCouponVo> findAvailableCoupon(Long customerId, BigDecimal orderAmount) {
        List<NoUseCouponVo> coupons = selectUnusedCouponsByCustomerId(customerId);
        if (CollectionUtils.isEmpty(coupons)) {
            return Collections.emptyList();
        }

        //过滤无法使用的优惠券
        List<AvailableCouponVo> vos = new ArrayList<>();
        coupons.forEach(coupon -> {
            CouponHelper couponHelper;
            try {
                couponHelper = CouponHelper.getInstance(coupon.getCouponType());
            } catch (IllegalArgumentException e) {
                return;
            }

            CouponHelper.CouponVo couponVo = CouponHelper.CouponVo.fromNoUseCouponVo(coupon);
            if (couponHelper.isAvailable(couponVo, orderAmount)) {
                vos.add(buildAvailableCouponVo(coupon, couponHelper.getReduceAmount(couponVo, orderAmount)));
            }
        });

        //按减免金额排序
        vos.sort(Comparator.comparing(AvailableCouponVo::getReduceAmount));

        return vos;
    }

    @Override
    public BigDecimal useCoupon(UseCouponForm useCouponForm) {
        CustomerCoupon customerCoupon = customerCouponMapper.selectById(useCouponForm.getCustomerCouponId());
        if (customerCoupon == null || customerCoupon.getCustomerId().intValue() != useCouponForm.getCustomerId()) {
            throw new BusinessException(ResultCodeEnum.ILLEGAL_REQUEST);
        }

        CouponInfo couponInfo = couponInfoMapper.selectById(customerCoupon.getCouponId());
        if (couponInfo == null) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }

        CouponHelper couponHelper = CouponHelper.getInstance(couponInfo.getCouponType());
        CouponHelper.CouponVo couponVo = CouponHelper.CouponVo.fromCouponInfo(couponInfo);
        if (!couponHelper.isAvailable(couponVo, useCouponForm.getOrderAmount())) {
            throw new BusinessException(ResultCodeEnum.ILLEGAL_REQUEST);
        }

        //满足优惠条件，更新数据库表
        //更新优惠券表
        CouponInfo updateCouponInfo = new CouponInfo();
        updateCouponInfo.setId(couponInfo.getId());
        updateCouponInfo.setUseCount(couponInfo.getUseCount() + 1);
        couponInfoMapper.updateById(updateCouponInfo);
        //更新用户优惠券表
        CustomerCoupon updateCustomerCoupon = new CustomerCoupon();
        updateCustomerCoupon.setId(customerCoupon.getId());
        updateCustomerCoupon.setStatus(2);
        updateCustomerCoupon.setUsedTime(new Date());
        updateCustomerCoupon.setOrderId(useCouponForm.getOrderId());
        customerCouponMapper.updateById(updateCustomerCoupon);

        return couponHelper.getReduceAmount(couponVo, useCouponForm.getOrderAmount());
    }

    private AvailableCouponVo buildAvailableCouponVo(NoUseCouponVo noUseCouponVo, BigDecimal reduceAmount) {
        AvailableCouponVo vo = new AvailableCouponVo();
        BeanUtils.copyProperties(noUseCouponVo, vo);
        vo.setCouponId(noUseCouponVo.getId());
        vo.setReduceAmount(reduceAmount);
        return vo;
    }

    private List<NoUseCouponVo> selectUnusedCouponsByCustomerId(Long customerId) {
        return couponInfoMapper.selectUnusedCouponsByCustomerId(customerId);
    }

    private void giveCoupon(Long customerId, Long couponId, Date expireTime) {
        CustomerCoupon coupon = new CustomerCoupon();
        coupon.setCouponId(couponId);
        coupon.setCustomerId(customerId);
        coupon.setStatus(1);
        coupon.setReceiveTime(new Date());
        coupon.setExpireTime(expireTime);
        int ret = customerCouponMapper.insert(coupon);
        if (ret == 0) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }
    }
}
