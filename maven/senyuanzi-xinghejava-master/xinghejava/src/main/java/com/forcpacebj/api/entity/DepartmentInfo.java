package com.forcpacebj.api.entity;

import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Created by pc on 2020/3/24.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentInfo extends BaseEntity {

    private Integer id;

    private String name;

    private String accountId;

    private UserInfo manager;

    private String power;

    private Integer sortNum;

    private List users;

    public JSONArray getPower() {
        return JSONArray.parseArray(power == null ? "[]" : power);
    }

    public DepartmentInfo(Integer id, String name, Object power) {
        this.id = id;
        this.name = name;
        this.power = power == null ? null : power.toString();
    }
}
