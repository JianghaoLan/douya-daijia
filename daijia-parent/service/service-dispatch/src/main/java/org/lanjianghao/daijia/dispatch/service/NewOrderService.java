package org.lanjianghao.daijia.dispatch.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lanjianghao.daijia.model.entity.dispatch.OrderJob;
import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import org.lanjianghao.daijia.model.vo.dispatch.NewOrderTaskVo;
import org.lanjianghao.daijia.model.vo.order.NewOrderDataVo;

import java.util.List;

public interface NewOrderService extends IService<OrderJob> {

//    Long addAndStartTask(NewOrderTaskVo newOrderTaskVo);
//
//    void executeTask(long jobId);
//
//    List<NewOrderDataVo> findNewOrderQueueData(Long driverId);
//
//    Boolean clearNewOrderQueueData(Long driverId);
}
