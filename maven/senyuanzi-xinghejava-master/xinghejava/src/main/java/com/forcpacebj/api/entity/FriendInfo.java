package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class FriendInfo extends BaseEntity {

    private String accountId;

    private AccountInfo friendAccount;

    private Date createTime;

    private Boolean isAcceptable;
    private Boolean isAuth;

    private Boolean isAccepted;

    private Date acceptTime;

    private FriendGroupInfo friendGroup;

    private String remark;
}
