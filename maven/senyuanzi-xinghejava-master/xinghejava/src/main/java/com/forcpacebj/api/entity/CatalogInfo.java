package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by pc on 2019/10/9.
 */
@Getter
@Setter
public class CatalogInfo extends TreeBase {

    private String parentName;

    private List<CatalogParameterInfo> parameters;

    private Integer sortNum;

}
