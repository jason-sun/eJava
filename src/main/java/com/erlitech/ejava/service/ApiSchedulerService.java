package com.erlitech.ejava.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erlitech.ejava.controller.ApiController;
import com.erlitech.ejava.utils.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

/**
 * 计划任务
 *
 * @author 孙振强
 */
public class ApiSchedulerService {

    private static Logger logger = XLoggerUtil.getLogger(ApiSchedulerService.class.getName());

    private static String SCHEDULER_TABLE = XPropertyUtil.getProperty("schedulerTable", "controller");

    // 初始化计划任务
    public static void init() {
        if (null == SCHEDULER_TABLE || "".equals(SCHEDULER_TABLE)) {
            SCHEDULER_TABLE = "sys_scheduler";
        }

        // run in a second
        final long timeInterval = 10000;
        Runnable runnable = new Runnable() {
            public void run() {
                while (true) {
                    // ------- code for task to run
                    logger.info("ApiSchedulerService");
                    getSchedulerTask();
                    // ------- ends here

                    try {
                        Thread.sleep(timeInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * 从数据表获取当前要执行的计划任务
     */
    public static void getSchedulerTask() {
        XqlUtil xql = new XqlUtil();

        xql.setTable(SCHEDULER_TABLE);
        xql.setField("*");
        xql.setWhere("isDeleted = 0" +
                " AND ( UNIX_TIMESTAMP( ) - UNIX_TIMESTAMP( lastRunTime ) - `interval` ) > -1" +
                " AND MOD ( ( UNIX_TIMESTAMP( ) - UNIX_TIMESTAMP( startTime ) ), `interval` ) < 10");

        JSONArray jaList = XdbUtil.readList(xql);

        if (jaList.isEmpty()) {
            return;
        }

        logger.info(jaList.toJSONString());

        for (int j = 0; j < jaList.size(); j++) {
            JSONObject joTask = jaList.getJSONObject(j);
            runSchedulerTask(joTask);
        }
    }

    /**
     * 更新计划任务记录
     *
     * @param joTask Task
     */
    public static void runSchedulerTask(JSONObject joTask) {
        JSONObject joValue = new JSONObject();

        joValue.put("lastRunTime", XDateUtil.getDateTime());

        XqlUtil xql = new XqlUtil();

        xql.setTable(SCHEDULER_TABLE);
        xql.setValue(joValue);
        xql.setWhere("id='" + joTask.getString("id") + "'");

        XdbUtil.update(xql);

        HttpServletRequest request = null;
        HttpServletResponse response = null;
        String outData = ApiController.invokeApi(joTask.getString("input"), request, response);

        joValue.put("output", outData);
        xql.setValue(joValue);

        XdbUtil.update(xql);
    }
}
