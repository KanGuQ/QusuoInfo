package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by pc on 2019/11/14.
 */
@Getter
@Setter
public class ProductRecordInfo {

    private Integer id;

    private UserInfo user;

    private String fromAccountId;

    private String fromAccountName;

    private AccountInfo account;

    private String fromProductId;

    private ProductInfo product;

    private Integer type;

    private Integer auditStatus;

    private String ApproverId;

    private String ApproverName;

    private Date updateTime;

    private Date createTime;

    public ProductRecordInfo() {
    }

    public ProductRecordInfo(String userId, String fromAccountId, String accountId, String fromProductId, ProductInfo product, Integer type, Integer auditStatus, String approverId, String approverName) {
        UserInfo user = new UserInfo();
        user.setUserId(userId);
        this.user = user;
        this.fromAccountId = fromAccountId;

        AccountInfo account = new AccountInfo();
        account.setAccountId(accountId);
        this.account = account;

        this.fromProductId = fromProductId;
        this.product = product;
        this.type = type;
        this.auditStatus = auditStatus;
        this.ApproverId = approverId;
        this.ApproverName = approverName;
    }
}
