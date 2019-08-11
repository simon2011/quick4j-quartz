package com.simon.quartz.Job;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @ClassName ScheduleJob
 * @Description TODO
 * @Author simon.pei
 * @Date 2019/7/14 23:25
 * @Version 1.0
 **/
@Component
public class ScheduleJob {

    @Autowired
    private Scheduler scheduler;
    @Bean
    public void mytaskSchedule()throws SchedulerException{
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("targetObject", "com.simon.quartz.job.MyTask");
        jobDataMap.put("targetMethod", "executeInternal");
        JobDetail jobDetail = JobBuilder.newJob(MyTask.class).withDescription("myTask")
                .storeDurably(true).usingJobData(jobDataMap).build();

        Trigger trigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule("0/3 * * * * ?"))
                .withDescription("TEST").forJob(jobDetail).build();

        scheduler.scheduleJob(jobDetail, trigger);
    }
}
