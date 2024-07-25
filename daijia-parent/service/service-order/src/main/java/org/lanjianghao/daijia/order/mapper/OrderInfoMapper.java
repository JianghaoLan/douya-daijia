package org.lanjianghao.daijia.order.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.lanjianghao.daijia.model.entity.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    @Select("SELECT status FROM order_info WHERE is_deleted = 0 AND id = #{id}")
    Integer selectStatusById(@Param("id") Long orderId);
}
