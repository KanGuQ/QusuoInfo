/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */
package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.ProductTagInfo;
import lombok.val;

import java.util.List;

public class ProductTagBusiness {

    public static List<ProductTagInfo> list(String accountId) {

        val sql = "SELECT Id ,TagName ,SortNumber FROM tbProductTag WHERE AccountId= :accountId";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(ProductTagInfo.class);
        }
    }

    public static ProductTagInfo insert(String accountId, ProductTagInfo obj) {

        val sql = " INSERT INTO tbProductTag(AccountId ,TagName ,SortNumber) values (:accountId ,:tagName ,:sortNumber)";

        try (val con = db.sql2o.open()) {

            val id = con.createQuery(sql).bind(obj)
                    .addParameter("accountId", accountId)
                    .executeUpdate().getKey(Integer.class);

            obj.setId((int) id);
            return obj;
        }
    }

    public static void delete(String accountId, String id) {

        val sql = " DELETE FROM tbProductTag WHERE AccountId= :accountId AND Id = :id";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeUpdate();
        }
    }


    public static void update(String accountId, ProductTagInfo obj) {

        val sql = " UPDATE tbProductTag SET TagName = :tagName , SortNumber = :sortNumber  WHERE AccountId= :accountId AND Id = :id";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .bind(obj)
                    .addParameter("accountId", accountId)
                    .executeUpdate();
        }
    }
}