package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PeopleRelatedInfo extends BaseEntity {

    private PeopleInfo relatedPeople;
    private String relatedPeopleRole;
}
