package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductTagInfo extends BaseEntity {

    private static final long serialVersionUID = -2351278239267100594L;

    private int id;

    private String tagName;

    private float sortNumber;
}
