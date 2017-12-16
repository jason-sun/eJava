package com.erlitech.ejava.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * jdbc操作类
 * V2.0
 * 2017-11-08
 * @author 孙振强
 */
public class XdbUtil {

    private static final Logger LOGGER = XLoggerUtil.getLogger(XdbUtil.class.getName());
    public static String propertyName = "jdbc";
    public static String allowResultNull = "1";
    public static HashMap<String, Connection> connectionMap = new HashMap<>();

    /**
     * 获得数据库的连接
     *
     * @param jdbcName jdbc配置文件名称
     * @return connection 数据库连接
     * @throws java.sql.SQLException SQL错误捕获
     */
    public static Connection getConnection(String jdbcName) throws SQLException {
        Connection connection = null;

        if (null == connectionMap.get(jdbcName) || !connectionMap.get(jdbcName).isValid(5)) {
            String url = XPropertyUtil.getProperty("jdbc_url", jdbcName);
            String username = XPropertyUtil.getProperty("jdbc_username", jdbcName);
            String password = XPropertyUtil.getProperty("jdbc_password", jdbcName);
            String driver = XPropertyUtil.getProperty("xdb_driver", jdbcName);
            allowResultNull = XPropertyUtil.getProperty("xdb_allowResultNull", jdbcName);

            try {
                Class.forName(driver);
                connection = DriverManager.getConnection(url, username, password);
                connection.isValid(5);
                connectionMap.put(jdbcName, connection);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "数据库连接失败。" + e);
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "数据库驱动加载失败。" + e);
            }
        } else {
            connection = connectionMap.get(jdbcName);
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
        return readList(xql, propertyName);
    }

    public static JSONArray readList(XqlUtil xql, String jdbcName) {
        return select(xql.getSelectSql(), jdbcName);
    }

    public static JSONArray readList(String sql) {
        return readList(sql, propertyName);
    }

    public static JSONArray readList(String sql, String jdbcName) {
        return select(sql, jdbcName);
    }

    /**
     * 查询单条记录
     *
     * @param xql xql对象
     * @return 查询结果JSONObject
     */
    public static JSONObject readOne(XqlUtil xql) {
        return readOne(xql, propertyName);
    }

    public static JSONObject readOne(XqlUtil xql, String jdbcName) {
        xql.setLimit("1");
        JSONArray jaList = select(xql.getSelectSql(), jdbcName);

        JSONObject joInfo;

        if (jaList.isEmpty()) {
            joInfo = new JSONObject();
        } else {
            joInfo = (JSONObject) jaList.get(0);
        }

        return joInfo;
    }

    public static JSONObject readOne(String sql) {
        return readOne(sql, propertyName);
    }

    public static JSONObject readOne(String sql, String jdbcName) {
        JSONArray jaList = select(sql, jdbcName);

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
    public static String readValue(XqlUtil xql, String key)  {
        return readValue(xql, key, propertyName);
    }

    public static String readValue(XqlUtil xql, String key, String jdbcName) {
        JSONObject joInfo = readOne(xql, jdbcName);

        String value;

        if (joInfo.isEmpty()) {
            value = "";
        } else {
            value = joInfo.getString(key);
        }

        return value;
    }

    public static String readValue(String sql, String key)  {
        return readValue(sql, key, propertyName);
    }

    public static String readValue(String sql, String key, String jdbcName) {
        JSONObject joInfo = readOne(sql, jdbcName);

        String value;

        if (joInfo.isEmpty()) {
            value = "";
        } else {
            value = joInfo.getString(key);
        }

        return value;
    }

    /**
     * 查询记录数, 输入xql
     *
     * @param xql xql对象
     * @return 查询结果Integer
     */
    public static Integer readNum(XqlUtil xql) {
        return readNum(xql, propertyName);
    }

    public static Integer readNum(XqlUtil xql, String jdbcName) {
        xql.setField("COUNT(*) AS num");

        String num = readValue(xql, "num", jdbcName);

        return Integer.valueOf(num);
    }

    public static Integer readNum(String sql) {
        return readNum(sql, propertyName);
    }

    public static Integer readNum(String sql, String jdbcName) {
        JSONArray jaList;
        final Integer[] num = {0};

        jaList = executeQuery(sql, jdbcName);

        if (!jaList.isEmpty()) {
            JSONObject joInfo = jaList.getJSONObject(0);

            joInfo.forEach((String k, Object v) ->{
                num[0] = joInfo.getInteger(k);
            });
        }

        return num[0];
    }

    /**
     * 查询记录, 输入sql
     *
     * @param sql 查询SQL语句
     * @return 查询结果JSONArray
     */
    public static JSONArray select(String sql) {
        return select(sql, propertyName);
    }

    public static JSONArray select(String sql, String jdbcName) {
        JSONArray jaList;

        jaList = executeQuery(sql, jdbcName);

        return jaList;
    }

    /**
     * 执行SQL，查
     *
     * @param sql 查询SQL语句
     * @return 查询结果JSONArray
     */
    public static JSONArray executeQuery(String sql) {
        return executeQuery(sql, propertyName);
    }

    public static JSONArray executeQuery(String sql, String jdbcName) {
        JSONArray jaList = new JSONArray();

        LOGGER.log(Level.INFO, sql);

        try {
            Connection connection = getConnection(jdbcName);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int colsLen = metaData.getColumnCount();

            while (resultSet.next()) {
                JSONObject joInfo = new JSONObject();

                for (int i = 0; i < colsLen; i++) {
                    String type = metaData.getColumnTypeName(i + 1);
                    String key = metaData.getColumnLabel(i + 1);
                    Object value;

                    switch (type) {
                        case "DATE":
                        case "TIME":
                            value = resultSet.getString(key);
                            break;
                        case "DATETIME":
                        case "TIMESTAMP":
                            String datatime = resultSet.getString(key);

                            if (datatime.indexOf(".") > 0) {
                                datatime = datatime.substring(0, datatime.indexOf("."));
                            }

                            value = datatime;
                            break;
                        default:
                            value = resultSet.getObject(key);
                            break;
                    }

                    // 如果为null，则默认为空
                    if ("0".equals(allowResultNull) && null == value) {
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
        }

        return jaList;
    }

    /**
     * 查询记录数, 输入sql
     *
     * @param sql 查询SQL语句
     * @return 查询结果Integer
     */
    public static Integer readNumBySql(String sql) {
        return readNumBySql(sql, propertyName);
    }

    public static Integer readNumBySql(String sql, String jdbcName) {
        Integer num = 0;

        LOGGER.log(Level.INFO, sql);

        try {
            Connection connection = getConnection(jdbcName);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            resultSet.last();
            num = resultSet.getRow();

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL执行错误。" + e);
        }

        return num;
    }

    /**
     * 新增记录
     *
     * @param xql xql对象
     * @return 新增记录主键
     */
    public static String insert(XqlUtil xql)  {
        return insert(xql, propertyName);
    }

    public static String insert(XqlUtil xql, String jdbcName) {
        String key;

        key = executeInsert(xql.getInsertSql(), jdbcName);

        return key;
    }

    /**
     * 删除记录
     *
     * @param xql xql对象
     * @return 删除记录数
     */
    public static Integer delete(XqlUtil xql)  {
        return delete(xql, propertyName);
    }

    public static Integer delete(XqlUtil xql, String jdbcName) {
        Integer integer;

        integer = executeUpdate(xql.getDeleteSql(), jdbcName);

        return integer;
    }

    /**
     * 修改记录
     *
     * @param xql xql对象
     * @return 修改记录数
     */
    public static Integer update(XqlUtil xql)  {
        return update(xql, propertyName);
    }

    public static Integer update(XqlUtil xql, String jdbcName) {
        Integer integer;

        integer = executeUpdate(xql.getUpdateSql(), jdbcName);

        return integer;
    }

    /**
     * 执行SQL，增、删、改
     *
     * @param sql SQL语句
     * @return 执行结果
     */
    public static Integer executeUpdate(String sql) {
        return executeUpdate(sql, propertyName);
    }

    public static Integer executeUpdate(String sql, String jdbcName) {
        Integer integer = null;

        LOGGER.log(Level.INFO, sql);

        try {
            Connection connection = getConnection(jdbcName);
            Statement statement = connection.createStatement();
            integer = statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL执行错误。" + e);
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
        return executeInsert(sql, propertyName);
    }

    public static String executeInsert(String sql, String jdbcName) {
        String key = null;

        LOGGER.log(Level.INFO, sql);

        try {
            Connection connection = getConnection(jdbcName);
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            //检索由于执行此 Statement 对象而创建的所有自动生成的键
            ResultSet resultSet = statement.getGeneratedKeys();

            if (resultSet.next()) {
                //知其仅有一列，故获取第一列
                key = resultSet.getString(1);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL执行错误。" + e);
        }

        return key;
    }

    /**
     * 执行SQL，批量，无返回
     *
     * @param list SQL语句List
     * @return 执行结果
     */
    public static boolean executeBatchStaticSQL(List<String> list) {
        return executeBatchStaticSQL(list, propertyName);
    }

    public static boolean executeBatchStaticSQL(List<String> list, String jdbcName) {
        Connection connection = null;

        try {
            connection = getConnection(jdbcName);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            Statement statement = connection.createStatement();

            for (String sql : list) {
                LOGGER.log(Level.INFO, sql);
                statement.addBatch(sql);
            }

            statement.executeBatch();
            statement.clearBatch();
            statement.close();

            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                LOGGER.log(Level.SEVERE, "操作回滚错误。" + e1);
            }

            LOGGER.log(Level.SEVERE, "SQL执行错误。" + e);

            return false;
        }
    }

    /**
     * 关闭Connection
     */
    public static void closeConnection() {
        if (connectionMap.isEmpty()) {
            return;
        }

        Connection connection = null;

        for (String key : connectionMap.keySet()) {
            try {
                connection = connectionMap.get(key);
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "connection 关闭错误。" + e);
            }
        }

        connectionMap.clear();
    }

    public static void closeConnection(String jdbcName) {
        Connection connection = null;

        try {
            connection = getConnection(jdbcName);
            connection.close();
            connectionMap.remove(jdbcName);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "connection 关闭错误。" + e);
        }
    }

    /**
     * 新增记录，批量Xql
     *
     * @param listXql Xql对象List
     */
    public static void insertList(List<XqlUtil> listXql) {
        insertList(listXql, propertyName);
    }

    public static void insertList(List<XqlUtil> listXql, String jdbcName) {
        List<String> list = new ArrayList<>();

        for (XqlUtil xql : listXql) {
            list.add(xql.getInsertSql());
        }

        executeBatchStaticSQL(list, jdbcName);
    }

    /**
     * 新增记录，批量数据
     *
     * @param table  表名
     * @param jaList 数据 JSONArray
     */
    public static void insertJaListWithTableName(String table, JSONArray jaList) {
        insertJaListWithTableName(table, jaList, propertyName);
    }

    public static void insertJaListWithTableName(String table, JSONArray jaList, String jdbcName) {
        if (jaList.isEmpty()) {
            return;
        }

        List<XqlUtil> list = new ArrayList<>();

        for (Object obj : jaList) {
            JSONObject joInfo = (JSONObject) obj;

            XqlUtil xql = new XqlUtil();
            xql.setTable(table);
            xql.setValue(joInfo);

            list.add(xql);
        }

        insertList(list, jdbcName);
    }
}
