package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by pc on 2020/2/8.
 */
@Setter
@Getter
public class ProjectFileInfo {
    private Integer id;
    private String projectId;
    private String accountId;
    private String fileUrl;
    private String name;
    private Integer sortNumber;
    private Date updateTime;
    private Date createTime;
}
