package org.lanjianghao.daijia.dispatch.xxl.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class DispatchJobHandler {

    @XxlJob("testJobHandler")
    public void testJobHandler() {
        System.out.println("测试Xxl job成功");
    }
}
