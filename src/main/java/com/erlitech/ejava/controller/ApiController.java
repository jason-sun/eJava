package com.erlitech.ejava.controller;

import com.alibaba.fastjson.JSONObject;
import com.erlitech.ejava.service.ApiLogService;
import com.erlitech.ejava.utils.XEncoder;
import com.erlitech.ejava.utils.XLoggerUtil;
import com.erlitech.ejava.utils.XPropertyUtil;
import com.erlitech.ejava.utils.XdbUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Api Controller 统一路由
 *
 * @author 孙振强
 * @date 2017-08-15
 */
public class ApiController {

    private static final Logger LOGGER = XLoggerUtil.getLogger(ApiController.class.getName());
    private static final String PACKAGE = XPropertyUtil.getProperty("package", "controller");
    private static final String ENCODE = XPropertyUtil.getProperty("encode", "controller");
    private static final String ALLOW_ACCESS = XPropertyUtil.getProperty("allowAccess", "controller");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String inData = "{\"api\":\"RecordService.mergeRecordFile\"}";
//        String test = router(inData, );
//        System.out.println(test);
    }

    /**
     * 路由
     *
     * @param request
     * @param response
     */
    public static void router(HttpServletRequest request, HttpServletResponse response) {
        if (!requestAllowAccess(request, response)) {
            return;
        }

        String inData = request.getParameter("inData");
        String outData = "";

        if (ENCODE.equals("1")) {
            inData = XEncoder.decode(inData);
        }

        JSONObject joInData = JSONObject.parseObject(inData);
        JSONObject joOutData = new JSONObject();

        String api = joInData.getString("api");

        if (StringUtils.isEmpty(api)) {
            LOGGER.log(Level.SEVERE, "inData参数api缺失。");
            return;
        }

        String apiArray[] = api.split("\\.");

        Class<?> xService;
        Method xMethod;

        try {
            JSONObject apiLog = ApiLogService.addLog(joInData, joOutData, getIp(request), request.getHeader("User-Agent"), request.getHeader("Referer"));

            //一般尽量采用这种形式
            xService = Class.forName(PACKAGE + "." + apiArray[0] + "Controller");
            xMethod = xService.getMethod(apiArray[1], JSONObject.class, HttpServletRequest.class, HttpServletResponse.class);
            joOutData = (JSONObject) xMethod.invoke(xService.newInstance(), joInData, request, response);
            outData = joOutData.toString();

            ApiLogService.updateLog(apiLog, joOutData);
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Class：" + apiArray[0] + " 不存在", ex);
        } catch (NoSuchMethodException ex) {
            LOGGER.log(Level.SEVERE, "Method：" + apiArray[1] + " 不存在", ex);
        } catch (SecurityException ex) {
            LOGGER.log(Level.SEVERE, "Security错误", ex);
        } catch (InstantiationException ex) {
            LOGGER.log(Level.SEVERE, "Instantiation错误", ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, "IllegalAccess错误", ex);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.SEVERE, "IllegalArgument错误", ex);
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, "InvocationTarget错误", ex);
        } finally {
            XdbUtil.closeConnection();
        }

        // 当为文件下载时，取消文本输出
        if (joOutData.getString("fileDownload") != null && joOutData.getString("fileDownload").equals("1")) {
            return;
        }

        try {
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();

            if (ENCODE.equals("1")) {
                outData = XEncoder.encode(outData);
            }

            out.print(outData);
        } catch (IOException e) {
        }
    }

    protected static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }

        ip = request.getHeader("X-Real-IP");

        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    public static boolean requestAllowAccess(HttpServletRequest request, HttpServletResponse response) {
        if (ALLOW_ACCESS == null || ALLOW_ACCESS.equals("")) {
            return true;
        }

        String referer = request.getHeader("referer");

        if (referer == null) {
            return true;
        }

        String origin = referer.substring(0, referer.indexOf("/", 10));

        if (!ALLOW_ACCESS.equals("*") && !ALLOW_ACCESS.contains(origin)) {
            return false;
        }

        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Max-Age", "100");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        return true;
    }
}
