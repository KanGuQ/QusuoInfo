package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class QuotationBillSheetInfo extends BaseEntity {

    private String billId;
    private int sheetId;
    private String sheetName;
    private BigDecimal productTotalAmount;
    private int serviceAmountRate;
    private BigDecimal serviceAmount;
    private BigDecimal taxRate;
    private BigDecimal tax;
    private Boolean isShowOfferAmount;
    private BigDecimal offerAmount;
    private BigDecimal totalAmount;

    private BigDecimal designPrice;
    private BigDecimal softwarePrice;
    private BigDecimal mediaPrice;
    private BigDecimal installationPrice;
    private BigDecimal remotePrice;
    private BigDecimal projectPrice;

    private List<QuotationBillSectionInfo> sectionList;
}
