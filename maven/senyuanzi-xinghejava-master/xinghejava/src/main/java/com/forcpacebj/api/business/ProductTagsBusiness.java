package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.ProductTagInfo;
import com.forcpacebj.api.utils.CollectionUtil;
import lombok.val;

import java.util.List;

public class ProductTagsBusiness {

    public static List<ProductTagInfo> load(String accountId, String productId) {

        val sql = "SELECT T.Id ,T.TagName ,T.SortNumber " +
                " FROM tbProductTags P" +
                " INNER JOIN tbProductTag T on P.ProductTagId  = T.Id ANd P.AccountId = T.AccountId " +
                " WHERE P.AccountId= :accountId AND P.ProductId =:productId";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("productId", productId)
                    .executeAndFetch(ProductTagInfo.class);
        }
    }

    public static void insert(String accountId, String productId, List<ProductTagInfo> tags) {

        val sql = " DELETE FROM tbProductTags WHERE AccountId= :accountId AND ProductId = :productId";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("productId", productId)
                    .executeUpdate();

            if (CollectionUtil.isNotEmpty(tags)) {
                tags.forEach(tag -> {

                    val insertSql = " INSERT INTO tbProductTags(AccountId ,ProductId ,ProductTagId) values (:accountId ,:productId ,:productTagId)";

                    con.createQuery(insertSql)
                            .addParameter("accountId", accountId)
                            .addParameter("productId", productId)
                            .addParameter("productTagId", tag.getId())
                            .executeUpdate();
                });
            }

            con.commit();
        }
    }
}
