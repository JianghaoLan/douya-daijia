package org.lanjianghao.daijia.mgr.service.impl;

import org.lanjianghao.daijia.mgr.service.SysOperLogService;
import org.lanjianghao.daijia.model.entity.system.SysOperLog;
import org.lanjianghao.daijia.model.query.system.SysOperLogQuery;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.lanjianghao.daijia.system.client.SysOperLogFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class SysOperLogServiceImpl implements SysOperLogService {

	@Autowired
	private SysOperLogFeignClient sysOperLogFeignClient;

	@Override
	public PageVo<SysOperLog> findPage(Long page, Long limit, SysOperLogQuery sysOperLogQuery) {
		return sysOperLogFeignClient.findPage(page, limit, sysOperLogQuery).getData();
	}

	@Override
	public void saveSysLog(SysOperLog sysOperLog) {
		sysOperLogFeignClient.saveSysLog(sysOperLog);
	}

	@Override
	public SysOperLog getById(Long id) {
		return sysOperLogFeignClient.getById(id).getData();
	}
}
