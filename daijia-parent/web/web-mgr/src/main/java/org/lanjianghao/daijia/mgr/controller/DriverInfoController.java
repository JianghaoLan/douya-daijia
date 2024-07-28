package org.lanjianghao.daijia.mgr.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.mgr.service.DriverInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.vo.driver.DriverInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "司机API接口管理")
@RestController
@RequestMapping(value="/driver/info")
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverInfoController {
	
	@Autowired
	private DriverInfoService driverInfoService;


}

