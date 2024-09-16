package org.lanjianghao.daijia.common.constant;

public class RedisConstant {

    //用户登录
    public static final String USER_LOGIN_KEY_PREFIX = "user:login:";
    public static final String USER_LOGIN_REFRESH_KEY_PREFIX = "user:login:refresh:";
    public static final int USER_LOGIN_KEY_TIMEOUT = 60 * 60 * 24 * 100;
    public static final int USER_LOGIN_REFRESH_KEY_TIMEOUT = 60 * 60 * 24 * 365;

//    //司机GEO地址
//    public static final String DRIVER_GEO_LOCATION = "driver:geo:location";
    //司机当前位置
    public static final String DRIVER_LOCATION = "driver:location:";
    //司机当前位置过期时间（min）
    public static final long DRIVER_LOCATION_EXPIRES_TIME = 60;
    //订单起点GEO地址
    public static final String ORDER_GEO_LOCATION = "order:geo:location:";
    //订单位置信息
    public static final String ORDER_LOCATION_INFO = "order:locationInfo:";
    //订单推送过的司机集合
    public static final String ORDER_DRIVER_SET = "order:driver:repeat:";
    //订单推送过的司机集合过期时间
    public static final long ORDER_DRIVER_SET_EXPIRES_TIME = 15;
    //司机接单临时容器
    public static final String DRIVER_ORDER_TEMP_LIST = "driver:order:temp:list:";
    public static final long DRIVER_ORDER_TEMP_LIST_EXPIRES_TIME = 1;
    //司机订单去重容器
    public static final String DRIVER_ORDER_REPEAT_LIST = "driver:order:repeat:list:";
    public static final long DRIVER_ORDER_REPEAT_LIST_EXPIRES_TIME = 16;

//    //订单与任务关联
//    public static final String ORDER_JOB = "order:job:";
//    public static final long ORDER_JOB_EXPIRES_TIME = 15;

    //更新订单位置
    public static final String UPDATE_ORDER_LOCATION = "update:order:location:";
    public static final long UPDATE_ORDER_LOCATION_EXPIRES_TIME = 15;

    //订单接单标识
    public static final String ORDER_ACCEPT_MARK = "order:accept:mark:";
    public static final long ORDER_ACCEPT_MARK_EXPIRES_TIME = 15;

    //抢新订单锁
    public static final String ROB_NEW_ORDER_LOCK = "rob:new:order:lock";
    //等待获取锁的时间
    public static final long ROB_NEW_ORDER_LOCK_WAIT_TIME = 1;
    //加锁的时间
    public static final long ROB_NEW_ORDER_LOCK_LEASE_TIME = 1;

    //优惠券信息
    public static final String COUPON_INFO = "coupon:info:";

    //优惠券分布式锁
    public static final String COUPON_LOCK = "coupon:lock:";
    //等待获取锁的时间
    public static final long COUPON_LOCK_WAIT_TIME = 1;
    //加锁的时间
    public static final long COUPON_LOCK_LEASE_TIME = 1;

    public static final String ORDER_PENDING_SET = "order:pending:";
}
