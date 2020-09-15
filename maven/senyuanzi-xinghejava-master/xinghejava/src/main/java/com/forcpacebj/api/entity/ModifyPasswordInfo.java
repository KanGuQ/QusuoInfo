package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModifyPasswordInfo {

    private String userId;

    private String originPwd;

    private String newPwd;
}
