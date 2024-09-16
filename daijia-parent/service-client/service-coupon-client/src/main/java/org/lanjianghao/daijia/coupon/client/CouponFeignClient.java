package org.lanjianghao.daijia.coupon.client;

import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.form.coupon.UseCouponForm;
import org.lanjianghao.daijia.model.vo.base.PageVo;
import org.lanjianghao.daijia.model.vo.coupon.AvailableCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.NoReceiveCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.NoUseCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.UsedCouponVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;


@FeignClient(value = "service-coupon")
public interface CouponFeignClient {

    @GetMapping("/coupon/info/findNoReceivePage/{customerId}/{page}/{limit}")
    Result<PageVo<NoReceiveCouponVo>> findNoReceivePage(@PathVariable Long customerId,
                                                        @PathVariable Long page,
                                                        @PathVariable Long limit);

    @GetMapping("/coupon/info/findNoUsePage/{customerId}/{page}/{limit}")
    Result<PageVo<NoUseCouponVo>> findNoUsePage(@PathVariable Long customerId,
                                                @PathVariable Long page,
                                                @PathVariable Long limit);

    @GetMapping("/coupon/info/findUsedPage/{customerId}/{page}/{limit}")
    Result<PageVo<UsedCouponVo>> findUsedPage(@PathVariable Long customerId,
                                              @PathVariable Long page,
                                              @PathVariable Long limit);

    @GetMapping("/coupon/info/receive/{customerId}/{couponId}")
    Result<Boolean> receive(@PathVariable Long customerId, @PathVariable Long couponId);

    @GetMapping("/coupon/info/findAvailableCoupon/{customerId}/{orderAmount}")
    Result<List<AvailableCouponVo>> findAvailableCoupon(@PathVariable Long customerId, @PathVariable BigDecimal orderAmount);

    @PostMapping("/coupon/info/useCoupon")
    Result<BigDecimal> useCoupon(@RequestBody UseCouponForm useCouponForm);
}