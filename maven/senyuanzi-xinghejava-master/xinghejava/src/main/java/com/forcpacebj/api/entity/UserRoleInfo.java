package com.forcpacebj.api.entity;

import com.alibaba.fastjson.JSONArray;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleInfo extends BaseEntity {

    private Integer roleId;

    private String roleName;

    private String power;

    public JSONArray getPower() {
        return JSONArray.parseArray(power);
    }

    public UserRoleInfo(Integer roleId, String roleName, Object power) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.power = power == null ? null : power.toString();
    }
}
