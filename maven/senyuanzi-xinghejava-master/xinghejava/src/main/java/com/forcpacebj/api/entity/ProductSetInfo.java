package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductSetInfo {

    private String id;

    private String name;

    private Boolean isShow;

    private ProductSetCategoryInfo category;

    private String pictureUrl;

    private List<ProductSetSectionInfo> sectionList;
}
