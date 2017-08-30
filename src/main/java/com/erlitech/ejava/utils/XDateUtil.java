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
     * @return
     */
    public static long getTimeStamp() {
        Date date = new Date();
        long timestamp = date.getTime();
        return timestamp;
    }

    /**
     * 获取当前时间, 格式化
     *
     * @return
     */
    public static String getDateTime() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = formatter.format(date);

        return datetime;
    }
}
