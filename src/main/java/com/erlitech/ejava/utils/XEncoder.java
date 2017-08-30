package com.erlitech.ejava.utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 字符串加密码处理类
 *
 * @author 孙振强
 * @since 2014-01-10
 */
public class XEncoder {

    public static String encode(String data) {
        //判断data是否为空
        if (data == null || data.length() == 0) {
            return "";
        }

        //将data进行url编码
        try {
            data = java.net.URLEncoder.encode(data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("error: " + e);
        }

        //将url编码后的data中的加号（即原来的空格）替换为"%20"
        data = data.replaceAll("\\+", "%20");

        //定义被替换的字符
        String[] a = {"%22", "%2C", "%3A", "%5B", "%5D", "%7B", "%7D"};

        //定义被替换后的字符并随机排序
        String[] b = {"^", "`", "\\$", "<", ",", ">", "@", ":"};
        List<String> c = Arrays.asList(b);
        Collections.shuffle(c);
        b = (String[]) c.toArray();

        //定义密钥字符串
        String d = "";

        //将data进行替换
        int i;
        for (i = 0; i < a.length; i++) {
            d += b[i];
            data = data.replaceAll(a[i], b[i]);
        }

        data = data.replace("%", b[7]);
        d += b[7];

        //反转data
        data = new StringBuffer(data).reverse().toString();

        //增加首尾
        data = "df" + data.replace("\\", "") + d.replace("\\", "") + "wf";

        return data;
    }

    public static String decode(String data) {
        //判断data是否为空
        if (data == null || data.length() == 0) {
            return "";
        }

        //定义data_length，获取data长度
        int data_length = data.length();

        //判断data是否wf编码格式
        if (data_length < 10 || !data.startsWith("df") || !data.endsWith("wf")) {
            return data;
        }

        //定义替换后的字符
        String[] a = {"%22", "%2C", "%3A", "%5B", "%5D", "%7B", "%7D", "%"};

        //定义并获取被替换的字符
        String[] b = data.substring(data_length - 10, data_length - 2).split("");

        //提取data
        data = data.substring(2, data_length - 10);

        //反转data
        data = new StringBuffer(data).reverse().toString();

        //遍历替换
        int i;

        for (i = 0; i < a.length; i++) {
            data = data.replace(b[i + b.length - a.length], a[i]);
        }

        //将data进行url解码
        try {
            data = java.net.URLDecoder.decode(data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("error: " + e);
        }

        return data;
    }

}
