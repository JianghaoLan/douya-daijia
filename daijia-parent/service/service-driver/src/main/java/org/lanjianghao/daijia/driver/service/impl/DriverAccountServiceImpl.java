package org.lanjianghao.daijia.driver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.lanjianghao.daijia.driver.mapper.DriverAccountDetailMapper;
import org.lanjianghao.daijia.driver.mapper.DriverAccountMapper;
import org.lanjianghao.daijia.driver.service.DriverAccountService;
import org.lanjianghao.daijia.model.entity.driver.DriverAccount;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.lanjianghao.daijia.model.entity.driver.DriverAccountDetail;
import org.lanjianghao.daijia.model.form.driver.TransferForm;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverAccountServiceImpl extends ServiceImpl<DriverAccountMapper, DriverAccount> implements DriverAccountService {

    @Autowired
    private DriverAccountMapper driverAccountMapper;

    @Autowired
    private DriverAccountDetailMapper driverAccountDetailMapper;

    private boolean getIsDriverAccountDetailExists(String tradeNo) {
        LambdaQueryWrapper<DriverAccountDetail> query = new LambdaQueryWrapper<>();
        query.eq(DriverAccountDetail::getTradeNo, tradeNo);
        return driverAccountDetailMapper.selectCount(query) > 0;
    }

    @Override
    public Boolean transfer(TransferForm transferForm) {

        //判断此订单的奖励是否已添加过
        boolean exists = getIsDriverAccountDetailExists(transferForm.getTradeNo());
        if (exists) {
            return true;
        }

        //添加奖励到司机账户
        driverAccountMapper.addAmount(transferForm.getDriverId(), transferForm.getAmount());

        DriverAccountDetail driverAccountDetail = new DriverAccountDetail();
        BeanUtils.copyProperties(transferForm, driverAccountDetail);
        driverAccountDetailMapper.insert(driverAccountDetail);

        return true;
    }
}
