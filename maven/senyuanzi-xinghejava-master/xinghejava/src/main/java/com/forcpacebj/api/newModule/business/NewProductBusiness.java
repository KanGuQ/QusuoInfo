package com.forcpacebj.api.newModule.business;

import com.forcpacebj.api.business.db;
import com.forcpacebj.api.entity.ProductInfo;
import com.forcpacebj.api.utils.CollectionUtil;
import com.forcpacebj.api.utils.StrUtil;
import lombok.val;
import org.sql2o.Connection;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.halt;

public class NewProductBusiness {


    /**
     * 根据productId，更新status
     *
     * @param productId
     * @param status    产品状态：1自建的 2待上架 3审核中 4已拒绝 5已通过，进入大公海 6协助中 7协助完毕，待确认 8同步中 9待更新，面对同步或复制的选择 10待下架
     */
    public static void updateStatus(int productId, int status) {
        val sql = "UPDATE tbProduct SET " +
                  "        status = :status " +
                  " WHERE  ProductId = :productId ";

        try (val con = db.sql2o.beginTransaction()) {

            con.createQuery(sql)
                    .addParameter("status", status)
                    .addParameter("productId", productId)
                    .executeUpdate();

            con.commit();
        }
    }

    /**
     * 根据productId，批量 更新status
     *
     * @param productIds
     * @param status     产品状态：1自建的 2待上架 3审核中 4已拒绝 5已通过，进入大公海 6协助中 7协助完毕，待确认 8同步中 9面对同步或复制的选择
     */
    public static void batchUpdateStatus(List<String> productIds, int status) {
        if (CollectionUtil.isEmpty(productIds)) throw halt(400, "未选择产品");
        val sql = "UPDATE tbProduct SET " +
                  "        status = :status " +
                  " WHERE  ProductId in  " + productIds.toString().replace("[", "(").replace("]", ")");

        try (val con = db.sql2o.beginTransaction()) {

            con.createQuery(sql)
                    .addParameter("status", status)
                    .executeUpdate();

            con.commit();
        }
    }

    /**
     * 根据originProductId，查询产品id的list
     *
     * @param originProductId 源产品id
     * @return 产品id的list
     */
    public static List<String> findProductIdsByOriginProductId(int originProductId) {
        val sql = "SELECT productId from " +
                  " tbProduct " +
                  " WHERE  originProductId = :originProductId  ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("originProductId", originProductId)
                    .executeAndFetch(ProductInfo.class).stream().map(ProductInfo::getProductId).collect(Collectors.toList());
        }
    }


    /**
     * 根据ProductId，查询产品
     *
     * @param productId 产品id
     * @return 产品id的list
     */
    public static ProductInfo findProductById(int productId) {

        val sql = "SELECT * from " +
                  " tbProduct " +
                  " WHERE  productId = :productId  ";

        try (val con = db.sql2o.open()) {

            //查询指定productId 的 产品
            return con.createQuery(sql)
                    .addParameter("productId", productId)
                    .executeAndFetchFirst(ProductInfo.class);
        }
    }

    /**
     * 用于数据统计的查询
     *
     * @param productId 产品id
     * @return
     */
    public static ProductInfo counterFindInTransaction(Connection con, String productId) {

        val sql = "SELECT productId,OriginAccountId,originProductId from " +
                  " tbProduct " +
                  " WHERE  productId = :productId  ";


        //查询指定productId 的 产品
        return con.createQuery(sql)
                .addParameter("productId", productId)
                .executeAndFetchFirst(ProductInfo.class);
    }


    /**
     * 根据OriginProductId，查询产品
     *
     * @param originProductId 产品id
     * @return ProductInfo
     */
    public static ProductInfo findProductByOriginProductId(int originProductId) {

        val sql = "SELECT * from " +
                  " tbProduct " +
                  " WHERE  originProductId = :originProductId  ";

        try (val con = db.sql2o.open()) {

            return con.createQuery(sql)
                    .addParameter("originProductId", originProductId)
                    .executeAndFetchFirst(ProductInfo.class);
        }
    }


    /**
     * 查询同步了指定productId产品的 企业id
     *
     * @param originProductId 源产品id
     * @return 产品id的list
     */
    public static List<String> findAccountIdsByOriginProductId(int originProductId) {
        val sql = "SELECT accountId from " +
                  " tbProduct " +
                  " WHERE  originProductId = :originProductId  ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("originProductId", originProductId)
                    .executeAndFetch(ProductInfo.class).stream().map(ProductInfo::getAccountId).distinct().collect(Collectors.toList());
        }
    }


    /**
     * 根据传入条件，查询产品list
     * <p>
     * 传null则为忽略这个条件
     * <p>
     * 产品状态：1自建的 2待上架 3审核中 4已拒绝 5已通过，进入大公海 6协助中 7协助完毕，待确认 8同步中 9待更新 10待下架
     *
     * @return 产品list
     */
    public static List<ProductInfo> findProductByCondition(Map condition) {
        val category = "(SELECT Id FROM tbproductcategory  " +
                       " WHERE Id = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory WHERE ParentId IN " +
                       " (SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public') " +
                       " AND RelatedAccountId = :accountId AND AccountId = 'Public' )";

        val sql = " SELECT P.ProductId ,P.ProductName ,P.shortName shortName ," +
                  " P.ProductCategoryId 'category.id', C.Name 'category.name',CL.Id 'catalog.id',CL.name 'catalog.name'," +
                  "(SELECT name FROM tbCatalog WHERE id = CL.parentId) 'catalog.parentName',CL.parentId 'catalog.parentId', P.Brand ," +
                  " P.Remark, P.Brief ,P.Feature ,P.ProductDetail ,P.ProductDetail2 ,P.ProductArea, P.Color, P.GuidePrice ,P.PurchPrice , P.SalePrice, P.Unit, P.Role ," +
                  " P.IsShow ,P.SortNumber ,P.PictureUrl ,P.PictureScale ,P.ProductType ,P.MinDistance ,P.MaxDistance ,P.CurtainSize ,P.curtainProportion ," +
                  " P.IsSharedProduct ,P.accountId,P.FromAccountId 'fromAccount.accountId' ,P.FromProductId ,P.OriginAccountId 'originAccount.accountId' ,P.OriginProductId ,P.status,P.createTime," +
                  " P.isInExclusive " +
                  " from " +
                  " tbProduct P" +
                  " LEFT JOIN tbProductCategory C on P.AccountId=C.AccountId AND P.ProductCategoryId = C.Id " +
                  " LEFT JOIN tbCatalog CL ON CL.Id = P.CatalogId " +
                  " WHERE ( P.accountId = :accountId or :accountId is null) " +
                  " and ((CASE WHEN :publicCategoryId = 'TEMP' THEN P.PublicCategoryId IS NULL ELSE P.PublicCategoryId IN " + category + " END) OR :publicCategoryId is null ) " +
                  " and ( P.status = :status or :status is null) " +
                  " and ( P.catalogId = :catalogId or :catalogId is null) " +
                  "  and  (P.ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                  "  and  (P.Brand LIKE CONCAT('%',:brand,'%') OR :brand is null) " +
                  "  ORDER BY updateTime desc " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            val status = condition.get("status").toString();

            val list = con.createQuery(sql)
                    .addParameter("accountId", condition.get("accountId"))
                    .addParameter("status", condition.get("status"))
                    .addParameter("publicCategoryId", condition.get("publicCategoryId"))
                    .addParameter("productName", condition.get("productName"))
                    .addParameter("brand", condition.get("brand"))
                    .addParameter("catalogId", condition.get("catalogId"))
                    .addParameter("PAGEOFFSET", condition.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", condition.get("PAGESIZE"))
                    .executeAndFetch(ProductInfo.class);

            if (status.equals("5") || status.equals("9") || status.equals("10")) {//为了节省性能，而导致函数的不一致，目前只有状态5、9、10，需要倒计时字段
                list.forEach(productInfo -> {

                    val sql1 = "SELECT createTime from " +
                               " tbProduct " +
                               " WHERE  originProductId = :originProductId and ( status=9 or status=10) ";

                    ProductInfo updateProduct;

                    updateProduct = con.createQuery(sql1)
                            .addParameter("originProductId", productInfo.getProductId())
                            .executeAndFetchFirst(ProductInfo.class);

                    if (updateProduct != null) {
                        long diff = new Date().getTime() - updateProduct.getCreateTime().getTime();
                        long day = 10 - diff / (1000 * 60 * 60 * 24);
                        if (status.equals("10")) {
                            productInfo.setCountDown(day + "天后下架");
                        }
                        if (status.equals("9")) {
                            productInfo.setCountDown(day + "天后更新");
                        }
                        if (status.equals("5")) {
                            productInfo.setCountDown(day + "天后变动");
                        }
                    } else {
                        productInfo.setCountDown("");
                    }
                });
            }

            return list;

        }
    }


    /**
     * 查询倒计时改变的产品
     *
     * @return 产品list
     */
    public static List<ProductInfo> findWillModifyProduct(Map condition) {
        val category = "(SELECT Id FROM tbproductcategory  " +
                       " WHERE Id = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory WHERE ParentId IN " +
                       " (SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public') " +
                       " AND RelatedAccountId = :accountId AND AccountId = 'Public' )";

        val sql = " SELECT P.ProductId ,P.ProductName ,P.shortName shortName ," +
                  " P.ProductCategoryId 'category.id', C.Name 'category.name',CL.Id 'catalog.id',CL.name 'catalog.name'," +
                  "(SELECT name FROM tbCatalog WHERE id = CL.parentId) 'catalog.parentName',CL.parentId 'catalog.parentId', P.Brand ," +
                  " P.Remark, P.Brief ,P.Feature ,P.ProductDetail ,P.ProductDetail2 ,P.ProductArea, P.Color, P.GuidePrice ,P.PurchPrice , P.SalePrice, P.Unit, P.Role ," +
                  " P.IsShow ,P.SortNumber ,P.PictureUrl ,P.PictureScale ,P.ProductType ,P.MinDistance ,P.MaxDistance ,P.CurtainSize ,P.curtainProportion ," +
                  " P.IsSharedProduct ,P.FromAccountId 'fromAccount.accountId' ,P.FromProductId ,P.OriginAccountId 'originAccount.accountId' ,P.OriginProductId ,P.status,P.createTime" +
                  " from " +
                  " tbProduct P" +
                  " LEFT JOIN tbProductCategory C on P.AccountId=C.AccountId AND P.ProductCategoryId = C.Id " +
                  " LEFT JOIN tbCatalog CL ON CL.Id = P.CatalogId " +
                  " WHERE ( P.accountId = :accountId ) " +
                  " and ( P.status = 9 or P.status = 10 ) " +
                  " and ( P.catalogId = :catalogId or :catalogId is null) " +
                  " and ((CASE WHEN :publicCategoryId = 'TEMP' THEN P.PublicCategoryId IS NULL ELSE P.PublicCategoryId IN " + category + " END) OR :publicCategoryId is null ) " +
                  "  and  (P.ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                  "  and  (P.Brand LIKE CONCAT('%',:brand,'%') OR :brand is null) " +
                  "  ORDER BY updateTime desc " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            val list = con.createQuery(sql)
                    .addParameter("accountId", condition.get("accountId"))
                    .addParameter("publicCategoryId", condition.get("publicCategoryId"))
                    .addParameter("productName", condition.get("productName"))
                    .addParameter("brand", condition.get("brand"))
                    .addParameter("catalogId", condition.get("catalogId"))
                    .addParameter("PAGEOFFSET", condition.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", condition.get("PAGESIZE"))
                    .executeAndFetch(ProductInfo.class);


            list.forEach(productInfo -> {

                long diff = new Date().getTime() - productInfo.getCreateTime().getTime();
                long day = 10 - diff / (1000 * 60 * 60 * 24);
                if (productInfo.getStatus() == 9) {
                    productInfo.setCountDown(day + "天后更新");
                }
                if (productInfo.getStatus() == 10) {
                    productInfo.setCountDown(day + "天后下架");
                }
            });

            return list;
        }
    }

    /**
     * 查询倒计时改变的产品，包括待更新和待下架
     *
     * @return 产品list
     */
    public static int findWillModifyProductCount(Map condition) {
        val category = "(SELECT Id FROM tbproductcategory  " +
                       " WHERE Id = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory WHERE ParentId IN " +
                       " (SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public') " +
                       " AND RelatedAccountId = :accountId AND AccountId = 'Public' )";

        val sql = "SELECT count(1) from " +
                  " tbProduct " +
                  " WHERE ( accountId = :accountId ) " +
                  " and ( status = 9 or status = 10 ) " +
                  " and ( catalogId = :catalogId or :catalogId is null) " +
                  " and ((CASE WHEN :publicCategoryId = 'TEMP' THEN PublicCategoryId IS NULL ELSE PublicCategoryId IN " + category + " END) OR :publicCategoryId is null ) " +
                  "  and  (ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                  "  and  (Brand LIKE CONCAT('%',:brand,'%') OR :brand is null) ";

        try (val con = db.sql2o.open()) {

            return con.createQuery(sql)
                    .addParameter("accountId", condition.get("accountId"))
                    .addParameter("publicCategoryId", condition.get("publicCategoryId"))
                    .addParameter("productName", condition.get("productName"))
                    .addParameter("brand", condition.get("brand"))
                    .addParameter("catalogId", condition.get("catalogId"))
                    .executeScalar(int.class);

        }
    }

    /**
     * 查询公海管理中心所有产品
     *
     * @return 产品list
     */
    public static List<ProductInfo> findAllProductsInPublicAdmin(Map condition) {
        val category = "(SELECT Id FROM tbproductcategory  " +
                       " WHERE Id = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory WHERE ParentId IN " +
                       " (SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public') " +
                       " AND RelatedAccountId = :accountId AND AccountId = 'Public' )";

        val sql = " SELECT P.ProductId ,P.ProductName ,P.shortName shortName ," +
                  " P.ProductCategoryId 'category.id', C.Name 'category.name',CL.Id 'catalog.id',CL.name 'catalog.name'," +
                  "(SELECT name FROM tbCatalog WHERE id = CL.parentId) 'catalog.parentName',CL.parentId 'catalog.parentId', P.Brand ," +
                  " P.Remark, P.Brief ,P.Feature ,P.ProductDetail ,P.ProductDetail2 ,P.ProductArea, P.Color, P.GuidePrice ,P.PurchPrice , P.SalePrice, P.Unit, P.Role ," +
                  " P.IsShow ,P.SortNumber ,P.PictureUrl ,P.PictureScale ,P.ProductType ,P.MinDistance ,P.MaxDistance ,P.CurtainSize ,P.curtainProportion ," +
                  " P.IsSharedProduct ,P.FromAccountId 'fromAccount.accountId' ,P.FromProductId ,P.OriginAccountId 'originAccount.accountId' ,P.OriginProductId ,P.status," +
                  " P.isInExclusive " +
                  " from " +
                  " tbProduct P" +
                  " LEFT JOIN tbProductCategory C on P.AccountId=C.AccountId AND P.ProductCategoryId = C.Id " +
                  " LEFT JOIN tbCatalog CL ON CL.Id = P.CatalogId " +
                  " WHERE ( P.accountId = :accountId) " +
                  " and ((CASE WHEN :publicCategoryId = 'TEMP' THEN P.PublicCategoryId IS NULL ELSE P.PublicCategoryId IN " + category + " END) OR :publicCategoryId is null ) " +
                  " and ( P.status not in (0,1,8,9,10) ) " +
                  " and ( P.catalogId = :catalogId or :catalogId is null) " +
                  "  and  (P.ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                  "  and  (P.Brand LIKE CONCAT('%',:brand,'%') OR :brand is null) " +
                  "  ORDER BY updateTime desc " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            val list = con.createQuery(sql)
                    .addParameter("accountId", condition.get("accountId"))
                    .addParameter("publicCategoryId", condition.get("publicCategoryId"))
                    .addParameter("productName", condition.get("productName"))
                    .addParameter("brand", condition.get("brand"))
                    .addParameter("catalogId", condition.get("catalogId"))
                    .addParameter("PAGEOFFSET", condition.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", condition.get("PAGESIZE"))
                    .executeAndFetch(ProductInfo.class);


            list.forEach(productInfo -> {
                if (productInfo.getStatus() == 5 || productInfo.getStatus() == 9 || productInfo.getStatus() == 10) {//为了节省性能，而导致函数的不一致，目前只有状态5、9、10，需要倒计时字段

                    val sql1 = "SELECT createTime from " +
                               " tbProduct " +
                               " WHERE  originProductId = :originProductId and ( status=9 or status=10) ";

                    ProductInfo updateProduct;

                    updateProduct = con.createQuery(sql1)
                            .addParameter("originProductId", productInfo.getProductId())
                            .executeAndFetchFirst(ProductInfo.class);

                    if (updateProduct != null) {
                        long diff = new Date().getTime() - updateProduct.getCreateTime().getTime();
                        long day = 10 - diff / (1000 * 60 * 60 * 24);
                        if (productInfo.getStatus() == 10) {
                            productInfo.setCountDown(day + "天后下架");
                        }
                        if (productInfo.getStatus() == 9) {
                            productInfo.setCountDown(day + "天后更新");
                        }
                        if (productInfo.getStatus() == 5) {
                            productInfo.setCountDown(day + "天后变动");
                        }
                    } else {
                        productInfo.setCountDown("");
                    }
                }
            });
            return list;

        }
    }

    /**
     * 查询公海管理中心所有产品数量
     *
     * @return 产品list
     */
    public static int findAllProductsInPublicAdminCount(Map condition) {
        val category = "(SELECT Id FROM tbproductcategory  " +
                       " WHERE Id = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory WHERE ParentId IN " +
                       " (SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public') " +
                       " AND RelatedAccountId = :accountId AND AccountId = 'Public' )";

        val sql = "SELECT count(1) from " +
                  " tbProduct " +
                  " WHERE ( accountId = :accountId ) " +
                  " and ( status not in (0,1,8,9,10) ) " +
                  " and ( catalogId = :catalogId or :catalogId is null) " +
                  " and ((CASE WHEN :publicCategoryId = 'TEMP' THEN PublicCategoryId IS NULL ELSE PublicCategoryId IN " + category + " END) OR :publicCategoryId is null ) " +
                  "  and  (ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                  "  and  (Brand LIKE CONCAT('%',:brand,'%') OR :brand is null) ";

        try (val con = db.sql2o.open()) {

            return con.createQuery(sql)
                    .addParameter("accountId", condition.get("accountId"))
                    .addParameter("publicCategoryId", condition.get("publicCategoryId"))
                    .addParameter("productName", condition.get("productName"))
                    .addParameter("brand", condition.get("brand"))
                    .addParameter("catalogId", condition.get("catalogId"))
                    .executeScalar(int.class);

        }
    }


    /**
     * 根据accountId和status，查询产品list 数量
     *
     * @return 产品list
     */
    public static int findProductCountByCondition(Map condition) {
        val category = "(SELECT Id FROM tbproductcategory  " +
                       " WHERE Id = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                       " UNION " +
                       " SELECT Id FROM tbproductcategory WHERE ParentId IN " +
                       " (SELECT Id FROM tbproductcategory  " +
                       " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public') " +
                       " AND RelatedAccountId = :accountId AND AccountId = 'Public' )";

        val sql = "SELECT count(1) from " +
                  " tbProduct " +
                  " WHERE ( accountId = :accountId or :accountId is null) " +
                  " and ((CASE WHEN :publicCategoryId = 'TEMP' THEN PublicCategoryId IS NULL ELSE PublicCategoryId IN " + category + " END) OR :publicCategoryId is null ) " +
                  " and ( status = :status or :status is null) " +
                  " and ( catalogId = :catalogId or :catalogId is null) " +
                  "  and  ( ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                  "  and  ( Brand LIKE CONCAT('%',:brand,'%') OR :brand is null) ";

        try (val con = db.sql2o.open()) {

            return con.createQuery(sql)
                    .addParameter("accountId", condition.get("accountId"))
                    .addParameter("status", condition.get("status"))
                    .addParameter("publicCategoryId", condition.get("publicCategoryId"))
                    .addParameter("productName", condition.get("productName"))
                    .addParameter("brand", condition.get("brand"))
                    .addParameter("catalogId", condition.get("catalogId"))
                    .executeScalar(int.class);
        }
    }


    /**
     * 同步指定产品
     * 零售商从大公海同步产品，到自己的产品库
     *
     * @param productId 产品id
     * @return
     */
    public static void syncProduct(int productId, String accountId) {


        val sql = "SELECT * from " +
                  " tbProduct " +
                  " WHERE  productId = :productId  " +
                  " and status = 5 ";//只有已上架产品才能被零售商同步

        try (val con = db.sql2o.beginTransaction()) {

            //查询指定productId 的 产品
            val product = con.createQuery(sql)
                    .addParameter("productId", productId)
                    .executeAndFetchFirst(ProductInfo.class);

            //为零售商的私有库插入该产品
            val insertSql = "INSERT INTO tbProduct(AccountId ,shortName ,ProductName ,ProductCategoryId ,CatalogId,Brand ,Remark ,Brief ,Feature ,ProductDetail ,ProductDetail2 ,ProductArea ,Color ," +
                            "GuidePrice ,PurchPrice ,SalePrice ,Unit ,Role ,IsShow ,SortNumber ,PictureUrl ,PictureScale ,ProductType,IsSharedProduct,FromAccountId ,FromProductId ,OriginAccountId,OriginProductId,status) " +
                            " values(:accountId ,:shortName ,CONCAT(:brand,' ',:shortName)  ,:categoryId ,:catalogId,:brand , :remark ,:brief ,:feature ,:productDetail ,:productDetail2 ,:productArea ,:color ," +
                            ":guidePrice ,:purchPrice ,:salePrice ,:unit ,:role ,:isShow ,:sortNumber ,:pictureUrl ,:pictureScale ,:productType ,:isSharedProduct ,:fromAccountId ,:fromProductId ,:originAccountId,:originProductId,:status )";

            //todo 同步到自己的产品库时，可以选择同步到自己的指定目录中
//            product.setProductId(null);
            con.createQuery(insertSql).bind(product)
                    .addParameter("accountId", accountId)//零售商的id
                    .addParameter("originAccountId", product.getAccountId())//创建者的id
                    .addParameter("originProductId", productId)//源产品的id
//                    .addParameter("categoryId",)//目录分类
//                    .addParameter("catalogId", )//产品分类，这个应该不能改？
                    .executeUpdate().getResult();


            //todo 先查询

            //轮播图
            if (CollectionUtil.isNotEmpty(product.getPictureList())) {
                val query = con.createQuery("insert into tbProductPicture(AccountId ,ProductId ,SortNumber ,PictureUrl) values(:accountId, :productId ,:sortNumber ,:pictureUrl)");
                for (int i = 0; i < product.getPictureList().size(); i++) {
                    query.addParameter("accountId", accountId)
                            .addParameter("productId", product.getProductId())
                            .addParameter("sortNumber", i)
                            .addParameter("pictureUrl", product.getPictureList().get(i).getPictureUrl())
                            .executeUpdate();
                }
            }

            //todo 先查询
            //保存特殊属性
            if (CollectionUtil.isNotEmpty(product.getProductParameters())) {
                val productParameterSql = " INSERT INTO tbproductparameter (ProductId,ParameterName,ParameterValue,AccountId,ParameterType,ParameterOptions,SortNum) " +
                                          "VALUES (:productId,:parameterName,:parameterValue,:accountId,:parameterType,:parameterOptions," +
                                          "IFNULL(:sortNum,(SELECT IFNULL(T.NUM,0) FROM (SELECT MAX(SortNum)+1 NUM FROM tbproductparameter WHERE ProductId = :productId) T) )" +
                                          ")";
                product.getProductParameters().forEach(productParameterInfo -> {
                    if (StrUtil.isNotBlank(productParameterInfo.getParameterValue()))
                        con.createQuery(productParameterSql).bind(productParameterInfo)
                                .addParameter("productId", product.getProductId())
                                .addParameter("accountId", accountId)
                                .executeUpdate().getResult();
                });
            }

            con.commit();
        }
    }


    //todo  更新接口
}
