package org.lanjianghao.daijia.map.service;

import org.lanjianghao.daijia.model.form.map.CalculateDrivingLineForm;
import org.lanjianghao.daijia.model.vo.map.DrivingLineVo;

public interface MapService {

    DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm);
}
