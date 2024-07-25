package org.lanjianghao.daijia.driver.service;

import org.lanjianghao.daijia.model.entity.driver.DriverInfo;

public interface IaiService {

    String createDriverFaceModel(DriverInfo driverId, String imageBase64);
}
