package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link County}
 * Author: ACL
 * Date:2020/02/18
 * Description:
 * Created by ACL on 2020/02/18.
 */
@Getter
@Setter
public class County extends BaseEntity  {

    private String countyId;
    private String countyName;
    private String cityId;
    private String countySort;
}
