package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by 树 on 2020/4/23.
 */
@Getter
@Setter
public class ProductShareInfo extends BaseEntity {

    private Integer id;

    private String accountId;

    private String productId;

    private BigDecimal salePrice;

    private Boolean isReceived;

    private String toAccountId;
}
