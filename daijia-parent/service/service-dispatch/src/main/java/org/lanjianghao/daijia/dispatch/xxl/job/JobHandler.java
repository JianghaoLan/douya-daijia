package org.lanjianghao.daijia.dispatch.xxl.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.dispatch.mapper.XxlJobLogMapper;
import org.lanjianghao.daijia.dispatch.service.NewOrderService;
import org.lanjianghao.daijia.model.entity.dispatch.XxlJobLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobHandler {

    @Autowired
    private XxlJobLogMapper xxlJobLogMapper;

    @Autowired
    private NewOrderService newOrderService;

    @XxlJob("newOrderTaskHandler")
    public void newOrderTaskHandler() {
        XxlJobLog xxlJobLog = new XxlJobLog();
        xxlJobLog.setJobId(XxlJobHelper.getJobId());
        log.info("执行任务newOrderTaskHandler，" + XxlJobHelper.getJobId());

        long startTime = System.currentTimeMillis();
        try {
            newOrderService.executeTask(XxlJobHelper.getJobId());
        } catch (Exception e) {
            xxlJobLog.setError(e.getMessage());
            xxlJobLog.setStatus(0);
            e.printStackTrace();
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            //改成Long类型
            xxlJobLog.setTimes((int)elapsed);
            xxlJobLogMapper.insert(xxlJobLog);
        }
    }
}
