package com.erlitech.ejava.utils;

import java.text.ParseException;
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
        long timeStamp = date.getTime();

        return timeStamp;
    }

    /**
     * 获取当前日期时间, 格式化
     *
     * @return 当日期时间 yyyy-MM-dd HH:mm:ss
     */
    public static String getDateTime() {
        return getDateTime(String.valueOf(getTimeStamp()));
    }

    public static String getDateTime(String timeStamp) {
        return getFormatTime(timeStamp, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 获取当前时间, 格式化
     *
     * @return 当时间时间 yyyy-MM-dd
     */
    public static String getDate() {
        return getDate(String.valueOf(getTimeStamp()));
    }

    public static String getDate(String timeStamp) {
        return getFormatTime(timeStamp, "yyyy-MM-dd");
    }

    /**
     * 获取当前时间, 格式化
     *
     * @return 当时间时间 HH:mm:ss
     */
    public static String getTime() {
        return getTime(String.valueOf(getTimeStamp()));
    }

    public static String getTime(String timeStamp) {
        return getFormatTime(timeStamp, "HH:mm:ss");
    }


    private static String getFormatTime(String timeStamp, String format) {
        Date date = new Date(new Long(timeStamp));
        SimpleDateFormat formatter = new SimpleDateFormat(format);

        return formatter.format(date);
    }

    /*
     * 将时间转换为时间戳
     * @return 时间戳
     */
    public static Long toTimeStamp(String dataTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;

        try {
            date = formatter.parse(dataTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long timeStamp = date.getTime();
        return timeStamp;
    }
}
