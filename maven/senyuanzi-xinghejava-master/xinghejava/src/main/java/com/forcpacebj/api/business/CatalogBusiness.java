package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.CatalogInfo;
import lombok.val;

import java.util.List;
import java.util.Map;

/**
 * Created by pc on 2019/10/9.
 * 类别
 */
public class CatalogBusiness {

    public static List<CatalogInfo> list(String parentId) {

        val sql = " SELECT Id ,Name ,ParentId ,SortNum FROM tbCatalog Where " +
                " CASE WHEN :parentId IS NULL THEN ParentId IS NULL ELSE ParentId = :parentId END Order by SortNum";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("parentId", parentId)
                    .executeAndFetch(CatalogInfo.class);
        }
    }

    public static List<CatalogInfo> allList() {

        val sql = " SELECT Id ,Name ,ParentId ,SortNum FROM tbCatalog Order by SortNum";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(CatalogInfo.class);
        }
    }


    public static List<CatalogInfo> effectiveList(Map conditions) {

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

        val sql = conditions.get("parentId") == null ? " SELECT C2.Id ,C2.Name , :parentId ParentId ,C2.SortNum FROM tbCatalog C " +
                "INNER JOIN tbproduct P ON P.CatalogId = C.ID AND (P.ProductCategoryId IN " + category + " OR :categoryId IS NULL) AND P.AccountId = :accountId AND P.Status NOT IN (9,10) " +
                "INNER JOIN tbCatalog C2 ON C.ParentId = C2.ID " +
                "GROUP BY C2.ID ORDER BY C2.SortNum ;"
                :
                "SELECT C.Id,C.Name,C.ParentId,c.SortNum FROM tbCatalog C " +
                        "INNER JOIN tbproduct P ON P.CatalogId = C.ID AND (P.ProductCategoryId IN " + category + " OR :categoryId IS NULL) AND P.AccountId = :accountId AND P.Status NOT IN (9,10) " +
                        "INNER JOIN tbCatalog C2 ON C.ParentId = C2.ID AND C2.ID = :parentId " +
                        "GROUP BY C.ID ORDER BY C.SortNum ;";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("parentId", conditions.get("parentId"))
                    .addParameter("categoryId", conditions.get("categoryId"))
                    .addParameter("accountId", conditions.get("accountId"))
                    .executeAndFetch(CatalogInfo.class);
        }
    }

    public static List<CatalogInfo> publicEffectiveList(Map conditions) {

        val category = "(SELECT Id FROM tbProductCategory WHERE AccountId = 'Public' AND RelatedAccountId = :relatedAccountId AND Id = ':categoryId' " +
                "UNION " +
                "SELECT Id FROM tbProductCategory WHERE AccountId = 'Public' AND RelatedAccountId = :relatedAccountId AND ParentId = ':categoryId' " +
                "UNION " +
                "SELECT Id FROM tbProductCategory WHERE AccountId = 'Public' AND RelatedAccountId = :relatedAccountId AND ParentId IN ( " +
                " SELECT Id FROM tbProductCategory WHERE AccountId = 'Public' AND RelatedAccountId = :relatedAccountId AND ParentId = ':categoryId' " +
                "))";

        String status = null;
        switch ((String) conditions.get("fromPage")) {
            case "private":
                status = " NOT IN (1,8,9,10) ";
                break;
            case "public":
                status = " = 5 ";
                break;
        }

        val sql = conditions.get("parentId") == null ? " SELECT C2.Id ,C2.Name , :parentId ParentId ,C2.SortNum FROM tbCatalog C " +
                "INNER JOIN tbProduct P ON P.CatalogId = C.ID AND (P.PublicCategoryId IN " + category + " OR :categoryId IS NULL) AND P.AccountId = :relatedAccountId AND P.Status " + status +
                "INNER JOIN tbCatalog C2 ON C.ParentId = C2.ID " +
                "GROUP BY C2.ID ORDER BY C2.SortNum ;"
                :
                "SELECT C.Id,C.Name,C.ParentId,c.SortNum FROM tbCatalog C " +
                        "INNER JOIN tbProduct P ON P.CatalogId = C.ID AND (P.PublicCategoryId IN " + category + " OR :categoryId IS NULL) AND P.AccountId = :relatedAccountId AND P.Status " + status +
                        "INNER JOIN tbCatalog C2 ON C.ParentId = C2.ID AND C2.ID = :parentId " +
                        "GROUP BY C.ID ORDER BY C.SortNum ;";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("parentId", conditions.get("parentId"))
                    .addParameter("categoryId", conditions.get("categoryId"))
                    .addParameter("relatedAccountId", conditions.get("relatedAccountId"))
                    .executeAndFetch(CatalogInfo.class);
        }
    }

    public static List<CatalogInfo> listExistProduct(String parentId) {

        val sql = " SELECT Id ,Name ,ParentId ,SortNum FROM tbCatalog Where " +
                " CASE WHEN :parentId IS NULL THEN ParentId IS NULL ELSE ParentId = :parentId END Order by SortNum";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("parentId", parentId)
                    .executeAndFetch(CatalogInfo.class);
        }
    }

    public static String insert(CatalogInfo catalogInfo) {

        val sql = "INSERT INTO tbCatalog(Name ,ParentId ,SortNum) " +
                " values (:name ,:parentId ,:sortNum)";

        try (val con = db.sql2o.open()) {
            if (catalogInfo.getSortNum() == null)
                catalogInfo.setSortNum(con.createQuery("SELECT IFNULL(MAX(SortNum)+1,1) FROM tbCatalog Where  " +
                        "  CASE WHEN :parentId IS NULL THEN ParentId IS NULL ELSE ParentId = :parentId END")
                        .addParameter("parentId", catalogInfo.getParentId()).executeScalar(Integer.class));

            return con.createQuery(sql).bind(catalogInfo)
                    .executeUpdate().getKey(String.class);
        }
    }

    public static void update(List<CatalogInfo> catalogInfo) {

        val sql = "UPDATE tbCatalog SET " +
                "        Name = :name ," +
                "        ParentId = :parentId ," +
                "        SortNum = :sortNum" +
                " WHERE Id = :id ";

        try (val con = db.sql2o.beginTransaction()) {
            val query = con.createQuery(sql);
            for (CatalogInfo catalog : catalogInfo) {
                query.bind(catalog).addToBatch();
            }
            query.executeBatch();
            con.commit();
        }
    }

    public static void delete(String id) {

        val sql = " DELETE FROM tbCatalog WHERE id = :id";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
        }
    }
}
