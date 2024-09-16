package org.lanjianghao.daijia.driver.mapper;

import org.apache.ibatis.annotations.Param;
import org.lanjianghao.daijia.model.entity.driver.DriverAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;

@Mapper
public interface DriverAccountMapper extends BaseMapper<DriverAccount> {


    void addAmount(@Param("driverId") Long driverId, @Param("amount") BigDecimal amount);
}
