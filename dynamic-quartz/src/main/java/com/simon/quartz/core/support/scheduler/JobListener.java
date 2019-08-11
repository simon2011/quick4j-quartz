package com.simon.quartz.core.support.scheduler;

import java.sql.Timestamp;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.simon.quartz.core.constant.Constants;
import com.simon.quartz.core.constant.Constants.JOBSTATE;
import com.simon.quartz.core.utils.NativeUtil;
import com.simon.quartz.model.TaskFireLog;
import lombok.extern.slf4j.Slf4j;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;


/**
 * 调度监听器
 *
 * @author ShenHuaJie
 * @version 2016年5月27日 下午4:31:31
 */
@Slf4j()
public class JobListener implements org.quartz.JobListener {

    @Lazy
    @Autowired
    private SchedulerServiceImpl schedulerService;

    // 线程池
    private static ExecutorService executorService = new ThreadPoolExecutor(2, 20, 5, TimeUnit.SECONDS,
        new ArrayBlockingQueue<Runnable>(20), new DiscardOldestPolicy());
    private static String JOB_LOG = "jobLog";

    /*public void setEmailQueueSender(QueueSender emailQueueSender) {
        this.emailQueueSender = emailQueueSender;
    }*/

    @Override
    public String getName() {
        return "taskListener";
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
    }

    // 任务开始前
    @Override
    public void jobToBeExecuted(final JobExecutionContext context) {
        final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String targetObject = jobDataMap.getString("targetObject");
        String targetMethod = jobDataMap.getString("targetMethod");
        if (log.isInfoEnabled()) {
            log.info("定时任务开始执行：{}.{}", targetObject, targetMethod);
        }
        // 保存日志
        TaskFireLog log = new TaskFireLog();
        log.setStartTime(new Timestamp(context.getFireTime().getTime()));
        log.setGroupName(targetObject);
        log.setTaskName(targetMethod);
        log.setStatus(JOBSTATE.INIT_STATS);
        log.setServerHost(NativeUtil.getHostName());
        log.setServerDuid(NativeUtil.getDUID());
        schedulerService.updateLog(log);
        jobDataMap.put(JOB_LOG, log);
    }

    // 任务结束后
    @Override
    public void jobWasExecuted(final JobExecutionContext context, JobExecutionException exp) {
        Timestamp end = new Timestamp(System.currentTimeMillis());
        final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String targetObject = jobDataMap.getString("targetObject");
        String targetMethod = jobDataMap.getString("targetMethod");
        if (log.isInfoEnabled()) {
            log.info("定时任务执行结束：{}.{}", targetObject, targetMethod);
        }
        // 更新任务执行状态

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (log != null) {
                    try {
                        //schedulerService.updateLog(log);
                    } catch (Exception e) {
                        log.error("Update TaskRunLog cause error. The log object is : " + JSON.toJSONString(log), e);
                    }
                }
            }
        });
    }

}
