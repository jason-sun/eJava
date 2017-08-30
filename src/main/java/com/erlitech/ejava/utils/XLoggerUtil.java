package com.erlitech.ejava.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class XLoggerUtil {

    // 初始化LogManager
    static {
        // 读取配置文件
        ClassLoader cl = XLoggerUtil.class.getClassLoader();
        InputStream inputStream;

        if (cl != null) {
            inputStream = cl.getResourceAsStream("logger.properties");
        } else {
            inputStream = ClassLoader.getSystemResourceAsStream("logger.properties");
        }

        java.util.logging.LogManager logManager = java.util.logging.LogManager.getLogManager();

        try {
            // 重新初始化日志属性并重新读取日志配置。
            logManager.readConfiguration(inputStream);
        } catch (SecurityException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * 获取日志对象
     *
     * @param className 类名
     * @return
     */
    public static Logger getLogger(String className) {
        Logger logger = Logger.getLogger(className);

        return logger;
    }

}