package com.forcpacebj.api.business;

import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.AccountInfo;
import com.forcpacebj.api.entity.ProductInfo;
import com.forcpacebj.api.entity.ProductRecordInfo;
import com.forcpacebj.api.entity.UserInfo;
import lombok.val;

import java.util.List;
import java.util.Map;

/**
 * Created by pc on 2019/11/14.
 */
public class ProductRecordBusiness {

    public static List<ProductRecordInfo> find(Map conditions) {

        val sql = "SELECT PR.Id,PR.TYPE,PR.AuditStatus,PR.UpdateTime,PR.CreateTime,PR.USERID 'user.userId',U.USERNAME 'user.userName'," +
                " A.AccountName 'account.accountName',PR.AccountId 'account.accountId', " +
                " P.OriginAccountId 'originAccount.accountId',fromA.AccountName 'originAccount.accountName', " +
                " PR.ProductId 'product.productId',P.brand 'product.brand',PR.ProductName 'product.productName', " +
                " P.catalogId 'product.catalog.id',C.Name 'product.catalog.name' FROM productRecord PR " +
                " INNER JOIN tbUser U ON U.UserId = PR.USERID " +
                " LEFT JOIN tbAccount A ON A.AccountId = PR.AccountId " +
                " LEFT JOIN tbProduct P ON PR.ProductId = P.ProductId " +
                " LEFT JOIN tbAccount fromA ON P.OriginAccountId = fromA.AccountId " +
                " LEFT JOIN tbCatalog C ON C.Id = P.CatalogId " +
                " WHERE (PR.AccountId = :accountId OR :accountId IS NULL) " +
                " AND (PR.ProductId = :productId OR :productId IS NULL) " +
                "  and  (PR.ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                "  and  (A.AccountName LIKE CONCAT('%',:accountName,'%') OR :accountName is null) " +
                " AND (PR.Type = :type OR :type IS NULL) " +
                " AND (PR.AuditStatus = :auditStatus OR :auditStatus IS NULL ) " +
                " Order By PR.UpdateTime DESC " +
                " LIMIT :PAGEOFFSET ,:PAGESIZE ";


        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("accountName", conditions.get("accountName"))
                    .addParameter("productId", conditions.get("productId"))
                    .addParameter("productName", conditions.get("productName"))
                    .addParameter("type", conditions.get("type"))
                    .addParameter("auditStatus", conditions.get("auditStatus"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch(ProductRecordInfo.class);
        }

    }

    public static Integer count(Map conditions) {

        val sql = "SELECT COUNT(1) xcount FROM productrecord PR " +
                " INNER JOIN tbUser U ON U.UserId = PR.USERID " +
                " LEFT JOIN tbAccount A ON A.AccountId = PR.AccountId " +
                " LEFT JOIN tbProduct P ON PR.ProductId = P.ProductId " +
                " LEFT JOIN tbAccount fromA ON P.OriginAccountId = fromA.AccountId " +
                " WHERE (PR.AccountId = :accountId OR :accountId IS NULL) " +
                "  and  (PR.ProductName LIKE CONCAT('%',:productName,'%') OR :productName is null) " +
                "  and  (A.AccountName LIKE CONCAT('%',:accountName,'%') OR :accountName is null) " +
                " AND (PR.ProductId = :productId OR :productId IS NULL) " +
                " AND (PR.Type = :type OR :type IS NULL) " +
                " AND (PR.AuditStatus = :auditStatus OR :auditStatus IS NULL ) ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("accountName", conditions.get("accountName"))
                    .addParameter("productId", conditions.get("productId"))
                    .addParameter("productName", conditions.get("productName"))
                    .addParameter("type", conditions.get("type"))
                    .addParameter("auditStatus", conditions.get("auditStatus"))
                    .executeScalar(Integer.class);
        }

    }

    public static ProductRecordInfo getInfo() {
        val sql = "SELECT ApproverId,ApproverName,CreateTime FROM ProductRecord " +
                " WHERE TYPE = 0 ORDER BY createTime DESC ";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .executeAndFetchFirst(ProductRecordInfo.class);
        }
    }

    public static void insert(ProductRecordInfo productRecord) {
        val sql = "INSERT INTO productRecord( UserId, fromAccountId, AccountId, fromProductId, ProductId, ProductName, Type, AuditStatus, ApproverId, ApproverName, CreateTime) " +
                " VALUES ( :userId, :fromAccountId, :accountId, :fromProductId, :productId, :productName, :type, :auditStatus, :approverId, :approverName, NOW());";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(productRecord)
                    .addParameter("userId", productRecord.getUser() == null ? null : productRecord.getUser().getUserId())
                    .addParameter("accountId", productRecord.getAccount() == null ? null : productRecord.getAccount().getAccountId())
                    .addParameter("productId", productRecord.getProduct() == null ? null : productRecord.getProduct().getProductId())
                    .addParameter("productName", productRecord.getProduct() == null ? null : productRecord.getProduct().getProductName())
                    .executeUpdate();
        }
    }

    public static void update(String productId, Integer auditStatus, UserInfo user) {
        val sql = "UPDATE productRecord SET AuditStatus = :auditStatus, " +
                " ApproverId = :approverId," +
                " ApproverName =  :approverName, " +
                " UpdateTime = NOW() " +
                " WHERE ProductId = :productId AND Type = 0 AND AuditStatus = 0 AND ApproverId IS NULL";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("auditStatus", auditStatus)
                    .addParameter("approverId", user.getUserId())
                    .addParameter("approverName", user.getUserName())
                    .addParameter("productId", productId)
                    .executeUpdate();
        }
    }

    public static ResponseWrapper accountBeDownloadedCount(Map conditions) {
        val sql = "SELECT A.AccountId, A.AccountName ,Count(PR.ProductId) downloadedCount FROM ProductRecord PR " +
                " INNER JOIN tbAccount A ON A.AccountId = PR.AccountId  " +
                " WHERE PR.Type = 1 " +
                " AND (PR.FromAccountId = :accountId OR :accountId IS NULL) " +
                " AND (PR.FromProductId = :productId OR :productId IS NULL) " +
                " GROUP BY A.AccountId " +
                " ORDER BY Count(PR.ProductId) DESC " +
                " LIMIT :PAGEOFFSET ,:PAGESIZE ";

        val countSql = "SELECT Count(A.AccountId) FROM ProductRecord PR " +
                " INNER JOIN tbAccount A ON A.AccountId = PR.AccountId  " +
                " WHERE PR.Type = 1 " +
                " AND (PR.FromAccountId = :accountId OR :accountId IS NULL) " +
                " AND (PR.FromProductId = :productId OR :productId IS NULL) ";
        try (val con = db.sql2o.open()) {
            List<AccountInfo> accountList = con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("productId", conditions.get("productId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch(AccountInfo.class);
            Integer count = con.createQuery(countSql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("productId", conditions.get("productId"))
                    .executeScalar(Integer.class);
            return ResponseWrapper.page(count, accountList);
        }
    }

    public static ResponseWrapper accountDownloadDetail(Map conditions) {
        val sql = " SELECT PR.FromAccountId,A.AccountName fromAccountName,PR.FromProductId,PR.ProductId 'product.productId',PR.ProductName 'product.productName',PR.CreateTime FROM ProductRecord PR " +
                " LEFT JOIN tbProduct P ON P.ProductId = PR.ProductId " +
                " INNER JOIN tbAccount A ON A.AccountId = PR.FromAccountId " +
                " WHERE PR.Type = 1 AND PR.AccountId = :accountId " +
                " ORDER BY PR.CreateTime DESC " +
                " LIMIT :PAGEOFFSET ,:PAGESIZE ";

        val countSql = " SELECT count(PR.ProductId) FROM ProductRecord PR " +
                " LEFT JOIN tbProduct P ON P.ProductId = PR.ProductId " +
                " INNER JOIN tbAccount A ON A.AccountId = PR.FromAccountId " +
                " WHERE PR.Type = 1 AND PR.AccountId = :accountId ";
        try (val con = db.sql2o.open()) {
            List<ProductRecordInfo> productRecordList = con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch(ProductRecordInfo.class);
            Integer count = con.createQuery(countSql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .executeScalar(Integer.class);
            return ResponseWrapper.page(count, productRecordList);
        }
    }

    public static AccountInfo productData(String accountId) {
        val sql = "SELECT " +
                "(SELECT COUNT(1) FROM tbProduct WHERE `status` = 1 AND AccountId = :accountId) AS selfBuiltCount, " +
                "(SELECT COUNT(1) FROM tbProduct WHERE `status` = 5 AND AccountId = :accountId) AS publicCount, " +
                "(SELECT COUNT(1) FROM tbProduct WHERE `status` NOT IN (5,8,9,10) AND AccountId = :accountId) AS unPublicCount, " +
                "(SELECT COUNT(1) FROM ProductRecord PR " +
                "WHERE PR.Type = 1 AND PR.FromAccountId = :accountId) AS downloadedCount, " +
                "(SELECT COUNT(1) FROM tbProduct P " +
                "WHERE P.AccountId = :accountId AND NOT EXISTS (SELECT 1 FROM ProductRecord WHERE FromAccountId = :accountId AND Type = 1 AND ProductId = P.ProductId ) ) AS unDownloadedCount";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetchFirst(AccountInfo.class);
        }
    }

    public static ResponseWrapper downloadedProductDetail(Map conditions) {
        val list = "SELECT P.ProductId ,p.ProductName,count(PR.FromProductId) downloadedCount From tbProduct P " +
                " LEFT JOIN ProductRecord PR ON P.ProductId = PR.FromProductId AND PR.type = 1 " +
                " WHERE P.State = 1 AND P.AccountId = :accountId " +
                " GROUP BY P.ProductId " +
                " ORDER BY downloadedCount DESC,P.CreateTime DESC" +
                " LIMIT :PAGEOFFSET ,:PAGESIZE ";

        val count = "SELECT count(1) From tbProduct P " +
                " WHERE P.State = 1 AND P.AccountId = :accountId ";

        try (val con = db.sql2o.open()) {
            List<ProductInfo> productList = con.createQuery(list)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch(ProductInfo.class);

            Integer productCount = con.createQuery(count)
                    .addParameter("accountId", conditions.get("accountId"))
                    .executeScalar(Integer.class);
            return ResponseWrapper.page(productCount, productList);
        }
    }

}
