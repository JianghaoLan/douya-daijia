package org.lanjianghao.daijia.mgr.service;

import org.lanjianghao.daijia.model.entity.system.SysOperLog;
import org.lanjianghao.daijia.model.query.system.SysOperLogQuery;
import org.lanjianghao.daijia.model.vo.base.PageVo;

public interface SysOperLogService {

    PageVo<SysOperLog> findPage(Long page, Long limit, SysOperLogQuery sysOperLogQuery);

    /**
     * 保存系统日志记录
     */
    void saveSysLog(SysOperLog sysOperLog);

    SysOperLog getById(Long id);
}
