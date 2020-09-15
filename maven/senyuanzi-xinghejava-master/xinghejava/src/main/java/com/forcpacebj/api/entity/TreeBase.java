/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */

package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 树形实体基类
 * Created by gelingfeng on 16/8/15.
 */
@Getter
@Setter
@NoArgsConstructor
public class TreeBase extends BaseEntity {

    private String id;

    private String name;

    private String parentId;

    public TreeBase(String id) {
        this.id = id;
    }
}
