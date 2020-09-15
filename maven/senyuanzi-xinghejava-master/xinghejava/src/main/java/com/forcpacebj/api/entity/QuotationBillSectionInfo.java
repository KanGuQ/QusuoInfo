package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class QuotationBillSectionInfo extends BaseEntity {

    private String billId;
    private int sheetId;
    private int sectionId;
    private String sectionName;
    private BigDecimal totalAmount;

    private List<QuotationBillProductInfo> productList;
}
