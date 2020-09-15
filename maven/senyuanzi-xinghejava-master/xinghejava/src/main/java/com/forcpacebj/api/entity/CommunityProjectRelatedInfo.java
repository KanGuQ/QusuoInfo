package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link CommunityProjectRelatedInfo}
 * Author: ACL
 * Date:2020/03/07
 * Description: 小区与项目直接的关联
 * Created by ACL on 2020/03/07.
 */
@Getter
@Setter
public class CommunityProjectRelatedInfo extends BaseEntity{

    private ProjectInfo project;
    /**
     * 项目状态  α 机会  β 在建  γ 售后
     */
    private String status;


}
