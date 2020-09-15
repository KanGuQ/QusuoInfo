package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 日志评论类
 */
@Getter
@Setter
public class RecordMsgInfo extends BaseEntity {

    private int msgId;
    private String postContent;
    private RecordInfo record;
    private UserInfo postUser;
    private Date postTime;
}
