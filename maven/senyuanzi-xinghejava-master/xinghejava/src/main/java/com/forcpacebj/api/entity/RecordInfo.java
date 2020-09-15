package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class RecordInfo extends BaseEntity {

    private String recordId;

    private String accountId;

    private Integer departmentId;

    /**
     * 日志内容
     */
    private String recordContent;

    /**
     * 日志日期
     */
    private Date recordDate;

    /**
     * 下一步日期
     */
    private Date nextDateTime;

    /**
     * 下一步性质
     * "普通",
     * "设计",
     * "工程"
     */
    private String nextNature;

    /**
     * 是否对工程师可见
     * 如果下一步性质是工程，控制项目是否对工程师可见
     */
    private Boolean toEngineer;

    /**
     * 本次花费
     */
    private double cost;

    /**
     * 花费时间
     */
    private double timeCost;

    /**
     * 记录人
     */
    private UserInfo user;

    /**
     * 前一个日志ID
     */
    private String preRecordId;

    /**
     * 日志状态
     */
    private String recordState;

    /**
     * 只允许修改三天内的日志
     */
    private Boolean isAllowUpdate;

    private int msgCount;

    /**
     * 图片
     */
    private List<RecordPictureInfo> recordPictureList;

    /**
     * 人
     */
    private List<PeopleRecordInfo> peopleRecordList;

    /**
     * 项目
     */
    private List<ProjectRecordInfo> projectRecordList;

    /**
     * 评论
     */
    private List<RecordMsgInfo> recordMsgList;

    /**
     * 日志类型
     */
    private String recordType;

    private Date updateTime;
}
