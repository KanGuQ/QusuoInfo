package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class QuotationBillInfo extends BaseEntity {

    private String billId;

    private String accountId;

    private Integer departmentId;

    /**
     * 报价单标题  1
     */
    private String billName;

    /**
     * 报价单号
     */
    private String quotationCode;

    /**
     * 报价单模版
     */
    private QuotationTemplateInfo template;

    /**
     * 报价单查看地址
     */
    private String url;

    private ProjectInfo project;

    /**
     * 报价人
     */
    private UserInfo user;

    /**
     * 报价日期
     */
    private Date quotationDate;

    /**
     * 更新时间
     */
    private Date updateTime;

    private int taxRate;
    private float fee;
    private float bonus;
    private float laborCost;
    private float otherCost;
    private float quotation;

    private BigDecimal totalProductAmount;
    private BigDecimal totalServiceAmount;
    private BigDecimal totalTax;
    private BigDecimal totalAmount;

    private Boolean isShowTotalOffer;

    private List<QuotationBillSheetInfo> sheetList;

    private Long quotationBookId;//报价书模板Id
}
