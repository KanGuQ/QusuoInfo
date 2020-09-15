package com.forcpacebj.api.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class WholesalerData extends BaseEntity {
    private Long id;
    private String companyId;
    private Long productDownloadCount;
    private Long productQuotationCount;
    private Date createTime;
    private Date updateTime;
    private Date deleteTime;

    private List<ProductData> productDataList;


}
