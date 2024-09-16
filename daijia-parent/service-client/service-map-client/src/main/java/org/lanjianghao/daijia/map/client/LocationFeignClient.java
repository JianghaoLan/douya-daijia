package org.lanjianghao.daijia.map.client;

import org.lanjianghao.daijia.common.result.Result;
import org.lanjianghao.daijia.model.form.map.OrderServiceLocationForm;
import org.lanjianghao.daijia.model.form.map.SearchNearByDriverForm;
import org.lanjianghao.daijia.model.form.map.UpdateDriverLocationForm;
import org.lanjianghao.daijia.model.form.map.UpdateOrderLocationForm;
import org.lanjianghao.daijia.model.vo.map.NearByDriverVo;
import org.lanjianghao.daijia.model.vo.map.OrderLocationVo;
import org.lanjianghao.daijia.model.vo.map.OrderServiceLastLocationVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(value = "service-map")
public interface LocationFeignClient {
    @PostMapping("/map/location/updateDriverLocation")
    Result<Boolean> updateDriverLocation(@RequestBody UpdateDriverLocationForm updateDriverLocationForm);

    @DeleteMapping("/map/location/removeDriverLocation/{driverId}")
    Result<Boolean> removeDriverLocation(@PathVariable Long driverId);

    @PostMapping("/map/location/searchNearByDriver")
    Result<List<NearByDriverVo>> searchNearByDriver(@RequestBody SearchNearByDriverForm searchNearByDriverForm);

    @PostMapping("/map/location/updateOrderLocationToCache")
    Result<Boolean> updateOrderLocationToCache(@RequestBody UpdateOrderLocationForm updateOrderLocationForm);

    @GetMapping("/map/location/getCacheOrderLocation/{orderId}")
    Result<OrderLocationVo> getCacheOrderLocation(@PathVariable Long orderId);

    @PostMapping("/map/location/saveOrderServiceLocation")
    Result<Boolean> saveOrderServiceLocation(@RequestBody List<OrderServiceLocationForm> orderLocationServiceFormList);

    @GetMapping("/map/location/getOrderServiceLastLocation/{orderId}")
    Result<OrderServiceLastLocationVo> getOrderServiceLastLocation(@PathVariable Long orderId);

    @GetMapping("/map/location/calculateOrderRealDistance/{orderId}")
    Result<BigDecimal> calculateOrderRealDistance(@PathVariable Long orderId);
}