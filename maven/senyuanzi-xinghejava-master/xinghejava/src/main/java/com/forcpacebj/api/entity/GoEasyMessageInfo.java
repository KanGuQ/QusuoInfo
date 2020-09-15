package com.forcpacebj.api.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by pc on 2020/4/26.
 */
@Data
@NoArgsConstructor
public class GoEasyMessageInfo {

    private Integer id;

    private String channel;

    private String content;

    private Boolean isPush;

    private Boolean isRead;

    private Date createTime;

    private Date updateTime;

    public GoEasyMessageInfo(String channel, String content, Boolean isPush) {
        this.channel = channel;
        this.content = content;
        this.isPush = isPush;
    }
}
