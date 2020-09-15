package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.QuotationTemplateInfo;
import lombok.val;

import java.util.List;

public class QuotationTemplateBusiness {

    public static List<QuotationTemplateInfo> list(String accountId) {

        val sql = " SELECT TemplateName ,TemplateId ,Url ,SortNumber FROM tbQuotationTemplate Where AccountId= :accountId Order by SortNumber ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(QuotationTemplateInfo.class);
        }
    }

    public static void clearQuotationBookId(String quotationBookID) {

        val sql = "UPDATE tbquotationbill " +
                " SET quotation_book_id=NULL " +
                " WHERE quotation_book_id=:quotationBookID";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("quotationBookID", quotationBookID)
                    .executeUpdate();
        }
    }

    public static QuotationTemplateInfo load(String accountId, String templateName) {

        val sql = " SELECT TemplateName ,TemplateId ,Url ,SortNumber FROM tbQuotationTemplate " +
                "  Where AccountId= :accountId AND TemplateName = :templateName Order by SortNumber ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("templateName", templateName)
                    .executeAndFetchFirst(QuotationTemplateInfo.class);
        }
    }
}