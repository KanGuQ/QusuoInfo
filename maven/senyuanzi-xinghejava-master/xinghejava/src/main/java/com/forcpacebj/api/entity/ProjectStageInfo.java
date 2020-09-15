package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by pc on 2020/2/19.
 */
@Setter
@Getter
public class ProjectStageInfo {

    private Integer id;

    private String stageName;

    private Integer type;

    private String typeName;

    private Boolean isShow;

    private String accountId;

    private Integer sortNum;

    private Date updateTime;

    private Date createTime;

    private Integer count;
}
