package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PeopleInfo extends BaseEntity {

    private String peopleId;
    private Integer departmentId;
    private String accountId;
    private String peopleName;

    /**
     * 姓名后缀
     */
    private String peopleSuffix;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 微信号
     */
    private String wechatId;

    /**
     * 性别
     */
    private String gender;

    /**
     * 年龄段
     */
    private String ageAlias;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 负责人
     */
    private UserInfo charger;

    /**
     * 婚姻状况
     */
    private String maritalStatus;

    /**
     * 子女状况
     */
    private String childrenStatus;

    /**
     * 公司单位
     */
    private String unit;

    /**
     * 成本
     */
    private double cost;

    /**
     * 类型
     */
    private String peopleRole;

    /**
     * 与负责人关系
     */
    private String relationWithCharger;

    /**
     * 备注
     */
    private String remark;

    /**
     * 住址
     */
    private String address;

    /**
     * 第一次接触方式
     */
    private String firstContact;

    private UserInfo createUser;
    private Date createDateTime;
    private Boolean state;

    /**
     * 最后跟进人
     */
    private UserInfo user;

    /**
     * 干系人
     */
    private List<PeopleRelatedInfo> peopleRelatedList;

    /**
     * 日志
     */
    private List<PeopleRecordInfo> peopleRecordList;

    /**
     * 人员参与的项目
     */
    private List<ProjectRelatedInfo> projectRelatedList;

    private Date updateTime;
}
