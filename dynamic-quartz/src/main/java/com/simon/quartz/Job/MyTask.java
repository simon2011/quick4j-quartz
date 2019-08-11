package com.simon.quartz.Job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @ClassName MyTask
 * @Description TODO
 * @Author simon.pei
 * @Date 2019/7/14 23:22
 * @Version 1.0
 **/
public class MyTask extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("定时任务测试");
    }
}
