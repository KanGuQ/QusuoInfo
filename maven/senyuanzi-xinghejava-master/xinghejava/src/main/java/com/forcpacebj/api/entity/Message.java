package com.forcpacebj.api.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Message extends BaseEntity {
    private Integer id;
    private String targetAccountId;
    private String originAccountId;
    private String targetUserId;
    private String originUserId;
    private String message;
    private Integer type;
    private Date create_time;
    private Date update_time;
}
