package org.lanjianghao.daijia.customer.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.lanjianghao.daijia.coupon.client.CouponFeignClient;
import org.lanjianghao.daijia.customer.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.entity.coupon.CouponInfo;
import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.lanjianghao.daijia.model.vo.coupon.AvailableCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.NoReceiveCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.NoUseCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.UsedCouponVo;
import org.lanjianghao.daijia.model.vo.order.OrderBillVo;
import org.lanjianghao.daijia.order.client.OrderInfoFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponFeignClient couponFeignClient;

    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    @Override
    public PageVo<NoReceiveCouponVo> findNoReceivePage(Long customerId, Long page, Long limit) {
        return couponFeignClient.findNoReceivePage(customerId, page, limit).getData();
    }

    @Override
    public PageVo<NoUseCouponVo> findNoUsePage(Long customerId, Long page, Long limit) {
        return couponFeignClient.findNoUsePage(customerId, page, limit).getData();
    }

    @Override
    public PageVo<UsedCouponVo> findUsedPage(Long customerId, Long page, Long limit) {
        return couponFeignClient.findUsedPage(customerId, page, limit).getData();
    }

    @Override
    public Boolean receive(Long customerId, Long couponId) {
        return couponFeignClient.receive(customerId, couponId).getData();
    }

    @Override
    public List<AvailableCouponVo> findAvailableCoupon(Long customerId, Long orderId) {
        OrderBillVo billVo = orderInfoFeignClient.getOrderBillInfo(orderId).getData();
        BigDecimal payAmount = billVo.getPayAmount();
        return couponFeignClient.findAvailableCoupon(customerId, payAmount).getData();
    }

}
