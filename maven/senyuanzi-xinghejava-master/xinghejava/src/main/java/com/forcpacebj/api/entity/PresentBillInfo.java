package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PresentBillInfo extends BaseEntity {

    private AccountInfo toAccount;

    private AccountInfo fromAccount;

    private String quotationBillId;

    private List<QuotationBillProductInfo> details;

    /**
     * 项目信息
     */
    private String title;

    /**
     * 项目城市
     */
    private String city;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 预计时间
     */
    private Date estimatedTime;

    /**
     * 备注
     */
    private String remark;

    private Date createTime;
}
