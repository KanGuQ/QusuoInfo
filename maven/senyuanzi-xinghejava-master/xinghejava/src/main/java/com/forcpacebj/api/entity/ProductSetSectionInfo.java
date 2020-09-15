package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductSetSectionInfo extends BaseEntity {

    private String ProductSetId;

    private int sectionId;

    private String sectionName;

    private List<ProductSetDetailInfo> productDetails;
}
