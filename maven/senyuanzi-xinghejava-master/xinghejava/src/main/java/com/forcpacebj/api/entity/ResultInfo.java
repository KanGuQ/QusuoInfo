/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */

package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultInfo {

    /**
     * 是否删除成功
     */
    private boolean success;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 上传访问地址
     */
    private String url;

    /**
     * 原文件名
     */
    private String originFileName;

    /**
     * 扩展名
     */
    private String extension;

    /**
     * 文件大小
     */
    private long size;

}
