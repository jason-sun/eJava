package com.erlitech.ejava.service;

import com.alibaba.fastjson.JSONObject;
import com.erlitech.ejava.utils.XDateUtil;
import com.erlitech.ejava.utils.XdbUtil;
import com.erlitech.ejava.utils.XqlUtil;

/**
 * Log服务(数据库)
 *
 * @author 孙振强
 */
public class ApiLogService {

    /**
     * 获取共享数据中心接口结果
     *
     * @param joInData 输入JSONObject
     * @param joOutData 输出JSONObject
     * @param ip 访问ip
     * @param userAgent 访问userAgent
     * @param referer 访问来源
     * @return Log对象
     */
    public static JSONObject addLog(JSONObject joInData, JSONObject joOutData, String ip, String userAgent, String referer) {
        String id;
        JSONObject log = new JSONObject();
        XqlUtil xql = new XqlUtil();

        if (referer == null) {
            referer = "";
        }

        log.put("ctime", XDateUtil.getDateTime());
        log.put("ctime2", XDateUtil.getTimeStamp());
        log.put("input", joInData.toString());
        log.put("output", joOutData.toString());
        log.put("ip", ip);
        log.put("user_agent", userAgent);
        log.put("referer", referer);

        xql.setTable("sf_api_log");
        xql.setValue(log);

        id = XdbUtil.instert(xql);

        log.put("id", id);

        return log;

    }

    /**
     * 获取共享数据中心接口结果
     *
     * @param oldLog 之前Log
     * @param joOutData  输出JSONObject
     */
    public static void updateLog(JSONObject oldLog, JSONObject joOutData) {
        JSONObject log = new JSONObject();
        XqlUtil xql = new XqlUtil();

        log.put("utime", XDateUtil.getDateTime());
        log.put("utime2", XDateUtil.getTimeStamp());
        log.put("output", joOutData.toString());
        log.put("runtime", XDateUtil.getTimeStamp() - oldLog.getLong("ctime2"));

        xql.setTable("sf_api_log");
        xql.setValue(log);
        xql.setWhere("id=\"" + oldLog.getString("id") + "\"");

        XdbUtil.update(xql);
    }
}
