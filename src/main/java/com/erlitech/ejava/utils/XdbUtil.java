package com.erlitech.ejava.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * jdbc操作类
 *
 * @author 孙振强
 */
public final class XdbUtil {

    private static final Logger LOGGER = XLoggerUtil.getLogger(XdbUtil.class.getName());
    public static String propertyName = "jdbc";
    private static Connection connection = null;

    private XdbUtil() {
    }

    /**
     * 获得数据库的连接
     *
     * @return connection对象
     * @throws java.sql.SQLException SQL错误捕获
     */
    public static Connection getConnection() throws SQLException {
        if (null == connection || !connection.isValid(3)) {
            String url = XPropertyUtil.getProperty("url", propertyName);
            String username = XPropertyUtil.getProperty("username", propertyName);
            String password = XPropertyUtil.getProperty("password", propertyName);
            String driver = XPropertyUtil.getProperty("driver", propertyName);

            try {
                Class.forName(driver);
                connection = DriverManager.getConnection(url, username, password);
                connection.isValid(5);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "数据库连接失败。" + e);
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "数据库驱动加载失败。" + e);
            }
        }

        return connection;
    }

    /**
     * 查询多条记录
     *
     * @param xql xql对象
     * @return 查询结果JSONArray
     */
    public static JSONArray readList(XqlUtil xql) {

        return select(xql.getSelectSql());
    }

    /**
     * 查询单条记录
     *
     * @param xql xql对象
     * @return 查询结果JSONObject
     */
    public static JSONObject readOne(XqlUtil xql) {
        xql.setLimit("0,1");
        JSONArray jaList = select(xql.getSelectSql());

        JSONObject joInfo;

        if (jaList.isEmpty()) {
            joInfo = new JSONObject();
        } else {
            joInfo = (JSONObject) jaList.get(0);
        }

        return joInfo;
    }

    /**
     * 查询单条记录单值
     *
     * @param xql xql对象
     * @param key 查询字段
     * @return 查询结果String
     */
    public static String readValue(XqlUtil xql, String key) {
        JSONObject joInfo = readOne(xql);

        String value;

        if (joInfo.isEmpty()) {
            value = "";
        } else {
            value = joInfo.getString(key);
        }

        return value;
    }

    /**
     * 查询记录, 输入sql
     *
     * @param sql 查询SQL语句
     * @return 查询结果JSONArray
     */
    public static JSONArray select(String sql) {
        JSONArray jaList;

        jaList = executeQuery(sql);

        return jaList;
    }

    /**
     * 执行SQL，查
     *
     * @param sql 查询SQL语句
     * @return 查询结果JSONArray
     */
    public static JSONArray executeQuery(String sql) {
        Statement statement = null;
        ResultSet resultSet = null;
        JSONArray jaList = new JSONArray();

        LOGGER.log(Level.INFO, sql);

        try {
            getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int colsLen = metaData.getColumnCount();

            while (resultSet.next()) {
                JSONObject joInfo = new JSONObject();

                for (int i = 0; i < colsLen; i++) {
                    String key = metaData.getColumnName(i + 1);
                    Object value = resultSet.getObject(key);

                    if (value == null) {
                        value = "";
                    }

                    joInfo.put(key, value);
                }

                jaList.add(joInfo);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL执行错误。" + e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
        }

        return jaList;
    }

    /**
     * 新增记录
     *
     * @param xql xql对象
     * @return 新增记录主键
     */
    public static String instert(XqlUtil xql) {
        String key;

        key = executeInsert(xql.getInsertSql());

        return key;
    }

    /**
     * 删除记录
     *
     * @param xql xql对象
     * @return 删除记录数
     */
    public static Integer delete(XqlUtil xql) {
        Integer integer;

        integer = executeUpdate(xql.getDeleteSql());

        return integer;
    }

    /**
     * 修改记录
     *
     * @param xql xql对象
     * @return 修改记录数
     */
    public static Integer update(XqlUtil xql) {
        Integer integer;

        integer = executeUpdate(xql.getUpdateSql());

        return integer;
    }

    /**
     * 执行SQL，增、删、改
     *
     * @param sql SQL语句
     * @return 执行结果
     */
    public static Integer executeUpdate(String sql) {
        Statement statement = null;
        Integer integer = null;

        LOGGER.log(Level.INFO, sql);

        try {
            getConnection();
            statement = connection.createStatement();
            integer = statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL执行错误。" + e);
        } finally {
            closeStatement(statement);
        }

        return integer;
    }

    /**
     * 执行SQL，增
     *
     * @param sql SQL语句
     * @return 执行结果
     */
    public static String executeInsert(String sql) {
        Statement statement = null;
        ResultSet resultSet = null;
        String key = null;

        LOGGER.log(Level.INFO, sql);

        try {
            getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            //检索由于执行此 Statement 对象而创建的所有自动生成的键
            resultSet = statement.getGeneratedKeys();

            if (resultSet.next()) {
                //知其仅有一列，故获取第一列
                key = resultSet.getString(1);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL执行错误。" + e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
        }

        return key;
    }

    /**
     * 执行SQL，批量，无返回
     *
     * @param list SQL语句List
     */
    public static void executeBatchStaticSQL(List<String> list) {
        Statement statement = null;

        try {
            getConnection();
            statement = connection.createStatement();

            for (String sql : list) {
                LOGGER.log(Level.INFO, sql);
                statement.addBatch(sql);
            }

            statement.executeBatch();
            statement.clearBatch();
            statement.close();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                LOGGER.log(Level.SEVERE, "操作回滚错误。" + e1);
            }

            LOGGER.log(Level.SEVERE, "SQL执行错误。" + e);
        } finally {
            closeStatement(statement);
        }
    }

    /**
     * 关闭Statement
     */
    private static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Statement 关闭错误。" + e);
            }
        }
    }

    /**
     * 关闭ResultSet
     */
    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "ResultSet 关闭错误。" + e);
            }
        }
    }

    /**
     * 关闭Connection
     */
    public static void closeConnection() {
//        if (connection != null) {
//            try {
//                connection.close();
//                connection = null;
//            } catch (SQLException e) {
//                LOGGER.log(Level.WARNING, "connection 关闭错误。" + e);
//            }
//        }
    }

    /**
     * 新增记录，批量
     *
     * @param listXql Xql对象List
     */
    public static void instertList(List<XqlUtil> listXql) {
        List<String> list = new ArrayList<>();

        for (XqlUtil xql : listXql) {
            list.add(xql.getInsertSql());
        }

        executeBatchStaticSQL(list);
    }
}
