/**
 *
 */
package com.simon.quartz.core.support.scheduler.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import com.simon.quartz.core.support.scheduler.TaskScheduled.TaskType;
import com.simon.quartz.core.support.scheduler.TaskScheduled.JobType;

/**
 * 默认调度(非阻塞)
 *
 * @author ShenHuaJie
 * @version 2016年12月29日 上午11:52:32
 */
@Slf4j
public class BaseJob implements Job {


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long start = System.currentTimeMillis();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String taskType = jobDataMap.getString("taskType");
        String targetObject = jobDataMap.getString("targetObject");
        String targetMethod = jobDataMap.getString("targetMethod");
        String key = targetMethod + "." + targetObject;
        try {
            log.info("定时任务[{}.{}]开始", targetObject, targetMethod);
            //String requestId = Sequence.next().toString();
            if (true) {
                try {
                    ApplicationContext applicationContext = (ApplicationContext) context.getScheduler().getContext()
                            .get("applicationContext");
                    if (TaskType.local.equals(taskType)) {
                        Object refer = applicationContext.getBean(targetObject);
                        refer.getClass().getDeclaredMethod(targetMethod).invoke(refer);
                    } else if (TaskType.dubbo.equals(taskType)) {
                        if (StringUtils.isEmpty(jobDataMap.getString("targetSystem"))) {
                            Object refer = applicationContext.getBean(targetObject);
                            refer.getClass().getDeclaredMethod(targetMethod).invoke(refer);
                        } else {
                           /* BaseProvider provider = (BaseProvider)applicationContext
                                    .getBean(jobDataMap.getString("targetSystem"));
                            provider.execute(new Parameter(targetObject, targetMethod));*/
                        }
                    } else {
                        Object refer = applicationContext.getBean(targetObject);
                        refer.getClass().getDeclaredMethod(targetMethod).invoke(refer);
                    }
                    Double time = (System.currentTimeMillis() - start) / 1000.0;
                    log.info("定时任务[{}.{}]用时：{}s", targetObject, targetMethod, time.toString());
                } finally {
                    //unLock(key, requestId);
                }
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }


}
