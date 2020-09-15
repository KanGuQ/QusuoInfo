package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link CommunityPeopleRelatedInfo}
 * Author: ACL
 * Date:2020/03/08
 * Description:
 * Created by ACL on 2020/03/08.
 */
@Getter
@Setter
public class CommunityPeopleRelatedInfo extends BaseEntity {

    private PeopleInfo relatedPeople; //干系人

    //关系
    private String relatedPeopleRole;
}
