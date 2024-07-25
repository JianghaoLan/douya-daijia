package org.lanjianghao.daijia.mgr.service.impl;

import org.lanjianghao.daijia.mgr.service.OrderInfoService;
import org.lanjianghao.daijia.order.client.OrderInfoFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderInfoServiceImpl implements OrderInfoService {

	@Autowired
	private OrderInfoFeignClient orderInfoFeignClient;



}
