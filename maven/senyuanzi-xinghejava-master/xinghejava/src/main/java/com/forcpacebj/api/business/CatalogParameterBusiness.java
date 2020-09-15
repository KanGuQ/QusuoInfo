package com.forcpacebj.api.business;


import com.forcpacebj.api.entity.CatalogParameterInfo;
import com.forcpacebj.api.entity.ProductParameterInfo;
import com.forcpacebj.api.utils.CollectionUtil;
import lombok.val;

import java.util.*;

/**
 * Created by pc on 2019/10/10.
 */
public class CatalogParameterBusiness {

    public static List<CatalogParameterInfo> list(String catalogId) {

        val sql = "SELECT CatalogId,ParameterName,ParameterType,ParameterOptions,SortNum FROM tbCatalogParameter " +
                " WHERE CatalogId = :catalogId ORDER BY SortNum ";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("catalogId", catalogId)
                    .executeAndFetch(CatalogParameterInfo.class);
        }
    }

    public static List<ProductParameterInfo> listForProductValues(String productId, String accountId) {

        val sql = "SELECT " +
                " ParameterName , " +
                " ParameterType , " +
                " ParameterOptions , " +
                " ParameterValue, SortNum FROM tbProductParameter " +
                " WHERE productId = :productId and accountId = :accountId ORDER BY SortNum;";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("productId", productId)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(ProductParameterInfo.class);
        }
    }


    public static List<ProductParameterInfo> listGroupConcat(String catalogId, String accountId ,String fromPage) {

        String status = "  NOT IN (9,10) ";
        if (fromPage != null)
        switch (fromPage) {
            case "center":
                status = " NOT IN (1,8,9,10) ";
                break;
            case "public":
                status = " = 5 ";
                break;
        }
        val sql = "SELECT pp.ParameterName,GROUP_CONCAT(DISTINCT pp.ParameterValue) ParameterValue,pp.parameterType FROM tbProductParameter pp " +
                " INNER JOIN tbProduct p on p.ProductId = pp.ProductId AND p.AccountId = :accountId AND p.status " + status +
                " LEFT JOIN tbCatalogParameter cp ON cp.catalogId = p.catalogId " +
                " WHERE cp.CatalogId = :catalogId GROUP BY pp.ParameterName ORDER BY pp.ParameterValue;";
        try (val con = db.sql2o.open()) {
            val list = con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("catalogId", catalogId)
                    .executeAndFetch(ProductParameterInfo.class);
            return list;
        }
    }

    private static void partition(String nums) {
        HashMap<Integer, Integer> map = new HashMap<>();//第一个值为出现的数字，第二个值为出现的次数
        val list = Arrays.asList(nums.split(","));
        list.forEach(number -> {
            double num = Double.parseDouble(number);
            int c = 0;
            if (num == 0) {
                c = 1;
            }
            while (num >= 1) {
                num /= 10;
                c += 1;
            }
            if (map.containsKey(c)) {
                int temp = map.get(c);
                map.put(c, temp + 1);
            } else {
                map.put(c, 1);
            }
        });

        Collection<Integer> count = map.values();
        int maxCount = Collections.max(count);
        int maxnum = 0;//最多次出现的是几位数
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (maxCount == entry.getValue()) {
                maxnum = entry.getKey();
            }
        }
        StringBuffer numPartition = new StringBuffer();
        if (maxnum == 1) {   //0-2,2-4,4-6,6-8,8-10
            numPartition.append(list.get(0));
            val temp = 0d;
            for (val num : list) {
                if (Double.parseDouble(num) >= 2) return;
//                temp = Double.parseDouble(num);
            }
            numPartition.append("-").append(temp);

            list.forEach(num -> {

                if (Double.parseDouble(num) >= 2) return;
            });

        } else if (maxnum == 2) {  //0-20,20-40,40-60,60-80,80-100

        } else if (maxnum == 3) {    //0-200,200-400,400-600,600-800,800-1000

        } else if (maxnum == 4) {    //0-2000,2000-4000,4000-6000,6000-8000,8000-10000

        }

    }

    public static void insert(List<CatalogParameterInfo> catalogParameters) {
        val sql = " INSERT INTO tbCatalogParameter (CatalogId,ParameterName,ParameterType,ParameterOptions,SortNum) " +
                " VALUES (:catalogId,:parameterName,:parameterType,:parameterOptions,:sortNum) ";

        try (val con = db.sql2o.beginTransaction()) {
            for (val catalogParameter : catalogParameters) {
                if (catalogParameter.getSortNum() == null)
                    catalogParameter.setSortNum(con.createQuery("SELECT IFNULL(MAX(SortNum)+1,1) FROM tbCatalogParameter Where  " +
                            "   CatalogId = :catalogId ")
                            .addParameter("catalogId", catalogParameter.getCatalogId()).executeScalar(Integer.class));

                con.createQuery(sql).bind(catalogParameter)
                        .addParameter("catalogId", catalogParameter.getCatalogId())
                        .executeUpdate();

            }
            con.commit();
        }
    }


    public static void update(List<CatalogParameterInfo> catalogParameters, String catalogId) {
        val sql = " INSERT INTO tbCatalogParameter (CatalogId,ParameterName,ParameterType,ParameterOptions,SortNum) " +
                " VALUES (:catalogId,:parameterName,:parameterType,:parameterOptions,:sortNum) ";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery("DELETE FROM tbCatalogParameter WHERE catalogId = :catalogId")
                    .addParameter("catalogId", catalogId)
                    .executeUpdate();

            if (CollectionUtil.isNotEmpty(catalogParameters))
                for (val catalogParameter : catalogParameters) {
                    if (catalogParameter.getSortNum() == null)
                        catalogParameter.setSortNum(con.createQuery("SELECT IFNULL(MAX(SortNum)+1,1) FROM tbCatalogParameter Where  " +
                                "   CatalogId = :catalogId ")
                                .addParameter("catalogId", catalogId).executeScalar(Integer.class));

                    con.createQuery(sql).bind(catalogParameter)
                            .addParameter("catalogId", catalogId)
                            .executeUpdate();

                }

            con.commit();
        }
    }

    public static void delete(String id) {
        try (val con = db.sql2o.open()) {
            con.createQuery("DELETE FROM tbCatalogParameter WHERE id = :id")
                    .addParameter("id", id)
                    .executeUpdate();
        }
    }

}
