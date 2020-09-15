package com.forcpacebj.api.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ProductData extends BaseEntity {
    private Long id;
    private String productId;
    private String companyId;
    private Long downloadCount;
    private Long quotationCount;
    private Date createTime;
    private Date updateTime;
    private Date deleteTime;

    public ProductData(String productId, String companyId, Long downloadCount, Long quotationCount) {
        super();
        this.productId = productId;
        this.companyId = companyId;
        this.downloadCount = downloadCount;
        this.quotationCount = quotationCount;
    }

    private String brand;
    private String role;
    private String shortName;

}
