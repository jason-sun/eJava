package com.erlitech.ejava.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 配置文件properties的操作
 *
 * @author 孙振强
 * @version 1.0
 * @since 2017-08-15
 */
public class XPropertyUtil {

    private static final Logger LOGGER = XLoggerUtil.getLogger(XPropertyUtil.class.getName());
    private static final Map<String, Properties> PROPERTIES = new HashMap<>();

    /**
     * 加载properties文件
     */
    private static void loadPropertiesFile(String fileName) {
        LOGGER.info("properties文件: " + fileName + "，开始加载...");

        try {
            Properties properties = new Properties();
            try (InputStream inputStream = XPropertyUtil.class.getClassLoader().getResourceAsStream(fileName + ".properties")) {
                properties.load(inputStream);
                PROPERTIES.put(fileName, properties);
            }
            LOGGER.info("properties文件: " + fileName + "，加载完成");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "properties文件: " + fileName + ".properties，IO错误。" + e);
        }
    }

    /**
     * 根据Key读取Value
     *
     * @param key      键
     * @param fileName 文件名
     * @return 值
     */
    public static String getProperty(String key, String fileName) {
        if (!PROPERTIES.containsKey(fileName)) {
            loadPropertiesFile(fileName);
        }

        Properties properties = PROPERTIES.get(fileName);

        return properties.getProperty(key);
    }
}
