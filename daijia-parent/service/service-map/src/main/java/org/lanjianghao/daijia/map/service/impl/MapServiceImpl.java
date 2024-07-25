package org.lanjianghao.daijia.map.service.impl;

import com.alibaba.fastjson2.JSONObject;
import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.map.service.MapService;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.form.map.CalculateDrivingLineForm;
import org.lanjianghao.daijia.model.vo.map.DrivingLineVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class MapServiceImpl implements MapService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tencent.map.key}")
    private String key;

    private static final String DIRECTION_API_URL =
            "https://apis.map.qq.com/ws/direction/v1/driving/?" +
            "from={from}&" +
            "to={to}&" +
            "key={key}";

    @Override
    public DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm) {
        Map<String, String> params = new HashMap<>();
        params.put("from", calculateDrivingLineForm.getStartPointLatitude() + "," +
                calculateDrivingLineForm.getStartPointLongitude());
        params.put("to", calculateDrivingLineForm.getEndPointLatitude() + "," +
                calculateDrivingLineForm.getEndPointLongitude());
        params.put("key", key);

        JSONObject res = restTemplate.getForObject(DIRECTION_API_URL, JSONObject.class, params);
        if (res == null || res.getIntValue("status") != 0) {
            if (res != null) {
                log.error("地图服务调用失败：" + res.getString("message"));
            }
            throw new BusinessException(ResultCodeEnum.MAP_FAIL);
        }

        JSONObject route = res.getJSONObject("result").getJSONArray("routes").getJSONObject(0);

        //创建vo对象
        DrivingLineVo drivingLineVo = new DrivingLineVo();
        drivingLineVo.setDuration(route.getBigDecimal("duration"));
        BigDecimal distanceKm = route.getBigDecimal("distance")
                .divideToIntegralValue(BigDecimal.valueOf(1000))
                .setScale(2, RoundingMode.HALF_UP);
        drivingLineVo.setDistance(distanceKm);
        drivingLineVo.setPolyline(route.getJSONArray("polyline"));
        return drivingLineVo;
    }
}
