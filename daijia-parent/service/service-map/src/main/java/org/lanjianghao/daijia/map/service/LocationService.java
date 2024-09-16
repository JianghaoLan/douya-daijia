package org.lanjianghao.daijia.map.service;

import org.lanjianghao.daijia.model.form.map.OrderServiceLocationForm;
import org.lanjianghao.daijia.model.form.map.SearchNearByDriverForm;
import org.lanjianghao.daijia.model.form.map.UpdateDriverLocationForm;
import org.lanjianghao.daijia.model.form.map.UpdateOrderLocationForm;
import org.lanjianghao.daijia.model.vo.map.*;

import java.math.BigDecimal;
import java.util.List;

public interface LocationService {

    boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm);

    boolean removeDriverLocation(Long driverId);

//    List<NearByDriverVo> searchNearByDriver(SearchNearByDriverForm searchNearByDriverForm);

    Boolean updateOrderLocationToCache(UpdateOrderLocationForm updateOrderLocationForm);

    OrderLocationVo getCacheOrderLocation(Long orderId);

    Boolean saveOrderServiceLocation(List<OrderServiceLocationForm> orderLocationServiceFormList);

    OrderServiceLastLocationVo getOrderServiceLastLocation(Long orderId);

    BigDecimal calculateOrderRealDistance(Long orderId);

//    Boolean addOrderStartLocation(OrderStartLocationVo orderStartLocationVo);
//
//    Boolean removeOrderStartLocation(Long orderId);

    List<AvailableOrderVo> searchNewAvailableOrder(Long driverId);

    Boolean setOrderLocationInfo(OrderLocationInfoVo locationInfo);

    Boolean removeOrderRelatedInfo(Long orderId);
}
