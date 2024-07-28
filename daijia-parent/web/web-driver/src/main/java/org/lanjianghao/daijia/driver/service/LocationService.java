package org.lanjianghao.daijia.driver.service;

import org.lanjianghao.daijia.model.form.map.OrderServiceLocationForm;
import org.lanjianghao.daijia.model.form.map.UpdateDriverLocationForm;
import org.lanjianghao.daijia.model.form.map.UpdateOrderLocationForm;

import java.util.List;

public interface LocationService {


    Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm);

    Boolean updateOrderLocationToCache(UpdateOrderLocationForm updateOrderLocationForm);

    Boolean saveOrderServiceLocation(List<OrderServiceLocationForm> orderLocationServiceFormList);
}
