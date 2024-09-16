package org.lanjianghao.daijia.coupon.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.lanjianghao.daijia.model.entity.coupon.CouponInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.lanjianghao.daijia.model.entity.coupon.CustomerCoupon;
import org.lanjianghao.daijia.model.vo.coupon.NoReceiveCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.NoUseCouponVo;
import org.lanjianghao.daijia.model.vo.coupon.UsedCouponVo;

import java.util.List;

@Mapper
public interface CouponInfoMapper extends BaseMapper<CouponInfo> {

    IPage<NoReceiveCouponVo> findNoReceivePage(Page<CouponInfo> pageParam, @Param("customerId") Long customerId);

    IPage<NoUseCouponVo> findNoUsePage(Page<CouponInfo> pageParam, @Param("customerId") Long customerId);

    IPage<UsedCouponVo> findUsedPage(Page<CouponInfo> pageParam, @Param("customerId") Long customerId);

    int takeOne(@Param("couponId") Long couponId);

    List<NoUseCouponVo> selectUnusedCouponsByCustomerId(@Param("customerId") Long customerId);
}
