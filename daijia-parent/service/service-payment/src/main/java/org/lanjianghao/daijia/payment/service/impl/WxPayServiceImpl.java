package org.lanjianghao.daijia.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.servlet.http.HttpServletRequest;
import org.lanjianghao.daijia.common.constant.MqConst;
import org.lanjianghao.daijia.common.service.RabbitService;
import org.lanjianghao.daijia.driver.client.DriverAccountFeignClient;
import org.lanjianghao.daijia.model.entity.payment.PaymentInfo;
import org.lanjianghao.daijia.model.enums.TradeType;
import org.lanjianghao.daijia.model.form.driver.TransferForm;
import org.lanjianghao.daijia.model.form.payment.PaymentInfoForm;
import org.lanjianghao.daijia.model.vo.order.OrderRewardVo;
import org.lanjianghao.daijia.model.vo.payment.WxPrepayVo;
import org.lanjianghao.daijia.order.client.OrderInfoFeignClient;
import org.lanjianghao.daijia.payment.mapper.PaymentInfoMapper;
import org.lanjianghao.daijia.payment.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    @Autowired
    private DriverAccountFeignClient driverAccountFeignClient;


    @Override
    public WxPrepayVo createWxPayment(PaymentInfoForm paymentInfoForm) {
        //查询支付记录看是否有已存在
        LambdaQueryWrapper<PaymentInfo> query = new LambdaQueryWrapper<>();
        query.eq(PaymentInfo::getOrderNo, paymentInfoForm.getOrderNo());
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(query);
        if (paymentInfo == null) {
            paymentInfo = new PaymentInfo();
            BeanUtils.copyProperties(paymentInfoForm, paymentInfo);
            paymentInfo.setPaymentStatus(0);
            paymentInfoMapper.insert(paymentInfo);
        }

        WxPrepayVo vo = new WxPrepayVo();
        //TODO 微信支付逻辑，需要商家账号

        return vo;
    }

    private Boolean getWxPayStatus(String orderNo) {
        //TODO 实现查询微信支付状态逻辑，需要商家账号
        return true;
    }

    @Override
    public Boolean queryPayStatus(String orderNo) {
        Boolean paid = getWxPayStatus(orderNo);
        if (!paid) {
            return false;
        }

        handlePayment(orderNo);
        return true;
    }

    private void handlePayment(String orderNo) {
        LambdaQueryWrapper<PaymentInfo> query = new LambdaQueryWrapper<>();
        query.eq(PaymentInfo::getOrderNo, orderNo);
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(query);

        //已经支付，不需要再更新
        if (paymentInfo.getPaymentStatus() == 1) {
            return;
        }

        PaymentInfo forUpdate = new PaymentInfo();
        forUpdate.setId(paymentInfo.getId());
        forUpdate.setPaymentStatus(1);
        //TODO 更新支付信息，需要微信支付返回结果
//        paymentInfo.setTransactionId(transaction.getTransactionId());
//        paymentInfo.setCallbackTime(new Date());
//        paymentInfo.setCallbackContent(JSON.toJSONString(transaction));
        paymentInfoMapper.updateById(forUpdate);

        //异步消息
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER, MqConst.ROUTING_PAY_SUCCESS, orderNo);
    }

    @Override
    public void wxnotify(HttpServletRequest request) {
        //TODO 微信支付成功通知逻辑

//        //1.回调通知的验签与解密
//        //从request头信息获取参数
//        //HTTP 头 Wechatpay-Signature
//        // HTTP 头 Wechatpay-Nonce
//        //HTTP 头 Wechatpay-Timestamp
//        //HTTP 头 Wechatpay-Serial
//        //HTTP 头 Wechatpay-Signature-Type
//        //HTTP 请求体 body。切记使用原始报文，不要用 JSON 对象序列化后的字符串，避免验签的 body 和原文不一致。
//        String wechatPaySerial = request.getHeader("Wechatpay-Serial");
//        String nonce = request.getHeader("Wechatpay-Nonce");
//        String timestamp = request.getHeader("Wechatpay-Timestamp");
//        String signature = request.getHeader("Wechatpay-Signature");
//        String requestBody = RequestUtils.readData(request);
//        log.info("wechatPaySerial：{}", wechatPaySerial);
//        log.info("nonce：{}", nonce);
//        log.info("timestamp：{}", timestamp);
//        log.info("signature：{}", signature);
//        log.info("requestBody：{}", requestBody);
//
//        //2.构造 RequestParam
//        RequestParam requestParam = new RequestParam.Builder()
//                .serialNumber(wechatPaySerial)
//                .nonce(nonce)
//                .signature(signature)
//                .timestamp(timestamp)
//                .body(requestBody)
//                .build();
//
//
//        //3.初始化 NotificationParser
//        NotificationParser parser = new NotificationParser(rsaAutoCertificateConfig);
//        //4.以支付通知回调为例，验签、解密并转换成 Transaction
//        Transaction transaction = parser.parse(requestParam, Transaction.class);
//        log.info("成功解析：{}", JSON.toJSONString(transaction));
//        if(null != transaction && transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
//            //5.处理支付业务
//            this.handlePayment(transaction);
//        }
    }

    @GlobalTransactional
    @Override
    public void handleOrder(String orderNo) {
        //1 更新订单状态：已支付
        orderInfoFeignClient.updateOrderPayStatus(orderNo);

        //2 获取系统奖励，打入司机账户
        OrderRewardVo orderReward = orderInfoFeignClient.getOrderRewardFee(orderNo).getData();
        if (orderReward != null && orderReward.getRewardFee().compareTo(BigDecimal.ZERO) > 0) {
            TransferForm transferForm = new TransferForm();
            transferForm.setAmount(orderReward.getRewardFee());
            transferForm.setDriverId(orderReward.getDriverId());
            transferForm.setTradeNo(orderNo);
            transferForm.setTradeType(TradeType.REWARD.getType());
            transferForm.setContent(TradeType.REWARD.getContent());
            driverAccountFeignClient.transfer(transferForm);
        }

        //3 其它
    }
}
