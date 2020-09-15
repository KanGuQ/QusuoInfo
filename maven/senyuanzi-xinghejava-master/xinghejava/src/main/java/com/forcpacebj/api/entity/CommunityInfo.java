package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * {@link CommunityInfo}
 * Author: ACL
 * Date:2020/02/22
 * Description: 小区信息表
 * Created by ACL on 2020/02/22.
 */
@Getter
@Setter
public class CommunityInfo extends BaseEntity{

    private String id; //区域id

    private String accountId; //  账号(租户id)

    private  String name;//小区名称

//    private String provinceId; // 省份id

    private String provinceName; //省份

    private String cityName; //城市

    private String countyName; //区县

//    private String cityId; //城市id

//    private String countyId; //区县 id

    private String address; // 详细地址

    private Double lat,lng;// 经纬度


    private String remarks; //备注信息

    private String filePath; // 图片存储路径

    private String pictureUrl; // 图片访问路径


    private UserInfo createUser; // 创建用户
//
//    private String createUserId; //创建用户
//
//    private String createUserName;//创建用户名

    private UserInfo charger; //内部负责人

    private List<CommunityVisibleUser> communitiesVisibleUsers; //小区对应可见用户

    private List<CommunityProjectRelatedInfo> communityProjectRelatedInfos=new ArrayList<>(  ); //关联的项目信息


    private List<CommunityPeopleRelatedInfo> communityPeopleRelatedInfos=new ArrayList<>(  ); //干系人管理

    private Date createDate; //创建时间

    private Date lastUpdateDate; //最后更新时间
}
