package org.lanjianghao.daijia.coupon.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.lanjianghao.daijia.model.entity.coupon.CouponInfo;
import org.lanjianghao.daijia.model.vo.coupon.NoUseCouponVo;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface CouponHelper {
    boolean isAvailable(CouponVo coupon, BigDecimal orderAmount);

    BigDecimal getReduceAmount(CouponVo coupon, BigDecimal orderAmount);

    @Data
    @Schema(description = "CouponVo")
    class CouponVo {

        @Schema(description = "优惠卷类型 1 现金券 2 折扣")
        private Integer couponType;

        @Schema(description = "金额")
        private BigDecimal amount;

        @Schema(description = "折扣：取值[1 到 10]")
        private BigDecimal discount;

        @Schema(description = "使用门槛 0->没门槛")
        private BigDecimal conditionAmount;

        public static CouponVo fromNoUseCouponVo(NoUseCouponVo noUseCouponVo) {
            CouponVo vo = new CouponVo();
            BeanUtils.copyProperties(noUseCouponVo, vo);
            return vo;
        }

        public static CouponVo fromCouponInfo(CouponInfo couponInfo) {
            CouponVo vo = new CouponVo();
            BeanUtils.copyProperties(couponInfo, vo);
            return vo;
        }
    }

    static CouponHelper getInstance(int couponType) {
        if (couponType == 1) {
            return new CashCouponHelper();
        } else if (couponType == 2) {
            return new DiscountCouponHelper();
        }
        throw new IllegalArgumentException("Invalid coupon type: " + couponType);
    }
}

class CashCouponHelper implements CouponHelper {

    @Override
    public boolean isAvailable(CouponVo coupon, BigDecimal orderAmount) {
        return (coupon.getConditionAmount().equals(BigDecimal.ZERO) && coupon.getAmount().compareTo(orderAmount) <= 0)
                || coupon.getConditionAmount().compareTo(orderAmount) <= 0;
    }

    @Override
    public BigDecimal getReduceAmount(CouponVo coupon, BigDecimal orderAmount) {
        return coupon.getAmount();
    }
}

class DiscountCouponHelper implements CouponHelper {

    @Override
    public boolean isAvailable(CouponVo coupon, BigDecimal orderAmount) {
        return coupon.getConditionAmount().equals(BigDecimal.ZERO)
                || coupon.getConditionAmount().compareTo(orderAmount) <= 0;
    }

    @Override
    public BigDecimal getReduceAmount(CouponVo coupon, BigDecimal orderAmount) {
        BigDecimal discounted = orderAmount.multiply(coupon.getDiscount()).setScale(2, RoundingMode.HALF_UP);
        return orderAmount.subtract(discounted);
    }
}