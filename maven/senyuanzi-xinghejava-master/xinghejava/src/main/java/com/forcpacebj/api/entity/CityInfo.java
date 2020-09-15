package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityInfo extends BaseEntity {

    private String provinceId;
    private String cityId;
    private String cityName;
}
