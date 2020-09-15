package com.forcpacebj.api.business;

import com.alibaba.fastjson.JSONArray;
import com.forcpacebj.api.entity.ProductShareInfo;
import lombok.val;
import org.sql2o.Query;

import java.util.Map;


/**
 * Created by pc on 2020/4/23.
 */
public class ProductShareBusiness {

    public static ProductShareInfo load(String accountId, String productId, String toAccountId) {

        val sql = " Select Id, AccountId, SalePrice From ProductShare" +
                " where (AccountId=:accountId or :accountId IS NULL) " +
                " And ProductId=:productId And ToAccountId =:toAccountId";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("productId", productId)
                    .addParameter("toAccountId", toAccountId)
                    .executeAndFetchFirst(ProductShareInfo.class);
        }
    }

    public static void insert(Map data) {

        val productIds = JSONArray.parseArray(data.get("productIds").toString(), Integer.class);
        val toAccountIds = JSONArray.parseArray(data.get("toAccountIds").toString(), String.class);
        val spSql = "insert into ProductShare(AccountId,ProductId,ToAccountId)" +
                " values(:accountId,:productId,:toAccountId)";
        try (val con = db.sql2o.beginTransaction()) {
            Query query = con.createQuery(spSql);
            toAccountIds.forEach(toAccountId ->
                    productIds.forEach(productId ->
                            query.addParameter("accountId", data.get("accountId"))
                                    .addParameter("toAccountId", toAccountId)
                                    .addParameter("productId", productId)
                                    .addToBatch()
                    )
            );
            query.executeBatch();
            con.commit();
        }
    }

    public static void update(ProductShareInfo priceInfo) {

        val spSql = "update ProductShare set SalePrice = :salePrice,IsReceived = :isReceived where id = :id ";
        try (val con = db.sql2o.open()) {
            con.createQuery(spSql).bind(priceInfo).executeUpdate();
        }
    }

    public static void batchDelete(String ids) {
        ids = ids.replace("[", "(").replace("]", ")");
        val deleteSql = "DELETE FROM ProductShare WHERE Id IN " + ids;
        try (val con = db.sql2o.open()) {
            con.createQuery(deleteSql).executeUpdate();
        }
    }
}
