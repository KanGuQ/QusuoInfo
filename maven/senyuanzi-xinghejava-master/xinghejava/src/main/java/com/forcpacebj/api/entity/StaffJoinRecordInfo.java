package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by pc on 2019/9/25.
 */
@Getter
@Setter
public class StaffJoinRecordInfo extends BaseEntity {

    private Integer id;

    private String userId;

    private String userName;

    private Integer departmentId;

    private String accountId;

    private Integer state;

    private Integer code;

    private Date updateTime;

    private Date createTime;

}
