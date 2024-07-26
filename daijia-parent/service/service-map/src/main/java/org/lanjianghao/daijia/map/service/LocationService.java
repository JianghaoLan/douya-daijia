package org.lanjianghao.daijia.map.service;

import org.lanjianghao.daijia.model.form.map.SearchNearByDriverForm;
import org.lanjianghao.daijia.model.form.map.UpdateDriverLocationForm;
import org.lanjianghao.daijia.model.vo.map.NearByDriverVo;

import java.util.List;

public interface LocationService {

    boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm);

    boolean removeDriverLocation(Long driverId);

    List<NearByDriverVo> searchNearByDriver(SearchNearByDriverForm searchNearByDriverForm);
}
