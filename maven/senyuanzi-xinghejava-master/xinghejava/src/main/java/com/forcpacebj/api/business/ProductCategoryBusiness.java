package com.forcpacebj.api.business;

import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.entity.ProductCategoryInfo;
import lombok.val;
import org.sql2o.Query;

import java.util.List;

public class ProductCategoryBusiness {

    public static List<ProductCategoryInfo> associateList(String accountId) {
        val sql = " SELECT PC.Id ,PC.Name ,PC.ParentId ,PC.AccountId,PC.RelatedAccountId,A.AccountName RelatedAccountName,PC.SortNumber FROM tbProductCategory PC " +
                " LEFT JOIN tbAccount A ON A.AccountId = PC.RelatedAccountId Where PC.RelatedAccountId = :accountId AND PC.AccountId = 'Public' " +
                " UNION ALL SELECT 'TEMP' Id,'收集箱' Name,'' ParentId ,:accountId AccountId,null RelatedAccountId ,null RelatedAccountName,-1 SortNumber " +
                " Order by SortNumber";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(ProductCategoryInfo.class);
        }
    }

    public static List<ProductCategoryInfo> list(String accountId) {

        val sql = " SELECT Id ,Name ,ParentId ,SortNumber FROM tbProductCategory Where AccountId= :accountId Order by SortNumber";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(ProductCategoryInfo.class);
        }
    }

    public static List<ProductCategoryInfo> tList(String accountId) {

        val sql = " SELECT Id ,Name ,ParentId ,AccountId ,SortNumber FROM tbProductCategory Where AccountId= :accountId " +
                " UNION ALL SELECT 'TEMP' Id,'" + (StaticParam.PUBLIC_PRODUCT.equals(accountId) ? "收集箱" : "临时仓库") + "' Name,'' ParentId ,:accountId AccountId,0 SortNumber " +
                " Order by SortNumber";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(ProductCategoryInfo.class);
        }
    }

    public static ProductCategoryInfo getPublicCategory(String accountId) {

        val sql = "SELECT PC.Id ,PC.Name ,PC.ParentId ,PC.AccountId,PC.RelatedAccountId,PC.SortNumber FROM tbProductCategory PC " +
                " WHERE PC.accountId = 'Public' AND PC.RelatedAccountId = :accountId AND PC.ParentId = '' ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql).addParameter("accountId", accountId).executeAndFetchFirst(ProductCategoryInfo.class);
        }
    }

    public static void insert(String accountId, ProductCategoryInfo productCategory) {

        val sql = "INSERT INTO tbProductCategory(AccountId ,Id ,Name ,ParentId ,RelatedAccountId,SortNumber) " +
                " values (:accountId ,:id ,:name ,:parentId ,:relatedAccountId,:sortNumber)";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(productCategory)
                    .addParameter("accountId", productCategory.getAccountId() == null ? accountId : productCategory.getAccountId())
                    .executeUpdate();
        }
    }

    public static void update(String accountId, ProductCategoryInfo productCategory) {

        val sql = "UPDATE tbProductCategory SET " +
                "        Name = :name ," +
                "        ParentId = :parentId ," +
                "        SortNumber = :sortNumber ," +
                "        RelatedAccountId = :relatedAccountId" +
                " WHERE AccountId= :accountId AND Id = :id ";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(productCategory)
                    .addParameter("accountId", productCategory.getAccountId() == null ? accountId : productCategory.getAccountId())
                    .executeUpdate();
        }
    }

    public static void update(String accountId, List<ProductCategoryInfo> categories) {

        val sql = "UPDATE tbProductCategory SET " +
                "        Name = :name ," +
                "        ParentId = :parentId ," +
                "        SortNumber = :sortNumber ," +
                "        RelatedAccountId = :relatedAccountId" +
                " WHERE AccountId= :accountId AND Id = :id ";

        try (val con = db.sql2o.beginTransaction()) {
            Query query = con.createQuery(sql);
            for (ProductCategoryInfo productCategory : categories) {
                query.bind(productCategory)
                        .addParameter("accountId", productCategory.getAccountId() == null ? accountId : productCategory.getAccountId())
                        .addToBatch();
            }
            query.executeBatch();
            con.commit();
        }
    }

    public static void delete(String accountId, String id) {

        val sql = " DELETE FROM tbProductCategory WHERE AccountId= :accountId AND id = :id";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeUpdate();
        }
    }
}