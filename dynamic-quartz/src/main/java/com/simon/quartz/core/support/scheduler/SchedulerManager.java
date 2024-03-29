package com.simon.quartz.core.support.scheduler;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.simon.quartz.core.exception.BusinessException;
import com.simon.quartz.core.support.scheduler.job.BaseJob;
import com.simon.quartz.core.support.scheduler.job.StatefulJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.InitializingBean;

import com.simon.quartz.core.support.scheduler.TaskScheduled.TaskType;
import com.simon.quartz.core.support.scheduler.TaskScheduled.JobType;
/**
 * 默认的定时任务管理器
 * 
 * @author ShenHuaJie
 * @version 2016年5月27日 上午10:28:26
 */
@Slf4j
public class SchedulerManager implements InitializingBean {


    private Scheduler scheduler;

    private List<JobListener> jobListeners;

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setJobListeners(List<JobListener> jobListeners) {
        this.jobListeners = jobListeners;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.jobListeners != null && this.jobListeners.size() > 0) {
            log.debug("Initing task scheduler[" + this.scheduler.getSchedulerName() + "] , add listener size ："
                + this.jobListeners.size());
            for (JobListener listener : this.jobListeners) {
                log.debug("Add JobListener : " + listener.getName());
                this.scheduler.getListenerManager().addJobListener(listener);
            }
        }
    }

    public List<TaskScheduled> getAllJobDetail() {
        List<TaskScheduled> result = new LinkedList<TaskScheduled>();
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.jobGroupContains("");
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
            for (JobKey jobKey : jobKeys) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    TaskScheduled job = new TaskScheduled();
                    job.setTaskName(jobKey.getName());
                    job.setTaskGroup(jobKey.getGroup());
                    TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    job.setStatus(triggerState.name());
                    if (trigger instanceof CronTrigger) {
                        CronTrigger cronTrigger = (CronTrigger)trigger;
                        String cronExpression = cronTrigger.getCronExpression();
                        job.setTaskCron(cronExpression);
                    }
                    job.setPreviousFireTime(trigger.getPreviousFireTime());
                    job.setNextFireTime(trigger.getNextFireTime());
                    JobDataMap jobDataMap = trigger.getJobDataMap();
                    job.setTaskType(jobDataMap.getString("taskType"));
                    job.setTargetSystem(jobDataMap.getString("targetSystem"));
                    job.setTargetObject(jobDataMap.getString("targetObject"));
                    job.setTargetMethod(jobDataMap.getString("targetMethod"));
                    job.setContactName(jobDataMap.getString("contactName"));
                    job.setContactEmail(jobDataMap.getString("contactEmail"));
                    job.setTaskDesc(jobDetail.getDescription());
                    String jobClass = jobDetail.getJobClass().getSimpleName();
                    if (jobClass.equals("StatefulJob")) {
                        job.setJobType("statefulJob");
                    } else if (jobClass.equals("DefaultJob")) {
                        job.setJobType("job");
                    }
                    result.add(job);
                }
            }
        } catch (Exception e) {
            log.error("Try to load All JobDetail cause error : ", e);
        }
        return result;
    }

    public JobDetail getJobDetailByTriggerName(Trigger trigger) {
        try {
            return this.scheduler.getJobDetail(trigger.getJobKey());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 新增job
     * @param taskScheduled
     * @return boolean
     */
    public boolean updateTask(TaskScheduled taskScheduled) {
        String jobGroup = taskScheduled.getTaskGroup();
        if (StringUtils.isBlank(jobGroup)) {
            jobGroup = "ds_job";
        }
        String jobName = taskScheduled.getTaskName();
        if (StringUtils.isBlank(jobName)) {
            jobName = String.valueOf(System.currentTimeMillis());
        }
        String cronExpression = taskScheduled.getTaskCron();
        String targetObject = taskScheduled.getTargetObject();
        String targetMethod = taskScheduled.getTargetMethod();
        String jobDescription = taskScheduled.getTaskDesc();
        String jobType = taskScheduled.getJobType();
        String taskType = taskScheduled.getTaskType();
        JobDataMap jobDataMap = new JobDataMap();
        if (TaskType.dubbo.equals(taskType)) {
            jobDataMap.put("targetSystem", taskScheduled.getTargetSystem());
        }
        jobDataMap.put("targetObject", targetObject);
        jobDataMap.put("targetMethod", targetMethod);
        jobDataMap.put("taskType", taskType);
        jobDataMap.put("contactName", taskScheduled.getContactName());
        jobDataMap.put("contactEmail", taskScheduled.getContactEmail());

        JobBuilder jobBuilder = null;
        if (JobType.job.equals(jobType)) {
            jobBuilder = JobBuilder.newJob(BaseJob.class);
        } else if (JobType.statefulJob.equals(jobType)) {
            jobBuilder = JobBuilder.newJob(StatefulJob.class);
        }
        if (jobBuilder != null) {
            JobDetail jobDetail = jobBuilder.withIdentity(jobName, jobGroup).withDescription(jobDescription)
                .storeDurably(true).usingJobData(jobDataMap).build();

            Trigger trigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .withIdentity(jobName, jobGroup).withDescription(jobDescription).forJob(jobDetail)
                .usingJobData(jobDataMap).build();

            try {
                JobDetail detail = scheduler.getJobDetail(new JobKey(jobName, jobGroup));
                if (detail == null) {
                    scheduler.scheduleJob(jobDetail, trigger);
                } else {
                    scheduler.addJob(jobDetail, true);
                    scheduler.rescheduleJob(new TriggerKey(jobName, jobGroup), trigger);
                }
                return true;
            } catch (SchedulerException e) {
                log.error("SchedulerException", e);
                throw new BusinessException(e);
            }
        }
        return false;
    }

    /**
     * 暂停所有触发器
     */
    public void pauseAllTrigger() {
        try {
            scheduler.standby();
        } catch (SchedulerException e) {
            log.error("SchedulerException", e);
            throw new BusinessException(e);
        }
    }

    /**
     * 启动所有触发器
     */
    public void startAllTrigger() {
        try {
            if (scheduler.isInStandbyMode()) {
                scheduler.start();
            }
        } catch (SchedulerException e) {
            log.error("SchedulerException", e);
            throw new BusinessException(e);
        }
    }

    // 暂停任务
    public void stopJob(TaskScheduled scheduleJob) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getTaskName(), scheduleJob.getTaskGroup());
            scheduler.pauseTrigger(triggerKey);
        } catch (Exception e) {
            log.error("Try to stop Job cause error : ", e);
            throw new BusinessException(e);
        }
    }

    // 启动任务
    public void resumeJob(TaskScheduled scheduleJob) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getTaskName(), scheduleJob.getTaskGroup());
            scheduler.resumeTrigger(triggerKey);
        } catch (Exception e) {
            log.error("Try to resume Job cause error : ", e);
            throw new BusinessException(e);
        }
    }

    // 执行任务
    public void runJob(TaskScheduled scheduleJob) {
        try {
            JobKey jobKey = JobKey.jobKey(scheduleJob.getTaskName(), scheduleJob.getTaskGroup());
            scheduler.triggerJob(jobKey);
        } catch (Exception e) {
            log.error("Try to resume Job cause error : ", e);
            throw new BusinessException(e);
        }
    }

    // 删除任务
    public void delJob(TaskScheduled scheduleJob) {
        try {
            JobKey jobKey = JobKey.jobKey(scheduleJob.getTaskName(), scheduleJob.getTaskGroup());
            TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getTaskName(), scheduleJob.getTaskGroup());
            scheduler.pauseTrigger(triggerKey);// 停止触发器
            scheduler.unscheduleJob(triggerKey);// 移除触发器
            scheduler.deleteJob(jobKey);// 删除任务
        } catch (Exception e) {
            log.error("Try to resume Job cause error : ", e);
            throw new BusinessException(e);
        }
    }
}
