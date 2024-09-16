package org.lanjianghao.daijia.order.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.lanjianghao.daijia.model.vo.order.OrderListVo;
import org.lanjianghao.daijia.model.vo.order.OrderPayVo;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    @Select("SELECT status FROM order_info WHERE is_deleted = 0 AND id = #{id}")
    Integer selectStatusById(@Param("id") Long orderId);

    IPage<OrderListVo> selectCustomerOrderPageByCustomerId(@Param("customerId") Long customerId, Page<OrderListVo> pageParam);

    IPage<OrderListVo> selectDriverOrderPageByDriverId(@Param("driverId") Long driverId, Page<OrderListVo> pageParam);

    OrderPayVo selectOrderPayVoByOrderNoAndCustomerId(@Param("orderNo") String orderNo, @Param("customerId") Long customerId);
}
