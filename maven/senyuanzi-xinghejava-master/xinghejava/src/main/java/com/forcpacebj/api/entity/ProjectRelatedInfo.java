package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRelatedInfo extends BaseEntity {

    private ProjectInfo project;
    private PeopleInfo relatedPeople;
    private String relatedPeopleRole;
}
