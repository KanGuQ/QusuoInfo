package com.forcpacebj.api.entity;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class ConstructStageInfo {

  private Integer id;
  private String accountId;
  private String name;
  private Integer sortNum;
  private Date createTime;
  private Date updateTime;

}
