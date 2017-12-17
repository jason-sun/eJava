package com.erlitech.ejava.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 连接数据库的工具类,被定义成不可继承且是私有访问
 *
 * @author 孙振强
 * @since 2017-07-18
 */
public final class XDateUtil {

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    public static long getTimeStamp() {
        Date date = new Date();
        return date.getTime();
    }

    /**
     * 获取当前日期时间, 格式化
     *
     * @return 当日期时间 yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTime() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return formatter.format(date);
    }

    /**
     * 获取当前时间, 格式化
     *
     * @return 当时间时间 HH:mm:ss
     */
    public static String getTime() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

        return formatter.format(date);
    }
}
