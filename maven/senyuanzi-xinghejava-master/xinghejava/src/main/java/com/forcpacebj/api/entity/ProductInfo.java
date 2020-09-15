package com.forcpacebj.api.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.forcpacebj.api.utils.CustomStringStringConverter;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ProductInfo {

    private String productId;

    private AccountInfo account;

    //产品状态：1自建的 2待上架 3审核中 4已拒绝 5已上架，进入大公海 6协助中 7协助完毕，待确认 8同步中 9待更新 10待下架
    @ExcelProperty(index = 12)
    private Integer status;

    private String productName;

    @ExcelProperty(index = 1)
    private String shortName;

    private ProductCategoryInfo category;

    private String publicCategoryId;    //category2 公海用的目录

    @ExcelProperty(index = 2)
    private String brand;

    @ExcelProperty(index = 3)
    private String productArea;

    @ExcelProperty(index = 4)
    private String color;

    @ExcelProperty(index = 9, converter = CustomStringStringConverter.class)
    private BigDecimal guidePrice;

    @ExcelProperty(index = 11, converter = CustomStringStringConverter.class)
    private BigDecimal purchPrice;

    @ExcelProperty(index = 10, converter = CustomStringStringConverter.class)
    private BigDecimal salePrice;

    @ExcelProperty(index = 5)
    private String unit;

    //待更新的产品 倒计时 天数 (拼成可直接展示的样子)
    private String countDown;

    @ExcelProperty(index = 0)
    private String role;

    @ExcelProperty(index = 6)
    private String remark;

    @ExcelProperty(index = 7)
    private String brief;

    @ExcelProperty(index = 8)
    private String feature;

    private String productDetail;

    private String productDetail2;  //自己的

    private Boolean isShow;

    private Boolean state;  //是否有效

    private Integer sortNumber;

    private String pictureUrl;

    private Integer pictureScale; //图片缩放

    private ProductTypeEnum productType;

    private Boolean isSharedProduct;

    private AccountInfo fromAccount;

    private String fromProductId;

    private AccountInfo originAccount;

    private String originProductId;

    /****** 投影机 ******/

    private BigDecimal minDistance; //投射100寸16:9幕布的最小距离

    private BigDecimal maxDistance; //投射100寸16:9幕布的最大距离


    /****** 幕布 ******/

    private BigDecimal curtainSize; //幕布尺寸

    private String curtainProportion; //幕布比例

    //角色进货价
    private List<RolePurchPriceInfo> rolePurchPriceList;
    //好友供货价
    private List<FriendSalePriceInfo> friendSalePriceList;


    private List<ProductTagInfo> tags;

    private List<ProductPictureInfo> pictureList;  //轮播图

    //分类 特殊属性
    private List<ProductParameterInfo> productParameters;

    private CatalogInfo catalog;  //分类id

    private ProductRecordInfo productRecord;

    private Integer downloadedCount;


    private Boolean isInExclusive;


    private Integer productShareId;

    private Boolean isAlreadyReceive;

    //适配字段
    private String id;
    private String ProductCategoryId;
    private String CatalogId;
    private String FromAccountId;
    private String OriginAccountId;
    private Date UpdateTime;
    private Date CreateTime;
    private String accountId;
}
