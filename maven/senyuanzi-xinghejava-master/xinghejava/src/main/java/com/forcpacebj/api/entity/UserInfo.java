package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserInfo extends BaseEntity {

    private String accountId;

    private DepartmentInfo department;

    private Integer departmentId;

    private String accountName;

    private String userId;

    private String userName;

    private UserRoleInfo userRole;

    private String userRoleEnum;

    private String email;

    private Boolean isAdmin;

    private Boolean multipleLogin;

    private String pwd;

    private String title;

    private String phoneNumber;

    private TokenInfo token;

    private Date lastLogin;

    private Date lastAccessed;

    private Integer recordCount;
    private Integer recordDateCount;

    private Integer currentDepartmentId;

}
