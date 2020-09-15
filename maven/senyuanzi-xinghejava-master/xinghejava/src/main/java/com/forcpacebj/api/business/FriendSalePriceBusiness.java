package com.forcpacebj.api.business;

import com.alibaba.fastjson.JSON;
import com.forcpacebj.api.entity.FriendSalePriceInfo;
import com.forcpacebj.api.entity.ProductInfo;
import com.forcpacebj.api.utils.CollectionUtil;
import lombok.val;
import org.sql2o.Query;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static spark.Spark.halt;

public class FriendSalePriceBusiness {

    public static FriendSalePriceInfo load(String accountId, String productId, String groupId) {

        val sql = " Select FriendGroupId 'friendGroup.groupId', SalePrice From tbFriendSalePrice" +
                " where AccountId=:accountId And ProductId=:productId And FriendGroupId =:groupId";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("productId", productId)
                    .addParameter("groupId", groupId)
                    .executeAndFetchFirst(FriendSalePriceInfo.class);
        }
    }

    public static void insert(String accountId, String productId, String groupId, BigDecimal salePrice) {

        val spSql = "insert into tbFriendSalePrice(AccountId,ProductId,FriendGroupId,SalePrice)" +
                " values(:accountId,:productId,:friendGroupId,:salePrice)";
        try (val con = db.sql2o.open()) {
            con.createQuery(spSql)
                    .addParameter("accountId", accountId)
                    .addParameter("productId", productId)
                    .addParameter("friendGroupId", groupId)
                    .addParameter("salePrice", salePrice)
                    .executeUpdate();
        }
    }

    public static void update(FriendSalePriceInfo priceInfo) {

        val spSql = "update tbFriendSalePrice set SalePrice = :salePrice" +
                " where id = :id ";
        try (val con = db.sql2o.open()) {
            con.createQuery(spSql).bind(priceInfo).executeUpdate();
        }
    }

    @SuppressWarnings("unchecked")
    public static void batchInsertOrUpdate(Map<String, BigDecimal> friendProductSales, String accountId, String friendGroupId) {
        if (CollectionUtil.isEmpty(friendProductSales)) throw halt(400, "未选中产品");

        val updateSql = "update tbFriendSalePrice set SalePrice = :salePrice where id = :id ";

        val insertSql = "insert into tbFriendSalePrice(AccountId,ProductId,FriendGroupId,SalePrice)" +
                " values(:accountId,:productId,:friendGroupId,:salePrice)";

        Set<String> productIds = new HashSet(friendProductSales.keySet());
        val selectSql = " Select Id, FriendGroupId 'friendGroup.groupId', SalePrice,ProductId From tbFriendSalePrice" +
                " where AccountId = :accountId AND FriendGroupId = :friendGroupId AND ProductId IN "
                + productIds.toString().replace("[", "(").replace("]", ")");


        try (val con = db.sql2o.beginTransaction()) {
            List<FriendSalePriceInfo> friendSalePrices = con.createQuery(selectSql)
                    .addParameter("accountId", accountId)
                    .addParameter("friendGroupId", friendGroupId)
                    .executeAndFetch(FriendSalePriceInfo.class);


            if (CollectionUtil.isNotEmpty(friendSalePrices)) {
                Query updateQuery = con.createQuery(updateSql);
                friendSalePrices.forEach(fsp -> {
                    if (CollectionUtil.isNotEmpty(productIds)) {
                        Set<String> temp = new HashSet();
                        productIds.forEach(pid -> {
                            if (fsp.getProductId().equals(pid)) {
                                temp.add(pid);
                                updateQuery.addParameter("id", fsp.getId())
                                        .addParameter("salePrice", friendProductSales.get(pid))
                                        .addToBatch();
                            }
                        });
                        productIds.removeAll(temp);
                    }
                });
                updateQuery.executeBatch();
            }
            if (CollectionUtil.isNotEmpty(productIds)) {
                Query insertQuery = con.createQuery(insertSql);
                productIds.forEach(pid -> {
                    insertQuery.addParameter("accountId", accountId)
                            .addParameter("productId", pid)
                            .addParameter("friendGroupId", friendGroupId)
                            .addParameter("salePrice", friendProductSales.get(pid))
                            .addToBatch();
                });
                insertQuery.executeBatch();
            }
            con.commit();
        }
    }

    public static void delete(String id) {

        val spSql = "delete from tbFriendSalePrice where id = :id ";
        try (val con = db.sql2o.open()) {
            con.createQuery(spSql).addParameter("id", id).executeUpdate();
        }
    }

    public static void batchDelete(String ids) {

        ids = ids.replace("[", "(").replace("]", ")");

        val deleteSql = "DELETE FROM tbFriendSalePrice WHERE Id IN " + ids;

        val selectSql = "SELECT ProductId FROM tbFriendSalePrice WHERE id IN " + ids;

        try (val con = db.sql2o.open()) {
            List<String> proIds = con.createQuery(selectSql).executeAndFetch(String.class);
            con.createQuery(deleteSql).executeUpdate();
        }
    }

    private static void sync(String productId) {
        try (val con = db.sql2o.beginTransaction()) {
            ProductInfo product = ProductBusiness.load(productId);
            val proList = con.createQuery("SELECT P.productId,P.accountId 'account.accountId',P.SalePrice FROM tbProduct P " +
                    " WHERE P.status = 8 AND P.OriginAccountId = :accountId AND P.OriginProductId = :productId ")
                    .addParameter("accountId", product.getAccountId())
                    .addParameter("productId", product.getProductId())
                    .executeAndFetch(ProductInfo.class);
            if (CollectionUtil.isNotEmpty(proList))
                proList.forEach(product_temp -> {
                    val groupId = FriendBusiness.load(product.getAccountId(), product_temp.getAccount().getAccountId()).getFriendGroup().getGroupId();
                    con.createQuery("UPDATE tbProduct P SET P.PurchPrice = :purchPrice WHERE ProductId = :id ")
                            .addParameter("purchPrice", product_temp.getSalePrice())
                            .addParameter("id", product_temp.getProductId())
                            .executeUpdate();
                    if (CollectionUtil.isNotEmpty(product.getFriendSalePriceList()))
                        product.getFriendSalePriceList().forEach(fsp -> {
                            if (fsp.getFriendGroup().getGroupId().equals(groupId)) {
                                con.createQuery("UPDATE tbProduct P SET P.PurchPrice = :purchPrice WHERE ProductId = :id ")
                                        .addParameter("purchPrice", fsp.getSalePrice())
                                        .addParameter("id", product_temp.getProductId())
                                        .executeUpdate();
                            }
                        });
                });
            con.commit();
        }
    }
}
