package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.ProductSetCategoryInfo;
import lombok.val;

import java.util.List;

public class ProductSetCategoryBusiness {

    public static List<ProductSetCategoryInfo> list(String accountId) {

        val sql = " SELECT Id ,Name ,ParentId ,SortNumber FROM tbProductSetCategory Where AccountId= :accountId " +
                " UNION ALL SELECT NULL Id,'默认套餐分类' Name,'' ParentId ,0 SortNumber " +
                " Order by SortNumber";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(ProductSetCategoryInfo.class);
        }
    }

    public static void insert(String accountId, ProductSetCategoryInfo productSetCategory) {

        val sql = "INSERT INTO tbProductSetCategory(AccountId ,Id ,Name ,ParentId ,SortNumber) " +
                " values (:accountId ,:id ,:name ,:parentId ,:sortNumber)";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(productSetCategory)
                    .addParameter("accountId", accountId)
                    .executeUpdate();
        }
    }

    public static void update(String accountId, ProductSetCategoryInfo productSetCategory) {

        val sql = "UPDATE tbProductSetCategory SET " +
                "        Name = :name ," +
                "        ParentId = :parentId ," +
                "        SortNumber = :sortNumber" +
                " WHERE AccountId= :accountId AND Id = :id ";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(productSetCategory)
                    .addParameter("accountId", accountId)
                    .executeUpdate();
        }
    }

    public static void delete(String accountId, String id) {

        val sql = " DELETE FROM tbProductSetCategory WHERE AccountId= :accountId AND id = :id";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeUpdate();
        }
    }
}