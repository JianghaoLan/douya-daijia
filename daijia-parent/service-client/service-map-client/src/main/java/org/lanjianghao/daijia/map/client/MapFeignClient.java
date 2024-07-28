package org.lanjianghao.daijia.map.client;

import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.form.map.CalculateDrivingLineForm;
import org.lanjianghao.daijia.model.form.map.OrderServiceLocationForm;
import org.lanjianghao.daijia.model.form.map.UpdateOrderLocationForm;
import org.lanjianghao.daijia.model.vo.map.DrivingLineVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "service-map")
public interface MapFeignClient {

    @PostMapping("/map/calculateDrivingLine")
    Result<DrivingLineVo> calculateDrivingLine(@RequestBody CalculateDrivingLineForm calculateDrivingLineForm);

}