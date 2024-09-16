package org.lanjianghao.daijia.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.lanjianghao.daijia.model.entity.coupon.CouponInfo;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.lanjianghao.daijia.model.vo.coupon.AvailableCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.NoReceiveCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.NoUseCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.UsedCouponVo;

import java.util.List;

public interface CouponService  {
    PageVo<NoReceiveCouponVo> findNoReceivePage(Long customerId, Long page, Long limit);


    PageVo<NoUseCouponVo> findNoUsePage(Long customerId, Long page, Long limit);

    PageVo<UsedCouponVo> findUsedPage(Long customerId, Long page, Long limit);

    Boolean receive(Long customerId, Long couponId);

    List<AvailableCouponVo> findAvailableCoupon(Long customerId, Long orderId);
}
