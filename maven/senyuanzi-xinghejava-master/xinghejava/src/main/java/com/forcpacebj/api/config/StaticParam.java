package com.forcpacebj.api.config;

/**
 * Created by pc on 2019/8/20.
 */
public class StaticParam {
    /**
     * 手机验证部分配置
     */
    // 设置超时时间-可自行调整
    public final static String defaultConnectTimeout = "sun.net.client.defaultConnectTimeout";
    public final static String defaultReadTimeout = "sun.net.client.defaultReadTimeout";
    public final static String Timeout = "10000";
    // 初始化ascClient需要的几个参数
    public final static String product = "Dysmsapi";// 短信API产品名称（短信产品名固定，无需修改）
    public final static String domain = "dysmsapi.aliyuncs.com";// 短信API产品域名（接口地址固定，无需修改）
    // AK (产品密)
    public final static String accessKeyId = "LTAIQstYTl6Rw1oa";// accessKeyId,上文配置所得  自行配置
    public final static String accessKeySecret = "FINK1BQAxftko9v05YHRK7suewPcng";// accessKeySecret,上文配置所得
    // 必填:短信签名-可在短信控制台中找到
    public final static String SIGN_NAME = "星合";     // 阿里云配置短信签名填入
    // 必填:短信模板-可在短信控制台中找到
    // 阿里云配置短信模板
    public final static String VERIFICATION_CODE = "SMS_172565079"; //注册验证码
    public final static String INVITATION_CODE = "SMS_174812502";    //企业邀请码
    public final static String REFUSE_JOIN_CODE = "SMS_174985564";   //企业拒绝申请
    public final static String AGREE_JOIN_CODE = "SMS_174985930";   //企业同意申请
    public final static String AGREE_CREATE_COMPANY_CODE = "SMS_174987200";   //同意企业创建
    public final static String REFUSE_CREATE_COMPANY_CODE = "SMS_174992134";   //拒绝企业创建
    //go easy key
    public final static String GOEASY_REGION_HOST = "http://rest-hangzhou.goeasy.io";
    public final static String GOEASY_COMMON_KEY = "PR-9c4e3e16c5ed4d7a9fffea8982d079e2";
    public final static String GOEASY_SECRET_KEY = "22e30a1f2a4445a8";//otp

    /**
     * 企业相关
     */
    public final static String SUPER_ADMIN = "XH_ADMIN";

    /**
     * 全局
     */
    public final static String PUBLIC_PRODUCT = "Public";

    /**
     * 产品状态
     */
    public final static int SELF_BUILT = 1;     //自建

    public final static int TO_ON_SHELVES = 2;  //待上架

    public final static int AUDITING = 3;       //审核中

    public final static int REFUSED = 4;        //已拒绝

    public final static int PASSED = 5;         //已通过,进入大公海

    public final static int ASSISTING = 6;      //协助中

    public final static int TO_CONFIRMED = 7;   //(协助完毕)待确认

    public final static int SYNC = 8;           //同步中

    public final static int TO_UPDATE = 9;      //待更新待选择状态（同步或复制）

    public final static int TO_OFF_SHELVES = 10;//待下架

    /**
     * 权限
     */

    //销售机会权限
    public final static String ALL_OPPORTUNITIES = "allOpportunities";  //查看所有

    public final static String COST_ZONE = "costZone";   //查看成本核算区

    public final static String DISCOUNT = "discount";   //价格优惠至（不低于）（折扣力度）

    public final static String DOWNLOAD_EXCEL = "downloadExcel";    //生成excel报价单

    //项目管理
    public final static String ALL_PROJECTS = "allProjects";    //查看所有

    //产品库
    public final static String ALL_PRODUCTS = "allProducts";    //查看未启用产品

    public final static String MY_PRODUCT = "myProduct";    //我的产品库 编辑、修改、移动 包括目录

    //客户管理
    public final static String ALL_CUSTOMERS = "allCustomers";  //查看所有

    //干系人管理
    public final static String ALL_PEOPLES = "allPeoples";  //查看所有

    //价格权限
    public final static String SALE_PRICE = "salePrice";    //我的零售价

    public final static String PURCH_PRICE = "purchPrice"; //进货价/成本价

    //日程安排
    public final static String ALL_SCHEDULES = "allSchedules";  //查看所有

    //日志查询
    public final static String ALL_RECORDS = "allRecords";   //查看所有

    //其他
    public final static String PROVIDER = "provider";   //云端管理权限

    public final static String SUPER = "super"; //管理员

    public final static String CUSTOM_OPPORTUNITY_STAGE = "customOpportunityStage";   //商机的自定义看板

    public final static String CUSTOM_PROJECT_STAGE = "customProjectStage";   //项目的自定义看板


    /**
     * 项目阶段类型
     */
    public final static Integer OPPORTUNITY_STAGE = 0;    //商机

    public final static Integer ENGINEERING_STAGE = 1;    //工程

    /**
     * 部门
     */
    public final static String DEPARTMENT_MANAGER = "departmentManager";    //部门经理

}
