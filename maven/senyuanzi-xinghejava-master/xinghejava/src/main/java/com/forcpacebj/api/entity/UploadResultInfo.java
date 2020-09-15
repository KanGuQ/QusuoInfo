package com.forcpacebj.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadResultInfo extends BaseEntity {

    private boolean success;

    private String name;

    private String url;

    private String md5;
}
