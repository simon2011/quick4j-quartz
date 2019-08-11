package com.simon.quartz.core.config;

import java.io.IOException;
import java.util.List;

import javax.sql.DataSource;

import com.google.common.collect.Lists;
import com.simon.quartz.core.support.scheduler.JobListener;
import com.simon.quartz.core.support.scheduler.SchedulerManager;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;


@Configuration
@ConditionalOnClass(org.quartz.JobListener.class)
public class SchedulerConfig {
    @Autowired
    DataSource dataSource;

    @Bean
    public SchedulerFactoryBean schedulerFactory() {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setSchedulerName("iBase4J-Scheduler");
        schedulerFactory.setApplicationContextSchedulerContextKey("applicationContext");
        schedulerFactory.setDataSource(dataSource);
        Resource resouce = new DefaultResourceLoader().getResource("classpath:quartz.properties");
        schedulerFactory.setConfigLocation(resouce);
        return schedulerFactory;
    }

    @Bean
    public SchedulerManager schedulerManager()throws IOException {
        SchedulerManager schedulerManager = new SchedulerManager();
        schedulerManager.setScheduler(scheduler());
        List<org.quartz.JobListener> jobListeners = Lists.newArrayList();
        jobListeners.add(jobListener());
        schedulerManager.setJobListeners(jobListeners);
        return schedulerManager;
    }

    @Bean
    public JobListener jobListener() {
        return new JobListener();
    }

    /*
     * 通过SchedulerFactoryBean获取Scheduler的实例
     */
    @Bean
    public Scheduler scheduler() throws IOException {
        return schedulerFactory().getScheduler();
    }

}
