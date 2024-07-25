package org.lanjianghao.daijia.mgr.service;

import org.lanjianghao.daijia.model.entity.system.SysLoginLog;
import org.lanjianghao.daijia.model.query.system.SysLoginLogQuery;
import org.lanjianghao.daijia.model.vo.base.PageVo;

public interface SysLoginLogService {

    PageVo<SysLoginLog> findPage(Long page, Long limit, SysLoginLogQuery sysLoginLogQuery);

    /**
     * 记录登录信息
     *
     * @param sysLoginLog
     * @return
     */
    void recordLoginLog(SysLoginLog sysLoginLog);

    SysLoginLog getById(Long id);
}
