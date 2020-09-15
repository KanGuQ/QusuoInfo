package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSetDetailInfo {

    private String ProductSetId;

    private int sectionId;

    private int sortNumber;

    private ProductInfo product;

    private int quantity;
}
