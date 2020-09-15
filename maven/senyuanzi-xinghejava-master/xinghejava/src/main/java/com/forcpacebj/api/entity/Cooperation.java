package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Cooperation extends BaseEntity {

    private String companyName;
    private String projectName;
    private Integer stageType;
    private String projectId;
    private String id;

}
