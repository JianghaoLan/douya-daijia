package org.lanjianghao.daijia.payment.service;

import jakarta.servlet.http.HttpServletRequest;
import org.lanjianghao.daijia.model.form.payment.PaymentInfoForm;
import org.lanjianghao.daijia.model.vo.payment.WxPrepayVo;

public interface WxPayService {


    WxPrepayVo createWxPayment(PaymentInfoForm paymentInfoForm);

    Boolean queryPayStatus(String orderNo);

    void wxnotify(HttpServletRequest request);

    void handleOrder(String orderNo);
}
