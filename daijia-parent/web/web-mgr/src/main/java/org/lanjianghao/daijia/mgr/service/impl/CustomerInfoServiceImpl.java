package org.lanjianghao.daijia.mgr.service.impl;

import org.lanjianghao.daijia.customer.client.CustomerInfoFeignClient;
import org.lanjianghao.daijia.mgr.service.CustomerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerInfoServiceImpl implements CustomerInfoService {

	@Autowired
	private CustomerInfoFeignClient customerInfoFeignClient;



}
