package org.lanjianghao.daijia.driver.service.impl;

import io.swagger.v3.oas.annotations.Operation;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.driver.client.CiFeignClient;
import org.lanjianghao.daijia.driver.service.FileService;
import org.lanjianghao.daijia.driver.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.entity.order.OrderMonitorRecord;
import org.lanjianghao.daijia.model.form.order.OrderMonitorForm;
import org.lanjianghao.daijia.model.vo.order.TextAuditingVo;
import org.lanjianghao.daijia.order.client.OrderMonitorFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class MonitorServiceImpl implements MonitorService {

    @Autowired
    private FileService fileService;

    @Autowired
    private OrderMonitorFeignClient orderMonitorFeignClient;

    @Autowired
    private CiFeignClient ciFeignClient;

    @Override
    public Boolean upload(MultipartFile file, OrderMonitorForm orderMonitorForm) {
        String url = fileService.upload(file);

        String content = orderMonitorForm.getContent();
        TextAuditingVo auditingRes = ciFeignClient.textAuditing(content).getData();

        OrderMonitorRecord orderMonitorRecord = new OrderMonitorRecord();
        orderMonitorRecord.setOrderId(orderMonitorForm.getOrderId());
        orderMonitorRecord.setContent(orderMonitorForm.getContent());
        orderMonitorRecord.setKeywords(auditingRes.getKeywords());
        orderMonitorRecord.setResult(auditingRes.getResult());
        orderMonitorRecord.setFileUrl(url);
        orderMonitorFeignClient.saveMonitorRecord(orderMonitorRecord);

        return true;
    }
}
