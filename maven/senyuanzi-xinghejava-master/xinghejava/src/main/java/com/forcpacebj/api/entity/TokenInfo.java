/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */

package com.forcpacebj.api.entity;

import lombok.*;

import java.util.Date;

/**
 * 登录Token
 * Created by gelingfeng on 2016/11/1.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo extends BaseEntity {

    private String key;

    private Date grantTime;
}
