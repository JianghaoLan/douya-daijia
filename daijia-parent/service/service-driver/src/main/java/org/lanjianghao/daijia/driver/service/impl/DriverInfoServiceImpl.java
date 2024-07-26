package org.lanjianghao.daijia.driver.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import me.chanjar.weixin.common.error.WxErrorException;
import org.joda.time.DateTime;
import org.lanjianghao.daijia.common.constant.SystemConstant;
import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.driver.mapper.DriverFaceRecognitionMapper;
import org.lanjianghao.daijia.driver.mapper.DriverInfoMapper;
import org.lanjianghao.daijia.driver.mapper.DriverLoginLogMapper;
import org.lanjianghao.daijia.driver.mapper.DriverSetMapper;
import org.lanjianghao.daijia.driver.service.CosService;
import org.lanjianghao.daijia.driver.service.DriverAccountService;
import org.lanjianghao.daijia.driver.service.DriverInfoService;
import org.lanjianghao.daijia.driver.service.IaiService;
import org.lanjianghao.daijia.model.entity.driver.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.form.driver.DriverFaceModelForm;
import org.lanjianghao.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import org.lanjianghao.daijia.model.vo.driver.DriverAuthInfoVo;
import org.lanjianghao.daijia.model.vo.driver.DriverLoginVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverInfoServiceImpl extends ServiceImpl<DriverInfoMapper, DriverInfo> implements DriverInfoService {

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private DriverSetMapper driverSetMapper;

    @Autowired
    private DriverAccountService driverAccountService;

    @Autowired
    private DriverLoginLogMapper driverLoginLogMapper;

    @Autowired
    private CosService cosService;

    @Autowired
    private IaiService iaiService;

    @Autowired
    private DriverFaceRecognitionMapper driverFaceRecognitionMapper;

    private DriverInfo newDriver(String openId) {
        //初始化司机基本信息
        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setNickname(String.valueOf(System.currentTimeMillis()));
        driverInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        driverInfo.setWxOpenId(openId);
        this.save(driverInfo);

        //初始化默认设置
        DriverSet driverSet = new DriverSet();
        driverSet.setDriverId(driverInfo.getId());
        driverSet.setOrderDistance(new BigDecimal(0));//0：无限制
        driverSet.setAcceptDistance(new BigDecimal(SystemConstant.ACCEPT_DISTANCE));//默认接单范围：5公里
        driverSet.setIsAutoAccept(0);//0：否 1：是
        driverSetMapper.insert(driverSet);

        //初始化司机账户
        DriverAccount driverAccount = new DriverAccount();
        driverAccount.setDriverId(driverInfo.getId());
        driverAccountService.save(driverAccount);

        return driverInfo;
    }

    @Override
    public Long login(String code) {
        String openId;
        try {
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            openId = sessionInfo.getOpenid();
        } catch (WxErrorException e) {
            throw new BusinessException(ResultCodeEnum.DATA_ERROR);
        }

        DriverInfo driverInfo = this.getOne(new LambdaQueryWrapper<DriverInfo>().eq(DriverInfo::getWxOpenId, openId));
        if (driverInfo == null) {
            driverInfo = newDriver(openId);
        }

        //登录日志
        DriverLoginLog driverLoginLog = new DriverLoginLog();
        driverLoginLog.setDriverId(driverInfo.getId());
        driverLoginLog.setMsg("小程序登录");
        driverLoginLogMapper.insert(driverLoginLog);

        return driverInfo.getId();
    }

    @Override
    public DriverLoginVo getDriverLoginInfo(Long driverId) {
        DriverInfo driverInfo = this.getById(driverId);

        DriverLoginVo loginInfo = new DriverLoginVo();
        BeanUtils.copyProperties(driverInfo, loginInfo);
        loginInfo.setIsArchiveFace(StringUtils.hasText(driverInfo.getFaceModelId()));

        return loginInfo;
    }

    @Override
    public DriverAuthInfoVo getDriverAuthInfo(Long driverId) {
        DriverInfo info = this.getById(driverId);
        DriverAuthInfoVo authInfo = new DriverAuthInfoVo();
        BeanUtils.copyProperties(info, authInfo);
        authInfo.setDriverId(info.getId());

        authInfo.setIdcardBackShowUrl(cosService.getImageUrl(info.getIdcardBackUrl()));
        authInfo.setIdcardFrontShowUrl(cosService.getImageUrl(info.getIdcardFrontUrl()));
        authInfo.setIdcardHandShowUrl(cosService.getImageUrl(info.getIdcardHandUrl()));
        authInfo.setDriverLicenseFrontShowUrl(cosService.getImageUrl(info.getDriverLicenseFrontUrl()));
        authInfo.setDriverLicenseBackShowUrl(cosService.getImageUrl(info.getDriverLicenseBackUrl()));
        authInfo.setDriverLicenseHandShowUrl(cosService.getImageUrl(info.getDriverLicenseHandUrl()));

        return authInfo;
    }

    @Override
    public boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        DriverInfo driverInfo = new DriverInfo();
        BeanUtils.copyProperties(updateDriverAuthInfoForm, driverInfo);
        driverInfo.setId(updateDriverAuthInfoForm.getDriverId());
        return this.updateById(driverInfo);
    }

    @Override
    public boolean createDriverFaceModel(DriverFaceModelForm faceModelForm) {
        DriverInfo driverInfo = this.getById(faceModelForm.getDriverId());
        String modelId = iaiService.createDriverFaceModel(driverInfo, faceModelForm.getImageBase64());
        if (!StringUtils.hasText(modelId)) {
            return false;
        }

        DriverInfo forUpdate = new DriverInfo();
        forUpdate.setId(driverInfo.getId());
        forUpdate.setFaceModelId(modelId);
        forUpdate.setAuthStatus(1);
        return this.updateById(forUpdate);
    }

    @Override
    public DriverSet getDriverSet(Long driverId) {
        return driverSetMapper.selectOne(new LambdaQueryWrapper<DriverSet>().eq(DriverSet::getDriverId, driverId));
    }

    @Override
    public List<DriverSet> getBatchDriverSet(List<Long> driverIds) {
        return driverSetMapper.selectList(new LambdaQueryWrapper<DriverSet>().in(DriverSet::getDriverId, driverIds));
    }

    @Override
    public boolean isFaceRecognition(Long driverId) {
        LambdaQueryWrapper<DriverFaceRecognition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DriverFaceRecognition::getDriverId, driverId);
        wrapper.eq(DriverFaceRecognition::getFaceDate, new DateTime().toString("yyyy-MM-dd"));
        Long count = driverFaceRecognitionMapper.selectCount(wrapper);
        return count > 0;
    }

    @Override
    public boolean verifyDriverFace(DriverFaceModelForm driverFaceModelForm) {
        Long driverId = driverFaceModelForm.getDriverId();
        //人脸比对和活体检测
        boolean res = iaiService.verifyFace(driverFaceModelForm.getImageBase64(), driverId)
                && iaiService.detectLiveFace(driverFaceModelForm.getImageBase64(), driverId);
        if (!res) {
            return false;
        }

        DriverFaceRecognition toSave = new DriverFaceRecognition();
        toSave.setFaceDate(new Date());
        toSave.setDriverId(driverId);
        driverFaceRecognitionMapper.insert(toSave);
        return true;
    }

    @Override
    public Boolean updateServiceStatus(Long driverId, Integer status) {
        DriverSet forUpdate = new DriverSet();
        forUpdate.setDriverId(driverId);
        forUpdate.setServiceStatus(status);
        LambdaQueryWrapper<DriverSet> query = new LambdaQueryWrapper<>();
        query.eq(DriverSet::getDriverId, driverId);
        int count = driverSetMapper.update(forUpdate, query);
        return count > 0;
    }
}