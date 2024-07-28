package org.lanjianghao.daijia.order.service;

import org.lanjianghao.daijia.model.entity.order.OrderMonitor;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lanjianghao.daijia.model.entity.order.OrderMonitorRecord;

public interface OrderMonitorService extends IService<OrderMonitor> {

    Boolean saveOrderMonitorRecord(OrderMonitorRecord orderMonitorRecord);
}
