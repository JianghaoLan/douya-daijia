package org.lanjianghao.daijia.common.login;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.lanjianghao.daijia.common.constant.RedisConstant;
import org.lanjianghao.daijia.common.execption.BusinessException;
import org.lanjianghao.daijia.common.result.ResultCodeEnum;
import org.lanjianghao.daijia.common.util.AuthContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
public class LoginAspect {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Around(value = "execution(* org.lanjianghao.daijia.*.controller.*.*(..)) && @annotation(loginRequired)")
    public Object login(ProceedingJoinPoint proceedingJoinPoint, LoginRequired loginRequired) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();

        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCodeEnum.LOGIN_AUTH);
        }

        String customerIdStr = redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
        if (!StringUtils.hasText(customerIdStr)) {
            throw new BusinessException(ResultCodeEnum.LOGIN_AUTH);
        }

        Long customerId = Long.parseLong(customerIdStr);
        AuthContextHolder.setUserId(customerId);
        return proceedingJoinPoint.proceed();
    }
}
