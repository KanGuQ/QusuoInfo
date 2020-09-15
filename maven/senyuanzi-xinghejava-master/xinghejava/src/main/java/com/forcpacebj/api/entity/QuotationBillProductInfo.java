package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class QuotationBillProductInfo extends BaseEntity {

    private String billId;
    private int sheetId;
    private int sectionId;
    private int sortNumber;
    private ProductInfo product;
    private BigDecimal quantity;
    private BigDecimal purchCost;
    private BigDecimal salesAmount;

    /**
     * 是否自建产品
     * 自建产品进价等于售价
     */
    private Boolean isManual;
}
