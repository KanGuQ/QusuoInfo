package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AccountInfo extends BaseEntity {

    private String accountId;

    private String accountName;

    private String inviteCode;

    private Integer userLimit;
    private Integer productLimit;
    private Integer projectLimit;
    private Integer userNum;

    private Integer isRegister;
    private Integer is_register;

    private String userId;
    private String userName;
    private String user_id;

    private String createUserPosition;
    private String telephone;
    private String remark;

    private String cityId;
    private Integer city_id;

    private String provinceId; //省份id
    private String countyId; // 区域id

    private String adCode; //高德区域码


    private Integer zoom;//地图缩放等级

    private Double lng, lat;//经纬度
    private String address;

    private String industry; //行业
    private String mainBrand;   //主营品牌

    private String projectType;
    private String project_type;

    private String employeesNum;
    private String employees_num;

    private String averageProjectSize;
    private String average_project_size;

    private String averageProjectNumber;
    private String average_project_number;

    private Date createTime;
    private Date create_time;
    private Date update_time;

    private Date effectiveDate;
    private Date expireDate;

    private Integer productCount;
    private Integer selfBuiltCount;
    private Integer publicCount;
    private Integer unPublicCount;
    private Integer downloadedCount;
    private Integer unDownloadedCount;


    private String courtyard;
    private String communityId;
    private String CountyId;
    private String status;

    private Boolean isPaid;
    private Boolean isAuth;
    private Integer starNumber;


    private Boolean isCooperative;
}
