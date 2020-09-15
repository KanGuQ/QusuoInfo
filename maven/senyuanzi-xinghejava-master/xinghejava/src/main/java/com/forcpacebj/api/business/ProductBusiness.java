package com.forcpacebj.api.business;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.*;
import com.forcpacebj.api.utils.CollectionUtil;
import com.forcpacebj.api.utils.StrUtil;
import lombok.val;
import lombok.var;
import org.sql2o.Query;
import spark.utils.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static spark.Spark.halt;

public class ProductBusiness {

    public static List<ProductInfo> find(Map conditions, String accountId) {
        val parameters = (JSONArray) conditions.get("parameters");
        String parameterTemp = "";
        if (CollectionUtil.isNotEmpty(parameters)) {
            parameterTemp = " and  ( exists (SELECT 1 FROM tbProductParameter PP WHERE P.AccountId = PP.AccountId AND P.ProductId = PP.ProductId AND (1=2 ";
            for (Object obj : parameters) {
                JSONObject parameter = (JSONObject) obj;
                parameterTemp += " OR (PP.ParameterName = '" + parameter.getString("parameterName") + "' AND PP.ParameterValue = '" + parameter.getString("parameterValue") + "') ";
            }
            parameterTemp += " ) ) ) ";
        }
        val category = "(SELECT Id FROM tbproductcategory  " +
                " WHERE Id = :categoryId AND AccountId = :accountId " +
                " UNION " +
                " SELECT Id FROM tbproductcategory  " +
                " WHERE ParentId = :categoryId AND AccountId = :accountId " +
                " UNION " +
                " SELECT Id FROM tbproductcategory WHERE ParentId IN " +
                " (SELECT Id FROM tbproductcategory  " +
                " WHERE ParentId = :categoryId AND AccountId = :accountId) " +
                " AND AccountId = :accountId)";
        val publicCategory = "(SELECT Id FROM tbproductcategory  " +
                " WHERE Id = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                " UNION " +
                " SELECT Id FROM tbproductcategory  " +
                " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                " UNION " +
                " SELECT Id FROM tbproductcategory WHERE ParentId IN " +
                " (SELECT Id FROM tbproductcategory  " +
                " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public') " +
                " AND RelatedAccountId = :accountId AND AccountId = 'Public' )";
        boolean isPub = "public".equals(conditions.get("fromPage"));
        val status = isPub ? " = 5 " : " NOT IN (9,10) ";
        val salePrice = isPub ? "IFNULL(FSP.SalePrice,P.SalePrice)" : "P.SalePrice";
        val account = isPub && "all".equals(accountId) ? ":accountId" : "P.AccountId";
        var sortBy = conditions.get("sortBy") == null ? "sortNumber" : conditions.get("sortBy");
        val sql = " SELECT P.ProductId ,P.ProductName ,P.shortName shortName ,P.isInExclusive," +
                " P.ProductCategoryId 'category.id', C.Name 'category.name',P.CatalogId 'catalog.Id', CL.Name 'catalog.name', P.Brand ," +
                " P.Remark, P.ProductArea, P.Color ,P.GuidePrice ,P.PurchPrice , " + salePrice + " SalePrice, P.Unit, P.Role ,P.IsShow ,P.SortNumber ,P.PictureUrl ,P.status," +
                " P.ProductType ,P.MinDistance ,P.MaxDistance ,P.CurtainSize ,P.CurtainProportion ,P.FromAccountId 'fromAccount.accountId' ,P.FromProductId ,A.AccountName 'fromAccount.accountName' ,P.OriginAccountId 'originAccount.accountId',A1.AccountName 'originAccount.accountName', P.OriginProductId " +
                " FROM tbProduct P " +
                " LEFT JOIN tbCatalog CL ON P.CatalogId = CL.Id " +
                " LEFT JOIN tbProductCategory C on P.AccountId=C.AccountId AND P.ProductCategoryId = C.Id " +
                " LEFT JOIN tbAccount A on P.FromAccountId=A.AccountId" +
                " LEFT JOIN tbAccount A1 on P.originAccountId=A1.AccountId " +
                " LEFT JOIN tbFriendSalePrice FSP ON FSP.ProductId = P.ProductId AND FSP.FriendGroupId IN (SELECT GroupId FROM tbfriend WHERE AccountId = P.OriginAccountId AND FriendAccountId = '" + conditions.get("currentAccountId") + "' AND IsAccepted = 1 ) " +
                " WHERE state = :state AND :accountId = " + account + " AND P.status " + status +
                "  and  ( (CASE WHEN :categoryId = 'TEMP' THEN P.ProductCategoryId IS NULL ELSE P.ProductCategoryId IN " + category + " END) OR :categoryId is null ) " +
                "  and  ( (CASE WHEN :publicCategoryId = 'TEMP' THEN P.PublicCategoryId IS NULL ELSE P.PublicCategoryId IN " + publicCategory + " END) OR :publicCategoryId is null ) " +
                "  and  (P.CatalogId IN (SELECT id FROM tbCatalog WHERE ParentId =:catalogId OR Id = :catalogId) OR :catalogId is null ) " +
                "  and  (P.ProductId in ( select ProductId from tbProductTags where AccountId = :accountId and ProductTagId = :productTagId ) OR :productTagId is null ) " +
                "  and  (P.ProductId = :productId OR :productId is null) " +
                "  and  (P.ProductId in ( select ProductId from tbFriendSalePrice where AccountId = :accountId and FriendGroupId = :friendGroupId ) OR :friendGroupId is null) " +
                "  and  ( (P.FromProductId = :fromProductId and P.FromAccountId = :fromAccountId) OR :fromProductId is null) " +
                "  and  (P.ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                "  and  (P.Brand LIKE CONCAT('%',:brand,'%') OR :brand is null) " +
                "  and  (P.Role LIKE CONCAT('%',:role,'%') OR :role is null) " +
                "  and  (P.IsShow = :isShow OR :isShow is null) " +
                "  and  (P.isInExclusive = :isInExclusive OR :isInExclusive is null) " +
                "  and  (P.ProductId LIKE CONCAT('%',:all,'%') OR P.ProductName LIKE CONCAT('%',:all,'%') OR P.Brand LIKE CONCAT('%',:all,'%') OR P.Role LIKE CONCAT('%',:all,'%') OR :all is null) " +
                "  and  ( exists ( select 1 from tbfriendsaleprice S where P.AccountId = S.AccountId and P.ProductId = S.ProductId and S.FriendGroupId = :friendGroupId) OR :friendGroupId is null )" +
                "  and  (P.ProductId in ( select ProductId from ProductShare where AccountId = :accountId and ToAccountId = :toAccountId ) OR :toAccountId is null)" +
                parameterTemp +
                " Order By P." + sortBy +
                " LIMIT :PAGEOFFSET ,:PAGESIZE";
        try (val con = db.sql2o.open()) {
            val list = con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("categoryId", conditions.get("categoryId"))
                    .addParameter("publicCategoryId", conditions.get("publicCategoryId"))
                    .addParameter("catalogId", conditions.get("catalogId"))
                    .addParameter("productTagId", conditions.get("productTagId"))
                    .addParameter("productId", conditions.get("productId"))
                    .addParameter("fromProductId", conditions.get("fromProductId"))
                    .addParameter("fromAccountId", conditions.get("fromAccountId"))
                    .addParameter("productName", conditions.get("productName"))
                    .addParameter("friendGroupId", conditions.get("friendGroupId"))
                    .addParameter("toAccountId", conditions.get("toAccountId"))
                    .addParameter("brand", conditions.get("brand"))
                    .addParameter("role", conditions.get("role"))
                    .addParameter("isShow", conditions.get("isShow"))
                    .addParameter("isInExclusive", conditions.get("isInExclusive"))
                    .addParameter("all", conditions.get("all"))
                    .addParameter("state", conditions.get("state") == null ? 1 : conditions.get("state"))
                    .executeAndFetch(ProductInfo.class);
            return list;
        }
    }

    public static List<ProductInfo> listIncludeFriendSalePrice(Map conditions, String accountId) {
        List<ProductInfo> list = find(conditions, accountId);
        if (CollectionUtil.isNotEmpty(list)) {
            //取好友供货价
            val friendsalePriceSql = " Select Id, FriendGroupId 'friendGroup.groupId',SalePrice From tbFriendSalePrice where AccountId=:accountId And ProductId=:id ";
            try (val con = db.sql2o.open()) {
                list.forEach(product -> {
                    val friendSalePriceList = con.createQuery(friendsalePriceSql)
                            .addParameter("accountId", accountId)
                            .addParameter("id", product.getProductId())
                            .executeAndFetch(FriendSalePriceInfo.class);

                    product.setFriendSalePriceList(friendSalePriceList);
                });
            }
        }
        return list;
    }

    public static int count(Map conditions, String accountId) {
        val parameters = (JSONArray) conditions.get("parameters");
        String parameterTemp = "";
        if (CollectionUtil.isNotEmpty(parameters)) {
            parameterTemp = " and ( exists (SELECT 1 FROM tbProductParameter PP WHERE P.AccountId = PP.AccountId AND P.ProductId = PP.ProductId AND (1=2 ";
            for (Object obj : parameters) {
                JSONObject parameter = (JSONObject) obj;
                parameterTemp += " OR (PP.ParameterName = '" + parameter.getString("parameterName") + "' AND PP.ParameterValue = '" + parameter.getString("parameterValue") + "') ";
            }
            parameterTemp += " ) ) ) ";
        }

        val category = " (SELECT Id FROM tbproductcategory  " +
                " WHERE Id = :categoryId AND AccountId = :accountId " +
                " UNION " +
                " SELECT Id FROM tbproductcategory  " +
                " WHERE ParentId = :categoryId AND AccountId = :accountId " +
                " UNION " +
                " SELECT Id FROM tbproductcategory WHERE ParentId IN " +
                " (SELECT Id FROM tbproductcategory  " +
                " WHERE ParentId = :categoryId AND AccountId = :accountId) " +
                " AND AccountId = :accountId) ";
        val publicCategory = "(SELECT Id FROM tbproductcategory  " +
                " WHERE Id = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                " UNION " +
                " SELECT Id FROM tbproductcategory  " +
                " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public' " +
                " UNION " +
                " SELECT Id FROM tbproductcategory WHERE ParentId IN " +
                " (SELECT Id FROM tbproductcategory  " +
                " WHERE ParentId = :publicCategoryId AND RelatedAccountId = :accountId AND AccountId = 'Public') " +
                " AND RelatedAccountId = :accountId AND AccountId = 'Public' )";

        boolean isPub = "public".equals(conditions.get("fromPage"));
        val status = isPub ? " = 5 " : " NOT IN (9,10) ";
        var account = isPub && "all".equals(accountId) ? ":accountId" : "P.AccountId";

        val sql = " SELECT count(1) xcount FROM tbProduct P " +
                " LEFT JOIN tbProductCategory C on P.AccountId=C.AccountId AND P.ProductCategoryId = C.Id " +
                " WHERE state = :state AND :accountId = " + account +
                "  AND  P.status " + status + parameterTemp +
                "  and  ( (CASE WHEN :categoryId = 'TEMP' THEN P.ProductCategoryId IS NULL ELSE P.ProductCategoryId IN " + category + " END) OR :categoryId is null ) " +
                "  and  ( (CASE WHEN :publicCategoryId = 'TEMP' THEN P.PublicCategoryId IS NULL ELSE P.PublicCategoryId IN " + publicCategory + " END) OR :publicCategoryId is null ) " +
                "  and  (P.CatalogId IN (SELECT id FROM tbCatalog WHERE ParentId =:catalogId OR Id = :catalogId) OR :catalogId is null ) " +
                "  and  (P.ProductId in ( select ProductId from tbProductTags where AccountId = :accountId and ProductTagId = :productTagId ) OR :productTagId is null ) " +
                "  and  (P.ProductId = :productId OR :productId is null) " +
                "  and  (P.ProductId in ( select ProductId from tbFriendSalePrice where AccountId = :accountId and FriendGroupId = :friendGroupId ) OR :friendGroupId is null) " +
                "  and  ( (P.FromProductId = :fromProductId and P.FromAccountId = :fromAccountId) OR :fromProductId is null) " +
                "  and  ( (P.OriginProductId = :originProductId and P.OriginAccountId = :originAccountId) OR :originProductId is null) " +
                "  and  (P.ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                "  and  (P.Brand LIKE CONCAT('%',:brand,'%') OR :brand is null) " +
                "  and  (P.IsShow = :isShow OR :isShow is null) " +
                "  and  (P.isInExclusive = :isInExclusive OR :isInExclusive is null) " +
                "  and  (P.Role LIKE CONCAT('%',:role,'%') OR :role is null) " +
                "  and  (P.ProductId LIKE CONCAT('%',:all,'%') OR P.ProductName LIKE CONCAT('%',:all,'%') OR P.Brand LIKE CONCAT('%',:all,'%') OR P.Role LIKE CONCAT('%',:all,'%') OR :all is null) " +
                "  and  ( exists ( select 1 from tbfriendsaleprice S where P.AccountId = S.AccountId and P.ProductId = S.ProductId and S.FriendGroupId = :friendGroupId) OR :friendGroupId is null )" +
                "  and  (P.ProductId in (SELECT DISTINCT ProductId FROM tbProductParameter WHERE ParameterName LIKE CONCAT('%',:paramName,'%') AND ParameterValue LIKE CONCAT('%',:paramValue,'%')) OR :paramValue IS NULL) " +
                "  and  (P.ProductId in ( select ProductId from ProductShare where AccountId = :accountId and ToAccountId = :toAccountId ) OR :toAccountId is null)";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("categoryId", conditions.get("categoryId"))
                    .addParameter("publicCategoryId", conditions.get("publicCategoryId"))
                    .addParameter("catalogId", conditions.get("catalogId"))
                    .addParameter("productTagId", conditions.get("productTagId"))
                    .addParameter("productId", conditions.get("productId"))
                    .addParameter("fromProductId", conditions.get("fromProductId"))
                    .addParameter("fromAccountId", conditions.get("fromAccountId"))
                    .addParameter("originProductId", conditions.get("originProductId"))
                    .addParameter("originAccountId", conditions.get("originAccountId"))
                    .addParameter("productName", conditions.get("productName"))
                    .addParameter("friendGroupId", conditions.get("friendGroupId"))
                    .addParameter("toAccountId", conditions.get("toAccountId"))
                    .addParameter("brand", conditions.get("brand"))
                    .addParameter("role", conditions.get("role"))
                    .addParameter("isShow", conditions.get("isShow"))
                    .addParameter("isInExclusive", conditions.get("isInExclusive"))
                    .addParameter("all", conditions.get("all"))
                    .addParameter("paramName", conditions.get("paramName"))
                    .addParameter("paramValue", conditions.get("paramValue"))
                    .addParameter("state", conditions.get("state") == null ? 1 : conditions.get("state"))
                    .executeScalar(int.class);
        }
    }

    public static ProductInfo load(String id) {

        String sql = " SELECT P.ProductId ,P.AccountId ,P.ProductName ,P.shortName shortName ,P.accountId," +
                " P.ProductCategoryId 'category.id', C.Name 'category.name',P.PublicCategoryId,CL.Id 'catalog.id',CL.name 'catalog.name'," +
                "(SELECT name FROM tbCatalog WHERE id = CL.parentId) 'catalog.parentName',CL.parentId 'catalog.parentId', P.Brand ," +
                " P.Remark, P.Brief ,P.Feature ,P.ProductDetail ,P.ProductDetail2 ,P.ProductArea, P.Color, P.GuidePrice ,P.PurchPrice , P.SalePrice, P.Unit, P.Role ," +
                " P.IsShow ,P.SortNumber ,P.PictureUrl ,P.PictureScale ,P.ProductType ,P.MinDistance ,P.MaxDistance ,P.CurtainSize ,P.curtainProportion ," +
                " P.IsSharedProduct ,P.FromAccountId 'fromAccount.accountId' ,P.FromProductId ,P.OriginAccountId 'originAccount.accountId' ,P.OriginProductId,P.status " +
                " FROM tbProduct P  " +
                " LEFT JOIN tbProductCategory C on P.AccountId=C.AccountId AND P.ProductCategoryId = C.Id " +
                " LEFT JOIN tbCatalog CL ON CL.Id = P.CatalogId " +
                " WHERE P.ProductId = :id";

        try (val con = db.sql2o.open()) {
            val product = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(ProductInfo.class);

            if (product != null) {
                //取角色进货价
                sql = " Select RoleId 'userRole.roleId', PurchPrice From tbRolePurchPrice  " +
                        " where AccountId=:accountId And ProductId=:id ";
                val rolePurchPriceList = con.createQuery(sql)
                        .addParameter("accountId", product.getAccountId())
                        .addParameter("id", id)
                        .executeAndFetch(RolePurchPriceInfo.class);

                product.setRolePurchPriceList(rolePurchPriceList);

                //取好友供货价
                sql = " Select FriendGroupId 'friendGroup.groupId',SalePrice From tbFriendSalePrice" +
                        " where AccountId=:accountId And ProductId=:id ";
                val friendSalePriceList = con.createQuery(sql)
                        .addParameter("accountId", product.getAccountId())
                        .addParameter("id", id)
                        .executeAndFetch(FriendSalePriceInfo.class);

                product.setFriendSalePriceList(friendSalePriceList);

                //取轮播图
                sql = " Select SortNumber ,PictureUrl From tbProductPicture Where AccountId=:accountId And ProductId=:id ORDER BY SortNumber";
                val productPictureList = con.createQuery(sql)
                        .addParameter("accountId", product.getAccountId())
                        .addParameter("id", id)
                        .executeAndFetch(ProductPictureInfo.class);

                product.setPictureList(productPictureList);

                //取特殊属性（按产品关联的分类id去取分类特殊属性）
                sql = "SELECT " +
                        " ParameterName , " +
                        " ParameterType , " +
                        " ParameterOptions , " +
                        " ParameterValue, SortNum FROM tbProductParameter " +
                        " WHERE productId = :productId and accountId = :accountId ORDER BY SortNum;";
                val catalogParameterList = con.createQuery(sql)
                        .addParameter("productId", id)
                        .addParameter("accountId", product.getAccountId())
                        .executeAndFetch(ProductParameterInfo.class);
                product.setProductParameters(catalogParameterList);

                //添加countdown字段
                val sql1 = "SELECT status,createTime from " +
                        " tbProduct " +
                        " WHERE  originProductId = :originProductId and ( status=9 or status=10) ";

                ProductInfo updateProduct;

                updateProduct = con.createQuery(sql1)
                        .addParameter("originProductId", product.getOriginProductId())
                        .executeAndFetchFirst(ProductInfo.class);
                if (updateProduct != null) {
                    long diff = new Date().getTime() - updateProduct.getCreateTime().getTime();
                    long day = 10 - diff / (1000 * 60 * 60 * 24);
                    int status = updateProduct.getStatus();
                    if (status == 10) {
                        product.setCountDown(day + "天后下架");
                    } else if (status == 9) {
                        product.setCountDown(day + "天后更新");
                    } else if (status == 5) {
                        product.setCountDown(day + "天后变动");
                    }
                } else {
                    product.setCountDown("");
                }

            }

            return product;
        }
    }

    /**
     * 对外调用,隐藏价格
     */
    public static ProductInfo loadByIdHidePrice(String id, String accountId) {
        String sql = " SELECT P.ProductId ,P.AccountId ,P.ProductName ,P.shortName shortName ,P.accountId," +
                " P.ProductCategoryId 'category.id', C.Name 'category.name',P.PublicCategoryId,CL.Id 'catalog.id',CL.name 'catalog.name'," +
                "(SELECT name FROM tbCatalog WHERE id = CL.parentId) 'catalog.parentName',CL.parentId 'catalog.parentId', P.Brand ," +
                " P.Remark, P.Brief ,P.Feature ,P.ProductDetail ,P.ProductDetail2 ,P.ProductArea, P.Color, P.GuidePrice ,IFNULL(FSP.SalePrice,P.SalePrice) PurchPrice , IFNULL(FSP.SalePrice,P.SalePrice) SalePrice, P.Unit, P.Role ," +
                " P.IsShow ,P.SortNumber ,P.PictureUrl ,P.PictureScale ,P.ProductType ,P.MinDistance ,P.MaxDistance ,P.CurtainSize ,P.curtainProportion ," +
                " P.IsSharedProduct ,P.FromAccountId 'fromAccount.accountId' ,P.FromProductId ,P.OriginAccountId 'originAccount.accountId' ,P.OriginProductId,P.status " +
                " FROM tbProduct P " +
                " LEFT JOIN tbProductCategory C on P.AccountId=C.AccountId AND P.ProductCategoryId = C.Id " +
                " LEFT JOIN tbCatalog CL ON CL.Id = P.CatalogId " +
                " LEFT JOIN tbFriendSalePrice FSP ON FSP.ProductId = P.ProductId AND FSP.FriendGroupId IN (SELECT GroupId FROM tbfriend WHERE AccountId = P.OriginAccountId AND FriendAccountId = :currentAccountId AND IsAccepted = 1 ) " +
                " WHERE P.ProductId = :id";

        try (val con = db.sql2o.open()) {
            val product = con.createQuery(sql)
                    .addParameter("id", id)
                    .addParameter("currentAccountId", accountId)
                    .executeAndFetchFirst(ProductInfo.class);
            if (product != null) {
                //取轮播图
                sql = " Select SortNumber,PictureUrl From tbProductPicture Where AccountId=:accountId And ProductId=:id ORDER BY SortNumber";
                val productPictureList = con.createQuery(sql)
                        .addParameter("accountId", product.getAccountId())
                        .addParameter("id", id)
                        .executeAndFetch(ProductPictureInfo.class);

                product.setPictureList(productPictureList);

                //取特殊属性（按产品关联的分类id去取分类特殊属性）
                sql = "SELECT " +
                        " ParameterName , " +
                        " ParameterType , " +
                        " ParameterOptions , " +
                        " ParameterValue, SortNum FROM tbProductParameter " +
                        " WHERE productId = :productId and accountId = :accountId ORDER BY SortNum;";
                val catalogParameterList = con.createQuery(sql)
                        .addParameter("productId", id)
                        .addParameter("accountId", product.getAccountId())
                        .executeAndFetch(ProductParameterInfo.class);
                product.setProductParameters(catalogParameterList);

                //添加countdown字段
                val sql1 = "SELECT status,createTime from " +
                        " tbProduct " +
                        " WHERE  originProductId = :originProductId and ( status=9 or status=10) ";

                ProductInfo updateProduct;

                updateProduct = con.createQuery(sql1)
                        .addParameter("originProductId", product.getOriginProductId())
                        .executeAndFetchFirst(ProductInfo.class);
                if (updateProduct != null) {
                    long diff = new Date().getTime() - updateProduct.getCreateTime().getTime();
                    long day = 10 - diff / (1000 * 60 * 60 * 24);
                    int status = updateProduct.getStatus();
                    if (status == 10) {
                        product.setCountDown(day + "天后下架");
                    } else if (status == 9) {
                        product.setCountDown(day + "天后更新");
                    } else if (status == 5) {
                        product.setCountDown(day + "天后变动");
                    }
                } else {
                    product.setCountDown("");
                }
            }


            return product;
        }
    }

    public static void insert(String accountId, ProductInfo product, UserInfo user, String type) {
        if (!"-1".equals(type) && checkProductLimit(accountId)) throw halt(400, "产品数量超过限制");

        val sql = "INSERT INTO tbProduct(AccountId ,shortName ,ProductName ,ProductCategoryId ,CatalogId,Brand ,Remark ,Brief ,Feature ,ProductDetail ,ProductDetail2 ,ProductArea ,Color ," +
                "GuidePrice ,PurchPrice ,SalePrice ,Unit ,Role ,IsShow ,SortNumber ,PictureUrl ,PictureScale ,ProductType,IsSharedProduct,FromAccountId ,FromProductId ,OriginAccountId,OriginProductId,status ) " +
                " values(:accountId ,:shortName ,CONCAT(:brand,' ',:shortName)  ,:categoryId ,:catalogId,:brand , :remark ,:brief ,:feature ,:productDetail ,:productDetail2 ,:productArea ,:color ," +
                ":guidePrice ,:purchPrice ,:salePrice ,:unit ,:role ,:isShow ,:sortNumber ,:pictureUrl ,:pictureScale ,:productType ,:isSharedProduct ,:fromAccountId ,:fromProductId ,:originAccountId,:originProductId,:status)";

        try (val con = db.sql2o.beginTransaction()) {
            val pid = con.createQuery(sql).bind(product)
                    .addParameter("accountId", accountId)
                    .addParameter("categoryId", product.getCategory() == null ? null : product.getCategory().getId())
                    .addParameter("catalogId", product.getCatalog() == null ? null : product.getCatalog().getId())
                    .addParameter("fromAccountId", product.getFromAccount() == null ? null : product.getFromAccount().getAccountId())
                    .addParameter("originAccountId", product.getOriginAccount() == null ? null : product.getOriginAccount().getAccountId())
                    .executeUpdate().getKey();
            product.setProductId(pid.toString());

            if (accountId.equals(product.getOriginAccount() == null ? "" : product.getOriginAccount().getAccountId())
                    && StringUtils.isEmpty(product.getFromProductId()) && product.getStatus() != 10 && product.getStatus() != 9)
                con.createQuery("UPDATE tbProduct SET OriginProductId = :pid WHERE ProductId = :pid")
                        .addParameter("pid", pid).executeUpdate();

            //保存角色进价
            if (CollectionUtil.isNotEmpty(product.getRolePurchPriceList())) {
                product.getRolePurchPriceList().forEach(rp -> {
                    val rpSql = " insert into tbRolePurchPrice(AccountId,ProductId,RoleId,PurchPrice)" +
                            "   values(:accountId,:productId,:roleId,:purchPrice)";
                    con.createQuery(rpSql)
                            .bind(rp)
                            .addParameter("accountId", accountId)
                            .addParameter("productId", product.getProductId())
                            .addParameter("roleId", rp.getUserRole().getRoleId())
                            .executeUpdate();
                });
            }
            //保存好友供货价
            if (CollectionUtil.isNotEmpty(product.getFriendSalePriceList())) {
                product.getFriendSalePriceList().forEach(sp -> {

                    val spSql = " insert into tbFriendSalePrice(AccountId,ProductId,FriendGroupId,SalePrice)" +
                            " values(:accountId,:productId,:friendGroupId,:salePrice)";

                    con.createQuery(spSql)
                            .bind(sp)
                            .addParameter("accountId", accountId)
                            .addParameter("productId", product.getProductId())
                            .addParameter("friendGroupId", sp.getFriendGroup().getGroupId())
                            .executeUpdate();
                });
            }

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
                                .executeUpdate();
                });
            }


            switch (type) {
                case "上传产品":
                    //创建 上传产品 消息
                    val uploadMessage = new Message();
                    uploadMessage.setOriginAccountId(user.getAccountId());
                    uploadMessage.setOriginUserId(user.getUserId());
                    uploadMessage.setTargetAccountId("xh");
                    uploadMessage.setType(1);
                    uploadMessage.setMessage(user.getAccountName() + "上传了产品：" + product.getProductName());
                    MessageBusiness.createMessage(con, uploadMessage);
                    break;
                case "下载产品":
                    //创建 下载产品 消息
                    val downloadMessage = new Message();
                    downloadMessage.setOriginAccountId(user.getAccountId());
                    downloadMessage.setOriginUserId(user.getUserId());
                    downloadMessage.setTargetAccountId(product.getOriginAccount().getAccountId());
                    downloadMessage.setType(3);
                    downloadMessage.setMessage(user.getAccountName() + "下载了贵公司的产品：" + product.getProductName());
                    MessageBusiness.createMessage(con, downloadMessage);
                    break;
            }

            con.commit();
        }
    }

    public static void update(String accountId, ProductInfo product) {


        val sql = "UPDATE tbProduct SET " +
                "        shortName = :shortName ," +
                "        ProductName = CONCAT(:brand,' ',:shortName) ," +
                "        ProductCategoryId = :categoryId ," +
                "        PublicCategoryId = :publicCategoryId, " +
                "        CatalogId = :catalogId ," +
                "        Brand = :brand ," +
                "        Remark = :remark ," +
                "        Brief = :brief ," +
                "        Feature = :feature," +
                "        ProductDetail =:productDetail ," +
                "        ProductDetail2 =:productDetail2 ," +
                "        ProductArea = :productArea ," +
                "        Color = :color ," +
                "        GuidePrice = :guidePrice ," +
                "        PurchPrice = :purchPrice ," +
                "        SalePrice = :salePrice ," +
                "        Unit = :unit ," +
                "        Role = :role ," +
                "        IsShow = :isShow ," +
                "        status = :status ," +
                "        SortNumber = :sortNumber ," +
                "        PictureUrl = :pictureUrl ," +
                "        PictureScale = :pictureScale " +
                " WHERE AccountId= :accountId AND ProductId = :productId ";

        try (val con = db.sql2o.beginTransaction()) {

            //删除角色进价和好友供货价
//            Arrays.asList(
//                    " DELETE FROM tbRolePurchPrice WHERE AccountId= :accountId And ProductId = :productId ; ",
//                    " DELETE FROM tbFriendSalePrice WHERE AccountId= :accountId And ProductId = :productId ; ")
//                    .forEach(s ->
//                            con.createQuery(s)
//                                    .addParameter("accountId", accountId)
//                                    .addParameter("productId", product.getProductId())
//                                    .executeUpdate()
//                    );

            // 更新商品信息
            con.createQuery(sql).bind(product)
                    .addParameter("accountId", accountId)
                    .addParameter("categoryId", product.getCategory().getId())
                    .addParameter("catalogId", product.getCatalog().getId())
                    .executeUpdate();

            //保存角色进价
//            if (CollectionUtil.isNotEmpty(product.getRolePurchPriceList())) {
//                product.getRolePurchPriceList().forEach(rp -> {
//                    val rpSql = " insert into tbRolePurchPrice(AccountId,ProductId,RoleId,PurchPrice)" +
//                            "   values(:accountId,:productId,:roleId,:purchPrice)";
//                    con.createQuery(rpSql)
//                            .bind(rp)
//                            .addParameter("accountId", accountId)
//                            .addParameter("productId", product.getProductId())
//                            .addParameter("roleId", rp.getUserRole().getRoleId())
//                            .executeUpdate();
//                });
//            }
            //保存好友供货价
//            if (CollectionUtil.isNotEmpty(product.getFriendSalePriceList())) {
//                product.getFriendSalePriceList().forEach(sp -> {
//
//                    val spSql = " insert into tbFriendSalePrice(AccountId,ProductId,FriendGroupId,SalePrice)" +
//                            " values(:accountId,:productId,:friendGroupId,:salePrice)";
//
//                    con.createQuery(spSql)
//                            .bind(sp)
//                            .addParameter("accountId", accountId)
//                            .addParameter("productId", product.getProductId())
//                            .addParameter("friendGroupId", sp.getFriendGroup().getGroupId())
//                            .executeUpdate();
//                });
//            }

            //更新已分享商品的供货价（临时）
//            val ps = con.createQuery("SELECT P.productId,P.accountId 'account.accountId',P.SalePrice FROM tbProduct P " +
//                    " WHERE P.IsSharedProduct = 1 AND P.FromAccountId = :accountId AND P.FromProductId = :productId ")
//                    .addParameter("accountId", accountId)
//                    .addParameter("productId", product.getProductId())
//                    .executeAndFetch(ProductInfo.class);
//            if (CollectionUtil.isNotEmpty(ps))
//                ps.forEach(product_temp -> {
//                    val groupId = FriendBusiness.load(accountId, product_temp.getAccount().getAccountId()).getFriendGroup().getGroupId();
//
//                    //先把PurchPrice全改为SalePrice
//                    con.createQuery("UPDATE tbProduct P SET P.PurchPrice = :purchPrice WHERE ProductId = :id ")
//                            .addParameter("purchPrice", product_temp.getSalePrice())
//                            .addParameter("id", product_temp.getProductId())
//                            .executeUpdate();
//                    if (CollectionUtil.isNotEmpty(product.getFriendSalePriceList()))
//                        product.getFriendSalePriceList().forEach(fsp -> {
//                            if (fsp.getFriendGroup().getGroupId().equals(groupId)) {
//                                con.createQuery("UPDATE tbProduct P SET P.PurchPrice = :purchPrice WHERE ProductId = :id ")
//                                        .addParameter("purchPrice", fsp.getSalePrice())
//                                        .addParameter("id", product_temp.getProductId())
//                                        .executeUpdate();
//                            }
//                        });
//                });

            con.createQuery("delete from tbProductPicture where AccountId = :accountId and ProductId = :productId")
                    .addParameter("accountId", accountId)
                    .addParameter("productId", product.getProductId())
                    .executeUpdate();

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

            //特殊属性
            con.createQuery("DELETE FROM tbproductparameter WHERE productId = :productId")
                    .addParameter("productId", product.getProductId()).executeUpdate();
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
                                .executeUpdate();
                });
            }

            con.commit();
        }
    }


    /**
     * 更新特殊产品信息
     */
    public static void updateSpecialProduct(String accountId, ProductInfo product) {

        val sql = " UPDATE tbProduct SET " +
                "        ProductType = :productType ," +
                "        MinDistance = :minDistance ," +
                "        MaxDistance = :maxDistance ," +
                "        CurtainSize = :curtainSize ," +
                "        CurtainProportion = :curtainProportion " +
                " WHERE AccountId= :accountId AND ProductId = :productId ";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(product)
                    .addParameter("accountId", accountId)
                    .executeUpdate();
        }
    }

    public static void delete(String accountId, String id) {

        try (val con = db.sql2o.beginTransaction()) {

            Arrays.asList(
                    " DELETE FROM tbProduct WHERE AccountId= :accountId AND ProductId = :productId ; ",
                    " DELETE FROM tbRolePurchPrice WHERE AccountId= :accountId And ProductId = :productId ; ",
                    " DELETE FROM tbFriendSalePrice WHERE AccountId= :accountId And ProductId = :productId ; ",
                    " DELETE FROM tbProductPicture where AccountId= :accountId And ProductId = :productId ",
                    " DELETE FROM tbProductParameter WHERE productId = :productId AND :accountId = :accountId")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("accountId", accountId)
                                    .addParameter("productId", id)
                                    .executeUpdate()
                    );

            con.commit();
        }
    }

    public static void batchDelete(String accountId, String productIds) {

        try (val con = db.sql2o.beginTransaction()) {
            productIds = productIds.replace("[", "(").replace("]", ")");
            Arrays.asList(
                    " DELETE FROM tbProduct WHERE AccountId= :accountId AND ProductId IN " + productIds,
                    " DELETE FROM tbRolePurchPrice WHERE AccountId= :accountId And ProductId IN " + productIds,
                    " DELETE FROM tbFriendSalePrice WHERE AccountId= :accountId And ProductId IN " + productIds,
                    " DELETE FROM tbProductPicture where AccountId= :accountId And ProductId IN " + productIds,
                    " DELETE FROM tbProductParameter WHERE :accountId = :accountId AND productId IN " + productIds)
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("accountId", accountId)
                                    .executeUpdate()
                    );

            con.commit();
        }
    }

    /**
     * 批量安全删除与还原
     *
     * @param accountId  校验产品数量限制
     * @param productIds
     * @param state
     */
    public static void safeDelete(String productIds, Boolean state, String accountId) {
        if (state != null && (state && checkProductLimit(accountId))) throw halt(400, "产品数量超过限制");

        val sql = " UPDATE tbProduct SET state = :state WHERE ProductId IN " + productIds.replace("[", "(").replace("]", ")");

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).addParameter("state", state == null ? false : state)
                    .executeUpdate();
        }
    }

    public static void moveCategory(String accountId, String sourceId, String targetId) {

        val sql = " UPDATE tbProduct SET ProductCategoryId = :targetId WHERE AccountId= :accountId AND ProductCategoryId = :sourceId";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("sourceId", sourceId)
                    .addParameter("targetId", targetId)
                    .executeUpdate();
        }
    }

    public static String getIdByOrigin(String accountId, String originPId) {
        val sql = "SELECT ProductId FROM tbProduct WHERE AccountId = :accountId AND OriginProductId = :originPId";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("originPId", originPId)
                    .executeScalar(String.class);
        }
    }

    /**
     * 批量移动
     *
     * @param PIds
     * @param categoryId
     */
    public static void batchMoveCategory(String PIds, String categoryId) {
        val sql = "UPDATE tbProduct SET ProductCategoryId = :targetId WHERE ProductId IN " + PIds.replace("[", "(").replace("]", ")");
        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("targetId", categoryId)
                    .executeUpdate();
        }
    }

    /**
     * 批量更新catalog
     */
    public static void batchMoveCatalog(String PIds, String catalogId) {
        val sql = "UPDATE tbProduct SET CatalogId = :targetId WHERE ProductId IN " + PIds.replace("[", "(").replace("]", ")");
        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("targetId", catalogId)
                    .executeUpdate();
        }
    }

    /**
     * 批量移动公海产品
     */
    public static void batchMovePublicCategory(String PIds, String publicCategoryId) {
        val sql = "UPDATE tbProduct SET PublicCategoryId = :targetId WHERE ProductId IN " + PIds.replace("[", "(").replace("]", ")");
        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("targetId", publicCategoryId)
                    .executeUpdate();
        }
    }

    /**
     * 获取产品详情 根据productId
     *
     * @param productId
     * @return
     */
    public static ProductInfo getProductDetail(String productId) {

        String sql = " SELECT P.ProductId ,P.ProductName ,P.shortName shortName ," +
                " P.ProductCategoryId 'category.id', C.Name 'category.name',CL.Id 'catalog.id',CL.name 'catalog.name'," +
                "(SELECT name FROM tbCatalog WHERE id = CL.parentId) 'catalog.parentName',CL.parentId 'catalog.parentId', P.Brand ," +
                " P.Remark, P.Brief ,P.Feature ,P.ProductDetail ,P.ProductDetail2 ,P.ProductArea, P.Color, P.GuidePrice ,P.PurchPrice , P.SalePrice, P.Unit, P.Role ," +
                " P.IsShow ,P.SortNumber ,P.PictureUrl ,P.PictureScale ,P.ProductType ,P.MinDistance ,P.MaxDistance ,P.CurtainSize ,P.curtainProportion ," +
                " P.IsSharedProduct ,P.FromAccountId 'fromAccount.accountId' ,P.FromProductId ,P.OriginAccountId 'originAccount.accountId' ,P.OriginProductId " +
                " FROM tbProduct P  " +
                " LEFT JOIN tbProductCategory C on P.AccountId=C.AccountId AND P.ProductCategoryId = C.Id " +
                " LEFT JOIN tbCatalog CL ON CL.Id = P.CatalogId " +
                " WHERE P.ProductId = :productId";

        try (val con = db.sql2o.open()) {
            val product = con.createQuery(sql)
                    .addParameter("productId", productId)
                    .executeAndFetchFirst(ProductInfo.class);
            if (product != null) {
                //取角色进货价
                sql = " Select RoleId 'userRole.roleId', PurchPrice From tbRolePurchPrice  " +
                        " where ProductId=:productId ";
                val rolePurchPriceList = con.createQuery(sql)
                        .addParameter("productId", productId)
                        .executeAndFetch(RolePurchPriceInfo.class);

                product.setRolePurchPriceList(rolePurchPriceList);

                //取好友供货价
                sql = " Select FriendGroupId 'friendGroup.groupId',SalePrice From tbFriendSalePrice" +
                        " where  ProductId=:productId ";
                val friendSalePriceList = con.createQuery(sql)
                        .addParameter("productId", productId)
                        .executeAndFetch(FriendSalePriceInfo.class);

                product.setFriendSalePriceList(friendSalePriceList);

                //取轮播图
                sql = " Select SortNumber ,PictureUrl From tbProductPicture Where  ProductId=:productId ORDER BY SortNumber";
                val productPictureList = con.createQuery(sql)
                        .addParameter("productId", productId)
                        .executeAndFetch(ProductPictureInfo.class);

                product.setPictureList(productPictureList);

                //取特殊属性（按产品关联的分类id去取分类特殊属性）
                sql = "SELECT " +
                        " ParameterName , " +
                        " ParameterType , " +
                        " ParameterOptions , " +
                        " ParameterValue, SortNum FROM tbProductParameter " +
                        " WHERE productId = :productId  ORDER BY SortNum;";
                val catalogParameterList = con.createQuery(sql)
                        .addParameter("productId", productId)
                        .executeAndFetch(ProductParameterInfo.class);
                product.setProductParameters(catalogParameterList);
            }

            return product;
        }


    }

    public static List<ProductInfo> getToDelList() {
        val sql = "SELECT ProductId,AccountId from tbProduct WHERE state = 0 AND DATE_ADD(UpdateTime,INTERVAL 7 day) <= NOW()";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(ProductInfo.class);
        }
    }

    public static void importExcel(String accountId, List<ProductInfo> list) {
        val sql = "INSERT INTO tbproduct (AccountId,shortName,`ProductName`,`Brand`,`ProductArea`,`Color`, `GuidePrice`,`PurchPrice`, `SalePrice`, `Unit`, `Role`, `Remark`, `Brief`, `Feature`, `IsShow`, `SortNumber`, `ProductType`,`OriginAccountId`,`status`) " +
                " SELECT :accountId,:shortName,concat(:brand,' ',:shortName),:brand,:productArea,:color,:guidePrice,ifNull(:purchPrice,:guidePrice),ifNull(:salePrice,:guidePrice),:unit,:role,:remark,:brief,:feature,1,1,'Default',:accountId,IFNULL(:status,1) FROM DUAL " +
                " WHERE NOT EXISTS (SELECT 1 FROM tbproduct WHERE shortName = :shortName AND Brand = :brand AND AccountId = :accountId AND State = 1) ";//重复不插入

        try (val con = db.sql2o.beginTransaction()) {
            Query query = con.createQuery(sql);
            list.forEach(product -> query.bind(product).addParameter("accountId", accountId).addToBatch());

            val keys = query.executeBatch().getKeys();
            if (keys != null && keys.length != 0)
                con.createQuery("UPDATE tbProduct SET OriginProductId = ProductId WHERE ProductId IN (" +
                        Arrays.toString(keys).replace("[", "").replace("]", "") + ")")
                        .executeUpdate();

            con.commit();
        }
    }

    /**
     * true 超过限制 / false 没超过限制
     * 企业产品数量限制判断
     */
    public static Boolean checkProductLimit(String accountId) {
        val sql = "SELECT COUNT(P.ProductId) >= IFNULL(A.ProductLimit,0) value FROM tbAccount A " +
                " LEFT JOIN tbproduct P ON A.AccountId = P.AccountId " +
                " WHERE A.AccountId = :accountId AND P.state = 1 AND P.status NOT IN (9,10)";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetchFirst(Boolean.class);
        }
    }

    public static void updateSortNum(List<ProductInfo> list) {
        val sql = "UPDATE tbProduct SET " +
                " SortNumber = :sortNumber " +
                " WHERE ProductId = :productId ";
        try (val con = db.sql2o.beginTransaction()) {
            val query = con.createQuery(sql);
            for (ProductInfo p : list) {
                query.bind(p).addToBatch();
            }
            query.executeBatch();
            con.commit();
        }
    }

    public static void inExclusive(String productId, Boolean isInExclusive) {
        val sql = "UPDATE tbProduct SET " +
                " isInExclusive = :isInExclusive " +
                " WHERE ProductId = :productId ";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("productId", productId)
                    .addParameter("isInExclusive", isInExclusive)
                    .executeUpdate();
        }
    }

    public static List<ProductInfo> findAllInExclusive(String accountId) {
        val sql = "select tbProduct " +
                "from tbProduct " +
                " WHERE accountId = :accountId " +
                "and isInExclusive = true";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(ProductInfo.class);
        }
    }

    public static ResponseWrapper find(Map conditions) {

        boolean isPub = "public".equals(conditions.get("fromPage"));

        val salePrice = isPub ? "IFNULL(FSP.SalePrice,P.SalePrice)" : "P.SalePrice";
        var sortBy = conditions.get("sortBy") == null ? "sortNumber" : conditions.get("sortBy");

        val list_sql = " SELECT P.ProductId ,P.ProductName ,P.shortName shortName ,P.isInExclusive," +
                " P.ProductCategoryId 'category.id', P.CatalogId 'catalog.Id', CL.Name 'catalog.name', P.Brand ," +
                " P.Remark, P.ProductArea, P.Color ,P.GuidePrice ,P.PurchPrice , " + salePrice + " SalePrice, P.Unit, P.Role ,P.IsShow ,P.SortNumber ,P.PictureUrl ,P.status," +
                " P.ProductType ,P.MinDistance ,P.MaxDistance ,P.CurtainSize ,P.CurtainProportion,A.AccountId 'account.accountId',A.AccountName 'account.accountName'," +
                " IFNULL((SELECT 1 FROM tbProduct WHERE AccountId = :toAccountIdAll AND State = 1 AND (FromProductId = P.ProductId OR OriginProductId = P.OriginProductId)  LIMIT 1),0) isAlreadyReceive " +
                " FROM tbProduct P " +
                " LEFT JOIN tbCatalog CL ON P.CatalogId = CL.Id " +
                " LEFT JOIN tbAccount A on P.AccountId=A.AccountId" +
                " WHERE state = :state " +
                "  and  (P.ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                "  and  (P.Brand LIKE CONCAT('%',:brand,'%') OR :brand is null) " +
                "  and  (P.Role LIKE CONCAT('%',:role,'%') OR :role is null) " +
                "  and  (P.isInExclusive = :isInExclusive OR :isInExclusive is null) " +
                "  and  (P.ProductId LIKE CONCAT('%',:all,'%') OR P.ProductName LIKE CONCAT('%',:all,'%') OR P.Brand LIKE CONCAT('%',:all,'%') OR P.Role LIKE CONCAT('%',:all,'%') OR :all is null) " +
                "  and  ( exists ( select 1 from ProductShare S where P.AccountId = S.AccountId and P.ProductId = S.ProductId and S.ToAccountId = :toAccountIdAll) OR :toAccountIdAll is null )" +
                " Order By P." + sortBy +
                " LIMIT :PAGEOFFSET ,:PAGESIZE";


        val count_sql = " SELECT count(1) xcount FROM tbProduct P " +
                " WHERE state = :state " +
                "  and  (P.ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                "  and  (P.Brand LIKE CONCAT('%',:brand,'%') OR :brand is null) " +
                "  and  (P.isInExclusive = :isInExclusive OR :isInExclusive is null) " +
                "  and  (P.Role LIKE CONCAT('%',:role,'%') OR :role is null) " +
                "  and  (P.ProductId LIKE CONCAT('%',:all,'%') OR P.ProductName LIKE CONCAT('%',:all,'%') OR P.Brand LIKE CONCAT('%',:all,'%') OR P.Role LIKE CONCAT('%',:all,'%') OR :all is null) " +
                "  and  ( exists ( select 1 from ProductShare S where P.AccountId = S.AccountId and P.ProductId = S.ProductId and S.ToAccountId = :toAccountIdAll) OR :toAccountIdAll is null )";

        try (val con = db.sql2o.open()) {
            val list = con.createQuery(list_sql)
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("productName", conditions.get("productName"))
                    .addParameter("toAccountIdAll", conditions.get("toAccountIdAll"))
                    .addParameter("brand", conditions.get("brand"))
                    .addParameter("role", conditions.get("role"))
                    .addParameter("isInExclusive", conditions.get("isInExclusive"))
                    .addParameter("all", conditions.get("all"))
                    .addParameter("state", conditions.get("state") == null ? 1 : conditions.get("state"))
                    .executeAndFetch(ProductInfo.class);

            val count = con.createQuery(count_sql)
                    .addParameter("productName", conditions.get("productName"))
                    .addParameter("toAccountIdAll", conditions.get("toAccountIdAll"))
                    .addParameter("brand", conditions.get("brand"))
                    .addParameter("role", conditions.get("role"))
                    .addParameter("isInExclusive", conditions.get("isInExclusive"))
                    .addParameter("all", conditions.get("all"))
                    .addParameter("state", conditions.get("state") == null ? 1 : conditions.get("state"))
                    .executeScalar(int.class);

            return ResponseWrapper.page(count, list);
        }
    }
}