package com.simon.quartz.core.support.scheduler;

import java.util.List;
import java.util.Map;

import com.simon.quartz.mapper.TaskFireLogMapper;
import com.simon.quartz.model.TaskFireLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 * @author ShenHuaJie
 * @version 2016年7月1日 上午11:34:59
 */
@Service
public class SchedulerServiceImpl implements ApplicationContextAware {

    @Autowired
    private TaskFireLogMapper logMapper;

    @Lazy
    @Autowired
    private SchedulerManager schedulerManager;

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // 获取所有作业
    public List<TaskScheduled> getAllTaskDetail() {
        return schedulerManager.getAllJobDetail();
    }

    // 执行作业
    public void execTask(TaskScheduled taskScheduler) {
        schedulerManager.runJob(taskScheduler);
    }

    // 恢复作业
    public void openTask(TaskScheduled taskScheduled) {
        schedulerManager.resumeJob(taskScheduled);
    }

    // 暂停作业
    public void closeTask(TaskScheduled taskScheduled) {
        schedulerManager.stopJob(taskScheduled);
    }

    // 删除作业
    public void delTask(TaskScheduled taskScheduled) {
        schedulerManager.delJob(taskScheduled);
    }

    // 修改任务
    public void updateTask(TaskScheduled taskScheduled) {
        schedulerManager.updateTask(taskScheduled);
    }

    @Transactional
    public TaskFireLog updateLog(TaskFireLog record) {
        if (record.getId() == null) {
            logMapper.insert(record);
        } else {
            logMapper.updateById(record);
        }
        return record;
    }
}
