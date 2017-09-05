package com.erlitech.ejava.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * sql处理类
 *
 * @author 孙振强
 * @since 2017-07-18
 */
public class XqlUtil {

    private String table;
    private String field;
    private JSONObject value;
    private String where;
    private String order;
    private String group;
    private String limit;

    //    public XqlUtil() {
//        // TODO Auto-generated constructor stub
//    }
    public String getTable() {
        return table;
    }

    /**
     * @param table the table to set
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * @param field the field to set
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * @return the value
     */
    public JSONObject getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(JSONObject value) {
        this.value = value;
    }

    /**
     * @return the where
     */
    public String getWhere() {
        return where;
    }

    /**
     * @param where the where to set
     */
    public void setWhere(String where) {
        this.where = where;
    }

    /**
     * @return the order
     */
    public String getOrder() {
        return order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(String order) {
        this.order = order;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * @return the limit
     */
    public String getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit(String limit) {
        this.limit = limit;
    }

    /**
     * @return sql语句
     */
    public String getSelectSql() {
        String sql = "";

        if (StringUtils.isEmpty(this.getTable())) {
            return sql;
        }

        if (StringUtils.isEmpty(this.getField())) {
            this.setField("id");
        }

        sql += "SELECT " + this.getField() + " FROM " + this.getTable();

        if (StringUtils.isNotEmpty(this.getWhere())) {
            sql += " WHERE " + this.getWhere();
        }

        if (StringUtils.isNotEmpty(this.getGroup())) {
            sql += " GROUP BY " + this.getGroup();
        }

        if (StringUtils.isNotEmpty(this.getOrder())) {
            sql += " ORDER BY " + this.getOrder();
        }

        if (StringUtils.isNotEmpty(this.getLimit())) {
            sql += " LIMIT " + this.getLimit();
        } else {
            sql += " LIMIT 0,1000";
        }

        return sql;
    }

    /**
     * @return sql语句
     */
    public String getDeleteSql() {
        String sql = "";

        if (StringUtils.isEmpty(this.getTable())) {
            return sql;
        }

        if (StringUtils.isEmpty(this.getWhere())) {
            return sql;
        }

        sql += "DELETE FROM " + this.getTable();

        if (StringUtils.isNotEmpty(this.getWhere())) {
            sql += " WHERE " + this.getWhere();
        }

        return sql;
    }

    /**
     * @return sql语句
     */
    public String getUpdateSql() {
        String sql = "";
        JSONObject joValue;
        List<String> listValue = new ArrayList<>();

        if (StringUtils.isEmpty(this.getTable())) {
//            $this->error('update: table is error.');
            return sql;
        }

        joValue = this.getValue();

        if (joValue.isEmpty()) {
            return sql;
        }

        sql += "UPDATE " + this.getTable();

        for (Map.Entry<String, Object> entry : joValue.entrySet()) {
            if (entry.getValue() == null) {
                listValue.add(entry.getKey() + " = null");
            } else {
                listValue.add(entry.getKey() + " = \"" + escapeSql(entry.getValue().toString()) + "\"");
            }
        }

        sql += " SET " + StringUtils.join(listValue.toArray(), ",");

        if (StringUtils.isNotEmpty(this.getWhere())) {
            sql += " WHERE " + this.getWhere();
        }

        return sql;
    }

    /**
     * @return sql语句
     */
    public String getInsertSql() {
        String sql = "";
        List<String> listField = new ArrayList<>();
        List<String> listValue = new ArrayList<>();
        JSONObject joValue;

        if (StringUtils.isEmpty(this.getTable())) {
            return sql;
        }

        joValue = this.getValue();

        if (joValue.isEmpty()) {
            return sql;
        }

        for (Map.Entry<String, Object> entry : joValue.entrySet()) {
            listField.add(entry.getKey());

            if (entry.getValue() == null) {
                listValue.add("null");
            } else {
                listValue.add("\"" + escapeSql(entry.getValue().toString()) + "\"");
            }
        }

        sql += "INSERT INTO " + this.getTable();
        sql += " (" + StringUtils.join(listField.toArray(), ",") + ")";
        sql += " VALUES (" + StringUtils.join(listValue.toArray(), ",") + ")";

        return sql;
    }

    /**
     * @param sql sql语句
     * @return 处理后的sql语句
     */
    public String escapeSql(String sql) {

        if (StringUtils.isNotEmpty(sql)) {
            sql = StringEscapeUtils.escapeSql(sql);  //lang3中不支持了
            sql = sql.replaceAll("\"","\\\\\"");
        }

        return sql;
    }
}
