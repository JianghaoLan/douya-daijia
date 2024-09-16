package org.lanjianghao.daijia.model.vo.map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AvailableOrderVo {

    @Schema(description = "订单id")
    private Long orderId;

    @Schema(description = "距离")
    private BigDecimal distance;
}
