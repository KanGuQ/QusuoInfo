package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by pc on 2019/10/9.
 */
@Getter
@Setter
public class CatalogParameterInfo {

    private Integer catalogId;

    private String parameterName;

    private Integer parameterType;

    private String parameterOptions;

    private Integer sortNum;
}
