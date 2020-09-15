package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuotationTemplateInfo extends BaseEntity {

    private String templateName;

    private String templateId;

    private String url;

    private int sortNumber;
}
