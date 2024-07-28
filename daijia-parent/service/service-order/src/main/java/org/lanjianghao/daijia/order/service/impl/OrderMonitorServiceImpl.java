package org.lanjianghao.daijia.order.service.impl;

import org.lanjianghao.daijia.model.entity.order.OrderMonitor;
import org.lanjianghao.daijia.model.entity.order.OrderMonitorRecord;
import org.lanjianghao.daijia.order.mapper.OrderMonitorMapper;
import org.lanjianghao.daijia.order.repository.OrderMonitorRecordRepository;
import org.lanjianghao.daijia.order.service.OrderMonitorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderMonitorServiceImpl extends ServiceImpl<OrderMonitorMapper, OrderMonitor> implements OrderMonitorService {

    @Autowired
    private OrderMonitorRecordRepository orderMonitorRecordRepository;

    @Override
    public Boolean saveOrderMonitorRecord(OrderMonitorRecord orderMonitorRecord) {
        orderMonitorRecordRepository.save(orderMonitorRecord);
        return true;
    }
}
