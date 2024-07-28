package org.lanjianghao.daijia.driver.service;

import org.lanjianghao.daijia.model.vo.order.TextAuditingVo;

public interface CiService {

    //图片审核
    Boolean imageAuditing(String path);

    TextAuditingVo textAuditing(String content);
}
