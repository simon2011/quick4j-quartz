package com.simon.quartz.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.sql.Timestamp;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author simon.pei
 * @since 2019-08-05
 */
public class TaskFireLog extends Model<TaskFireLog> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id_", type = IdType.ID_WORKER)
    private Long id;

    @TableField("group_name")
    private String groupName;

    @TableField("task_name")
    private String taskName;

    @TableField("start_time")
    private Timestamp startTime;

    @TableField("end_time")
    private Timestamp endTime;

    @TableField("status_")
    private String status;

    /**
     * 服务器名
     */
    @TableField("server_host")
    private String serverHost;

    /**
     * 服务器网卡序列号
     */
    @TableField("server_duid")
    private String serverDuid;

    @TableField("fire_info")
    private String fireInfo;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getServerDuid() {
        return serverDuid;
    }

    public void setServerDuid(String serverDuid) {
        this.serverDuid = serverDuid;
    }

    public String getFireInfo() {
        return fireInfo;
    }

    public void setFireInfo(String fireInfo) {
        this.fireInfo = fireInfo;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "TaskFireLog{" +
        "id=" + id +
        ", groupName=" + groupName +
        ", taskName=" + taskName +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", status=" + status +
        ", serverHost=" + serverHost +
        ", serverDuid=" + serverDuid +
        ", fireInfo=" + fireInfo +
        "}";
    }
}
