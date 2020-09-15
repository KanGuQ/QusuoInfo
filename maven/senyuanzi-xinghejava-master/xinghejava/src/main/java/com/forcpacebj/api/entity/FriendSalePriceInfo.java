package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FriendSalePriceInfo extends BaseEntity {

    private Integer id;

    private FriendGroupInfo friendGroup;

    private String productId;

    private BigDecimal salePrice;
}
