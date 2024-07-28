package org.lanjianghao.daijia.driver.client;

import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.vo.order.TextAuditingVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-driver")
public interface CiFeignClient {

    @PostMapping("/cos/textAuditing")
    Result<TextAuditingVo> textAuditing(@RequestBody String content);

}