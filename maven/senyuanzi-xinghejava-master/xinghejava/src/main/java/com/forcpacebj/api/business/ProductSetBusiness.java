package com.forcpacebj.api.business;


import com.forcpacebj.api.entity.ProductSetDetailInfo;
import com.forcpacebj.api.entity.ProductSetInfo;
import com.forcpacebj.api.entity.ProductSetSectionInfo;
import com.forcpacebj.api.utils.CollectionUtil;
import com.forcpacebj.api.utils.StrUtil;
import lombok.val;
import org.sql2o.Connection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProductSetBusiness {

    public static List<ProductSetInfo> find(Map conditions, String accountId) {
        val category = conditions.containsKey("categoryId") && conditions.get("categoryId") == null ? "IS :categoryId" : "= :categoryId OR :categoryId is null";

        val sql = " SELECT Id ,Name ,IsShow ,PictureUrl ,ProductSetCategoryId 'category.id' FROM tbProductSet " +
                " WHERE AccountId= :accountId" +
                "  and (ProductSetCategoryId " + category + " ) " +
                "  and (Name LIKE CONCAT('%',:name,'%') OR :name is null) " +
                "  and  (IsShow = :isShow OR :isShow is null) " +
                "  and  (Name LIKE CONCAT('%',:all,'%') OR :all is null) " +
                " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("categoryId", conditions.get("categoryId"))
                    .addParameter("name", conditions.get("name"))
                    .addParameter("isShow", conditions.get("isShow"))
                    .addParameter("all", conditions.get("all"))
                    .executeAndFetch(ProductSetInfo.class);
        }
    }

    public static int count(Map conditions, String accountId) {
        val category = conditions.containsKey("categoryId") && conditions.get("categoryId") == null ? "IS :categoryId" : "= :categoryId OR :categoryId is null";

        val sql = " SELECT count(*) xcount FROM tbProductSet " +
                " WHERE AccountId= :accountId " +
                "  and (ProductSetCategoryId " + category + ") " +
                "  and (Name LIKE CONCAT('%',:name,'%') OR :name is null) " +
                "  and  (Name LIKE CONCAT('%',:all,'%') OR :all is null) " +
                "  and  (IsShow = :isShow OR :isShow is null) ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("categoryId", conditions.get("categoryId"))
                    .addParameter("name", conditions.get("name"))
                    .addParameter("isShow", conditions.get("isShow"))
                    .addParameter("all", conditions.get("all"))
                    .executeScalar(int.class);
        }
    }

    public static ProductSetInfo load(String accountId, String id) {

        String sql = " SELECT S.Id ,S.Name ,S.IsShow ,S.PictureUrl ,S.ProductSetCategoryId 'category.id' ,C.Name 'category.name' " +
                " FROM tbProductSet S" +
                " Left Join tbProductSetCategory C on S.AccountId=C.AccountId AND S.ProductSetCategoryId = C.Id " +
                " WHERE S.AccountId= :accountId AND S.Id = :id";

        try (val con = db.sql2o.open()) {

            val productSet = con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeAndFetchFirst(ProductSetInfo.class);


            if (productSet != null) {
                sql = " select ProductSetId ,SectionId ,SectionName from tbProductSetSection " +
                        "Where AccountId= :accountId AND ProductSetId = :id order by SectionId";

                val sectionList = con.createQuery(sql)
                        .addParameter("accountId", accountId)
                        .addParameter("id", id)
                        .executeAndFetch(ProductSetSectionInfo.class);

                if (CollectionUtil.isNotEmpty(sectionList)) {

                    for (val section : sectionList) {

                        sql = " SELECT SortNumber ,ProductId 'product.productId' ,Quantity ,Role 'product.role' " +
                                " FROM tbProductSetDetail " +
                                " WHERE AccountId= :accountId " +
                                "   AND ProductSetId = :id" +
                                "   and SectionId = :sectionId " +
                                " Order by SortNumber";
                        val productDetails = con.createQuery(sql)
                                .addParameter("accountId", accountId)
                                .addParameter("id", id)
                                .addParameter("sectionId", section.getSectionId())
                                .executeAndFetch(ProductSetDetailInfo.class);

                        if (CollectionUtil.isNotEmpty(productDetails)) {
                            productDetails.forEach(d -> {

                                val product = ProductBusiness.load(d.getProduct().getProductId());
                                if (StrUtil.isNotBlank(d.getProduct().getRole()) && product != null) {  //角色不能覆盖
                                    product.setRole(d.getProduct().getRole());
                                }
                                d.setProduct(product);
                            });
                            section.setProductDetails(productDetails);
                        }
                    }
                    productSet.setSectionList(sectionList);
                }
            }
            return productSet;
        }
    }

    public static void insert(String accountId, ProductSetInfo obj) {

        val sql = " INSERT INTO tbProductSet(AccountId ,Id ,Name ,IsShow ,PictureUrl ,ProductSetCategoryId) " +
                "        values (:accountId ,:id ,:name ,:isShow ,:pictureUrl ,:categoryId)";

        try (val con = db.sql2o.beginTransaction()) {

            con.createQuery(sql).bind(obj)
                    .addParameter("accountId", accountId)
                    .addParameter("categoryId", obj.getCategory().getId())
                    .executeUpdate();

            insertDetail(con, accountId, obj.getId(), obj.getSectionList());

            con.commit();
        }
    }

    public static void update(String accountId, ProductSetInfo obj) {

        val productSetId = obj.getId();

        val sql = " UPDATE tbProductSet " +
                "      SET Name = :name , " +
                "          IsShow = :isShow ," +
                "          ProductSetCategoryId=:productSetCategoryId," +
                "          PictureUrl = :pictureUrl" +
                "    WHERE AccountId= :accountId And Id = :id ";

        try (val con = db.sql2o.beginTransaction()) {

            con.createQuery(sql).bind(obj)
                    .addParameter("accountId", accountId)
                    .addParameter("id", productSetId)
                    .addParameter("productSetCategoryId", obj.getCategory() == null ? null : obj.getCategory().getId())
                    .executeUpdate();

            Arrays.asList(" DELETE FROM tbProductSetSection WHERE AccountId= :accountId AND ProductSetId = :id ;",
                    " DELETE FROM tbProductSetDetail WHERE AccountId= :accountId AND ProductSetId = :id ; ")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("accountId", accountId)
                                    .addParameter("id", productSetId)
                                    .executeUpdate()
                    );

            insertDetail(con, accountId, productSetId, obj.getSectionList());

            con.commit();
        }
    }

    private static void insertDetail(Connection con, String accountId, String productSetId, List<ProductSetSectionInfo> sectionList) {

        if (CollectionUtil.isNotEmpty(sectionList)) {

            for (int i = 0; i < sectionList.size(); i++) {
                val section = sectionList.get(i);

                String sql = " insert into tbProductSetSection(AccountId ,ProductSetId ,SectionId ,SectionName ) Values(:accountId ,:productSetId ,:sectionId ,:sectionName)";

                con.createQuery(sql)
                        .addParameter("accountId", accountId)
                        .addParameter("productSetId", productSetId)
                        .addParameter("sectionId", i)
                        .addParameter("sectionName", section.getSectionName())
                        .executeUpdate();

                val productDetails = section.getProductDetails();
                if (CollectionUtil.isNotEmpty(productDetails)) {

                    for (int j = 0; j < productDetails.size(); j++) {

                        val product = productDetails.get(j);

                        sql = " INSERT INTO tbProductSetDetail(AccountId ,ProductSetId ,SectionId ,SortNumber ,ProductId ,Quantity ,Role)" +
                                " Values(:accountId ,:productSetId , :sectionId ,:sortNumber ,:productId ,:quantity ,:role)";

                        con.createQuery(sql)
                                .addParameter("accountId", accountId)
                                .addParameter("productSetId", productSetId)
                                .addParameter("sectionId", i)
                                .addParameter("sortNumber", j)
                                .addParameter("productId", product.getProduct().getProductId())
                                .addParameter("quantity", product.getQuantity())
                                .addParameter("role", product.getProduct().getRole())
                                .executeUpdate();
                    }
                }
            }
        }
    }

    public static void delete(String accountId, String id) {

        try (val con = db.sql2o.beginTransaction()) {

            Arrays.asList(
                    " DELETE FROM tbProductSetDetail WHERE AccountId= :accountId AND ProductSetId = :id ",
                    " DELETE FROM tbProductSetSection WHERE AccountId= :accountId AND ProductSetId = :id ;",
                    " DELETE FROM tbProductSet WHERE AccountId= :accountId AND Id = :id ")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("accountId", accountId)
                                    .addParameter("id", id)
                                    .executeUpdate()
                    );

            con.commit();
        }
    }
}
