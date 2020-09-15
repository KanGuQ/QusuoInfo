/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */

package com.forcpacebj.api.entity;

import lombok.*;

import java.math.BigDecimal;

/**
 * 查询结果项
 * Created by gelingfeng on 16/9/19.
 */
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class SmartLookupItemInfo extends BaseEntity {

    /**
     * 实际值
     */
    @NonNull
    private String value;

    /**
     * 显示值
     */
    @NonNull
    private String display;


    //额外属性，用于取数的时候放回额外的属性，比如联想会员的同时返回 手机号码，邮箱等字段
    private String prop1;
    private String prop2;
    private String prop3;
    private String prop4;
    private String prop5;
    private String prop6;
    private String prop7;
    private String prop8;
    private String prop9;
    private String prop10;

    private BigDecimal prop1number;
    private BigDecimal prop2number;
    private BigDecimal prop3number;
    private BigDecimal prop4number;
    private BigDecimal prop5number;

    //额外联想弹出下拉显示属性，默认只显示display属性
    private String popupDisplay1;
    private String popupDisplay2;
    private String popupDisplay3;
}
