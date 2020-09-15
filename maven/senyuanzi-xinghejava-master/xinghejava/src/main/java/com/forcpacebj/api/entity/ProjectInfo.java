package com.forcpacebj.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ProjectInfo {

    private String projectId;

    private Integer departmentId;

    private String accountId;

    private String projectName;

    /**
     * 项目类型 0 业务型 | 1 非业务型
     */
    private int projectType;

    /**
     * 项目性质
     * "家庭项目",
     * "商业场所项目",
     * "政府招标项目",
     * "企业招标项目",
     * "外包项目",
     * "快销项目"
     */
    private String projectNature;

    private PeopleInfo owner;

    private List<QuotationBillInfo> quotationBillList;

    private UserInfo salesMan;

    private UserInfo designer;

    private UserInfo charger;

    private String projectSource;

    private ProvinceInfo province;

    private CityInfo city;

    private String area;

    private int projectProgress;

    private ProjectStageInfo stage;

    private ConstructStageInfo constructStage;

    private String projectStage;

    private String engineeringStage;

    private Date estimateContractDate;

    private double estimateContractMoney;

    private int dealRate;

    private double dealContractMoney;

    private Date contractDate;

    private double commision;

    private double otherCost;

    private double deviceCost;

    private double buildCost;

    private double totalCost;

    private String remark;

    private UserInfo createUser;
    private Date createDateTime;
    private UserInfo modifyUser;
    private Date modifyDateTime;
    private Boolean state;
    private Boolean isImportant;

    private List<ProjectCooperatorInfo> projectCooperatorList;

    private List<ProjectRelatedInfo> projectRelatedList;

    private List<ProjectUserInfo> projectUserList;

    private List<ProjectRecordInfo> projectRecordList;

    private String createUserName;


    private Date updateTime;

    private Integer installment;
    private Integer paidInstallment;

    private String contractFileUrl;
    private String ContractFileName;
}