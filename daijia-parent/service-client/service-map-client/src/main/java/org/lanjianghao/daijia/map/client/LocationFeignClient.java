package org.lanjianghao.daijia.map.client;

import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.form.map.SearchNearByDriverForm;
import org.lanjianghao.daijia.model.form.map.UpdateDriverLocationForm;
import org.lanjianghao.daijia.model.vo.map.NearByDriverVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "service-map")
public interface LocationFeignClient {
    @PostMapping("/map/location/updateDriverLocation")
    Result<Boolean> updateDriverLocation(@RequestBody UpdateDriverLocationForm updateDriverLocationForm);

    @DeleteMapping("/map/location/removeDriverLocation/{driverId}")
    Result<Boolean> removeDriverLocation(@PathVariable Long driverId);

    @PostMapping("/map/location/searchNearByDriver")
    Result<List<NearByDriverVo>> searchNearByDriver(@RequestBody SearchNearByDriverForm searchNearByDriverForm);
}