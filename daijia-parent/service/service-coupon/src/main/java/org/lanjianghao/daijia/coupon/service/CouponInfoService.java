package org.lanjianghao.daijia.coupon.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.lanjianghao.daijia.model.entity.coupon.CouponInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lanjianghao.daijia.model.form.coupon.UseCouponForm;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.lanjianghao.daijia.model.vo.coupon.AvailableCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.NoReceiveCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.NoUseCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.UsedCouponVo;

import java.math.BigDecimal;
import java.util.List;

public interface CouponInfoService extends IService<CouponInfo> {


    PageVo<NoReceiveCouponVo> findNoReceivePage(Page<CouponInfo> pageParam, Long customerId);

    PageVo<NoUseCouponVo> findNoUsePage(Page<CouponInfo> pageParam, Long customerId);

    PageVo<UsedCouponVo> findUsedPage(Page<CouponInfo> pageParam, Long customerId);

    Boolean receive(Long customerId, Long couponId);

    List<AvailableCouponVo> findAvailableCoupon(Long customerId, BigDecimal orderAmount);

    BigDecimal useCoupon(UseCouponForm useCouponForm);
}
