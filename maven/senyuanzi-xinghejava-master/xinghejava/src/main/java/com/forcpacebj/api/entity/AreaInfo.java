package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link AreaInfo}
 * Author: ACL
 * Date:2020/02/26
 * Description:区域
 * Created by ACL on 2020/02/26.
 */
@Getter
@Setter
public class AreaInfo {

    private Integer id;

    private String name;
    private String parent_id;
    private String short_name;
    private int level_type;
    private String zip_code;
    private String city_code;
    private String tree_names;
    private double lng,lat;
    private String pin_yin;
}
