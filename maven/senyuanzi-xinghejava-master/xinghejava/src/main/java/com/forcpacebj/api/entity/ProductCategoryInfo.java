package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductCategoryInfo extends TreeBase {

    private String accountId;

    private String relatedAccountId;

    private String RelatedAccountName;

    private int sortNumber;

    public ProductCategoryInfo(String id) {
        super(id);
    }
}
