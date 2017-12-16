package com.erlitech.ejava.utils;

import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sunzhenqiang
 */
public class EHttpClientUtil {

    // 通过eJava方式直接获取http请求结果，输入输出均为JSONObject
    public static JSONObject getData(String url, JSONObject joInData) {
        JSONObject joOutData = new JSONObject();
        Map<String, Object> params = new HashMap<>();
        params.put("inData", XEncoder.encode(joInData.toString()));

        try {
            String result = null;
            result = XHttpClientUtil.httpPostRequest(url, params);

            if (result.indexOf("\"") == 0 && result.lastIndexOf("\"") == result.length() - 1) {
                result = result.substring(1, result.length() - 1);
            }

            result = XEncoder.decode(result);
            joOutData = JSONObject.parseObject(result);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return joOutData;
    }
}
