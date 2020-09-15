package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RolePurchPriceInfo extends BaseEntity {

    private UserRoleInfo userRole;

    private BigDecimal purchPrice;
}
