package com.erlitech.ejava.controller;

import com.alibaba.fastjson.JSONObject;
import com.erlitech.ejava.service.ApiLogService;
import com.erlitech.ejava.utils.XEncoder;
import com.erlitech.ejava.utils.XLoggerUtil;
import com.erlitech.ejava.utils.XPropertyUtil;
import com.erlitech.ejava.utils.XdbUtil;
import org.apache.commons.lang.StringUtils;

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
 */
public class ApiController {

    public static final Logger LOGGER = XLoggerUtil.getLogger(ApiController.class.getName());
    public static final String PACKAGE = XPropertyUtil.getProperty("package", "controller");
    public static final String ENCODE = XPropertyUtil.getProperty("encode", "controller");
    public static final String ALLOW_ACCESS = XPropertyUtil.getProperty("allowAccess", "controller");
    public static final String ALLOW_DB_LOG = XPropertyUtil.getProperty("allowDbLog", "controller");
    public static final String DB_LOG_API_FILTER = XPropertyUtil.getProperty("dbLogApiFilter", "controller");

    /**
     * 路由
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    public static void router(HttpServletRequest request, HttpServletResponse response) {
        if (!requestAllowAccess(request, response)) {
            return;
        }

        response.setCharacterEncoding("utf-8");

        String inData = request.getParameter("inData");
        String outData = "";

        if (ENCODE.equals("1")) {
            inData = XEncoder.decode(inData);
        }

        outData = invokeApi(inData, request, response);

        if (null == outData || "".equals(inData)) {
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
            LOGGER.log(Level.INFO, "output error.");
        }
    }

    public static String invokeApi(String inData, HttpServletRequest request, HttpServletResponse response) {
        String outData = null;

        JSONObject joInData = JSONObject.parseObject(inData);
        JSONObject joOutData = new JSONObject();

        String api = joInData.getString("api");

        if (StringUtils.isEmpty(api)) {
            LOGGER.log(Level.SEVERE, "inData参数api缺失。");
            return outData;
        }


        JSONObject apiLog = new JSONObject();

        if (ALLOW_DB_LOG.equals("1") && DB_LOG_API_FILTER.indexOf("|" + api + "|") == -1) {
            apiLog = ApiLogService.addLog(joInData, joOutData, getIp(request), getUserAgent(request), getReferer(request));
        }

        String apiArray[] = api.split("\\.");

        Class<?> xService;
        Method xMethod;

        try {
            String className = apiArray[0];
            className = className.substring(0, 1).toUpperCase() + className.substring(1);

            //一般尽量采用这种形式
            xService = Class.forName(PACKAGE + "." + className + "Controller");
            xMethod = xService.getMethod(apiArray[1], JSONObject.class, HttpServletRequest.class, HttpServletResponse.class);
            joOutData = (JSONObject) xMethod.invoke(xService.newInstance(), joInData, request, response);
        } catch (ClassNotFoundException ex) {
            joOutData.put("error", 1710021441);
            joOutData.put("errorMessage", "Class：" + apiArray[0] + " 不存在。");
            LOGGER.severe(joOutData.toString());
        } catch (NoSuchMethodException ex) {
            joOutData.put("error", 1710021442);
            joOutData.put("errorMessage", "Method：" + apiArray[0] + "." + apiArray[1] + " 不存在。");
            LOGGER.severe(joOutData.toString());
        } catch (SecurityException ex) {
            joOutData.put("error", 1710021443);
            joOutData.put("errorMessage", "Security错误。");
            joOutData.put("errorDetail", ex.getCause().getStackTrace()[0].toString());
            LOGGER.severe(joOutData.toString());
        } catch (InstantiationException ex) {
            joOutData.put("error", 1710021444);
            joOutData.put("errorMessage", "Instantiation错误。");
            joOutData.put("errorDetail", ex.getCause().getStackTrace()[0].toString());
            LOGGER.severe(joOutData.toString());
        } catch (IllegalAccessException ex) {
            joOutData.put("error", 1710021445);
            joOutData.put("errorMessage", "IllegalAccess错误。");
            joOutData.put("errorDetail", ex.getCause().getStackTrace()[0].toString());
            LOGGER.severe(joOutData.toString());
        } catch (IllegalArgumentException ex) {
            joOutData.put("error", 1710021446);
            joOutData.put("errorMessage", "IllegalArgument错误。");
            joOutData.put("errorDetail", ex.getCause().getStackTrace()[0].toString());
            LOGGER.severe(joOutData.toString());
        } catch (InvocationTargetException ex) {
            joOutData.put("error", 1710021447);
            joOutData.put("errorMessage", "InvocationTarget错误。");
            joOutData.put("errorDetail", ex.getCause().getStackTrace()[0].toString());
            LOGGER.severe(joOutData.toString());
        } finally {
            XdbUtil.closeConnection();
        }


        if (!apiLog.isEmpty()) {
            ApiLogService.updateLog(apiLog, joOutData);
        }

        // 当为文件下载时，取消文本输出
        if (joOutData.getString("fileDownload") != null && joOutData.getString("fileDownload").equals("1")) {
            return outData;
        }

        outData = joOutData.toString();


        return outData;
    }

    public static String getIp(HttpServletRequest request) {
        String ip = "";

        try {
            ip = request.getHeader("X-Forwarded-For");
        } catch (Exception ex) {
            return "0.0.0.0";
        }

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

    public static String getUserAgent(HttpServletRequest request) {
        try {
            return request.getHeader("User-Agent");
        } catch (Exception ex) {
            return "";
        }
    }

    public static String getReferer(HttpServletRequest request) {
        try {
            return request.getHeader("Referer");
        } catch (Exception ex) {
            return "";
        }
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
