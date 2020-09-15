package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.ProductData;
import lombok.val;
import lombok.var;
import org.sql2o.Connection;

import java.util.Map;

public class ProductDataBusiness {

    public static void recordInTransaction(Connection con, String productId, String companyId, Long addDownloadCount, Long addQuotationCount) {
        var productData = ProductDataBusiness.find(con, productId);
        if (productData == null) {
            productData = new ProductData(productId, companyId, addDownloadCount, addQuotationCount);
        } else {
            productData.setDownloadCount(productData.getDownloadCount() + addDownloadCount);
            productData.setQuotationCount(productData.getQuotationCount() + addQuotationCount);
        }
        ProductDataBusiness.save(con, productData);
    }

    /**
     * 新建更新二合一
     */
    public static void save(Connection con, ProductData data) {
        var sql = "";
        if (data.getId() == null) {//insert
            sql = "INSERT INTO ProductData (productId,companyId,downloadCount,quotationCount) VALUES (:productId,:companyId,:downloadCount,:quotationCount)";
        } else {//update
            sql = "UPDATE ProductData set " +
                  " productId=:productId, " +
                  " companyId=:companyId, " +
                  " downloadCount=:downloadCount, " +
                  " quotationCount=:quotationCount " +
                  " where productId=:productId";
        }

        con.createQuery(sql)
                .bind(data)
                .executeUpdate();
    }

    /**
     * @return ProductData
     */
    public static ProductData find(Connection con, String productId) {
        var sql = "select * from productData where productId=:productId";

        return con.createQuery(sql)
                .addParameter("productId", productId)
                .executeAndFetchFirst(ProductData.class);
    }

    /**
     * @return ProductData
     */
    public static int count(Connection con, Map conditions) {
        var sql = "select count(1) from productData P" +
                  " left join tbproduct T ON P.productId=T.productId" +
                  " where P.companyId=:companyId" +
                  "  and  (T.brand like CONCAT('%',:brand,'%') OR :brand is null) " +
                  "  and  (T.role like CONCAT('%',:role,'%') OR :role is null) " +
                  "  and  (T.shortName like CONCAT('%',:shortName,'%') OR :shortName is null) ";

        return con.createQuery(sql)
                .addParameter("companyId", conditions.get("companyId"))
                .addParameter("brand", conditions.get("brand"))
                .addParameter("role", conditions.get("role"))
                .addParameter("shortName", conditions.get("shortName"))
                .executeScalar(int.class);
    }


}
