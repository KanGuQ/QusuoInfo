package com.forcpacebj.api.business;

import com.forcpacebj.api.Program;
import com.forcpacebj.api.entity.*;
import com.forcpacebj.api.utils.*;
import lombok.val;
import org.sql2o.Connection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class QuotationBillBusiness {

    public static List<QuotationBillInfo> listCoo(UserInfo user, String projectId) {

        val sql = " SELECT Q.AccountId,Q.BillId ,Q.BillName ,Q.QuotationCode ,Q.QuotationDate ,Q.UserId 'user.userId' ,Q.UserName 'user.userName' ,Q.quotation_book_id 'quotationBookId'," +
                " Q.TemplateName 'template.templateName' ,T.TemplateId 'template.templateId' ,T.Url 'template.url' ,Q.UpdateTime ,Q.Url" +
                " FROM tbQuotationBill Q" +
                " Left Join tbQuotationTemplate T On Q.AccountId=T.AccountId And Q.TemplateName = T.TemplateName " +
//                " WHERE Q.AccountId= :accountId " +
//                " AND (Q.DepartmentId=:departmentId OR :departmentId IS NULL)" +
//                " And Q.ProjectId = :projectId ORDER BY Q.UpdateTime DESC";
                " where Q.ProjectId = :projectId ORDER BY Q.UpdateTime DESC";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
//                    .addParameter("accountId", user.getAccountId())
//                    .addParameter("departmentId", user.getDepartment().getId())
                    .addParameter("projectId", projectId)
                    .executeAndFetch(QuotationBillInfo.class);
        }
    }


    public static List<QuotationBillInfo> list(UserInfo user, String projectId) {

        val sql = " SELECT Q.AccountId,Q.BillId ,Q.BillName ,Q.QuotationCode ,Q.QuotationDate ,Q.UserId 'user.userId' ,Q.UserName 'user.userName' ,Q.quotation_book_id 'quotationBookId'," +
                  " Q.TemplateName 'template.templateName' ,T.TemplateId 'template.templateId' ,T.Url 'template.url' ,Q.UpdateTime ,Q.Url" +
                  " FROM tbQuotationBill Q" +
                  " Left Join tbQuotationTemplate T On Q.AccountId=T.AccountId And Q.TemplateName = T.TemplateName " +
                " WHERE Q.AccountId= :accountId " +
                " AND (Q.DepartmentId=:departmentId OR :departmentId IS NULL)" +
                " And Q.ProjectId = :projectId ORDER BY Q.UpdateTime DESC";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", user.getAccountId())
                    .addParameter("departmentId", user.getDepartment().getId())
                    .addParameter("projectId", projectId)
                    .executeAndFetch(QuotationBillInfo.class);
        }
    }


    public static List<QuotationBillInfo> displayList(UserInfo user) {

        val sql = " SELECT Q.BillId ,Q.BillName ,Q.QuotationCode ,Q.QuotationDate ,Q.UserId 'user.userId' ,Q.UserName 'user.userName' , P.ProjectStage 'project.projectStage', Q.ProjectId 'project.projectId'," +
                " Q.TemplateName 'template.templateName' ,T.TemplateId 'template.templateId' ,T.Url 'template.url' ,Q.UpdateTime ,Q.Url ,Q.TotalAmount,P.ProjectName 'project.projectName',P.EngineeringStage 'project.engineeringStage' " +
                " FROM tbQuotationBill Q" +
                " Left Join tbQuotationTemplate T On Q.AccountId=T.AccountId And Q.TemplateName = T.TemplateName " +
                " INNER JOIN tbProject P ON P.ProjectId = Q.ProjectId AND Q.AccountId = P.AccountId " +
                " WHERE Q.AccountId= :accountId AND (Q.DepartmentId=:departmentId OR :departmentId IS NULL)" +
                " AND Q.ProjectId <> 0  " + (user.getIsAdmin() ? "And :userId = :userId":"And Q.UserId = :userId") + " ORDER BY Q.UpdateTime DESC LIMIT 10 "; //如果是管理员则显示所有的，否则显示个人的

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", user.getAccountId())
                    .addParameter("departmentId", user.getDepartment().getId())
                    .addParameter("userId",user.getUserId())
                    .executeAndFetch(QuotationBillInfo.class);
        }
    }

    public static int count(UserInfo user, String projectId) {

        val sql = " SELECT count(1) xcount FROM tbQuotationBill " +
                " WHERE AccountId= :accountId AND (DepartmentId=:departmentId OR :departmentId IS NULL)" +
                " AND ProjectId = :projectId ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", user.getAccountId())
                    .addParameter("departmentId", user.getDepartment().getId())
                    .addParameter("projectId", projectId)
                    .executeScalar(int.class);
        }
    }

    public static QuotationBillInfo load(String id, Integer roleId) {

        String sql = " SELECT B.BillId, B.BillName, B.QuotationCode ,B.UserId 'user.userId' ,B.UserName 'user.userName', B.ProjectId 'project.projectId' ," +
                " B.QuotationDate,B.TaxRate,B.Fee,B.Bonus,B.LaborCost,B.OtherCost,B.Quotation ,B.IsShowTotalOffer ,B.quotation_book_id 'quotationBookId' ," +
                " B.TotalProductAmount ,B.TotalServiceAmount ,B.TotalTax ,B.TotalAmount ," +
                " B.TemplateName 'template.templateName' ,T.TemplateId 'template.templateId' ,T.Url 'template.url' ,B.UpdateTime ,B.Url" +
                " FROM tbQuotationBill B " +
                " LEFT JOIN tbQuotationTemplate T ON B.AccountId = T.AccountId AND B.TemplateName = T.TemplateName" +
                " WHERE B.BillId = :id";

        try (val con = db.sql2o.open()) {
            val result = con.createQuery(sql)
                    .addParameter("id", id)
                    .executeAndFetchFirst(QuotationBillInfo.class);

            if (result != null) {

                sql = " Select BillId,SheetId,SheetName,ProductTotalAmount ,ServiceAmount," +
                        " IsShowOfferAmount,OfferAmount , ServiceAmountRate , TaxRate ,Tax ,TotalAmount," +
                        " DesignPrice,SoftwarePrice,MediaPrice,InstallationPrice,RemotePrice,ProjectPrice " +
                        " from tbQuotationBillSheet " +
                        " Where BillId= :id Order By SheetId";

                val sheetList = con.createQuery(sql)
                        .addParameter("id", id)
                        .executeAndFetch(QuotationBillSheetInfo.class);

                if (CollectionUtil.isNotEmpty(sheetList)) {
                    for (val sheet : sheetList) {
                        sql = " Select BillId,SheetId,SectionId,SectionName ,TotalAmount from tbQuotationBillSection " +
                                " Where BillId= :id And SheetId= :sheetId order By SectionId";
                        val sectionList = con.createQuery(sql)
                                .addParameter("id", id)
                                .addParameter("sheetId", sheet.getSheetId())
                                .executeAndFetch(QuotationBillSectionInfo.class);

                        if (CollectionUtil.isNotEmpty(sectionList)) {
                            for (val section : sectionList) {
                                sql = "Select B.ProductId 'product.productId' ,B.ProductName 'product.productName', B.Role 'product.role', B.Quantity ," +
                                        " B.Unit 'product.unit' ,B.SalesPrice 'product.salePrice', P.PictureUrl 'product.pictureUrl' ,P.PictureScale 'product.pictureScale', P.Brand 'product.brand'," +
                                        (roleId != null ? " Case When IsManual=1 Then B.SalesPrice Else case when R.PurchPrice is null then P.PurchPrice else R.PurchPrice end  End 'product.purchPrice' ,"
                                                : " Case When IsManual=1 Then B.SalesPrice Else P.PurchPrice End 'product.purchPrice' ,") +
                                        " B.PurchCost ,B.SalesAmount ,B.IsManual ,P.ProductType 'product.productType'," +
                                        " P.MinDistance 'product.MinDistance' ,P.MaxDistance 'product.maxDistance' ," +
                                        " P.CurtainSize 'product.curtainSize', P.CurtainProportion 'product.curtainProportion' ,P.Brief 'product.brief' ,P.Feature 'product.feature' ,P.Remark 'product.remark'  " +
                                        " from tbQuotationBillProduct B " +
                                        " left join tbProduct P on B.AccountId=P.AccountId AND B.ProductId = P.ProductId " +
                                        (roleId != null ? " left join tbRolePurchPrice R on B.AccountId=R.AccountId AND B.ProductId = R.ProductId AND R.RoleId =:roleId " : "") +
                                        " Where B.BillId= :id And B.SheetId= :sheetId And B.SectionId= :sectionId " +
                                        " Order By B.SortNumber";

                                val productList = roleId != null ?
                                        con.createQuery(sql)
                                                .addParameter("id", id)
                                                .addParameter("roleId", roleId)
                                                .addParameter("sheetId", sheet.getSheetId())
                                                .addParameter("sectionId", section.getSectionId())
                                                .executeAndFetch(QuotationBillProductInfo.class) :
                                        con.createQuery(sql)
                                                .addParameter("id", id)
                                                .addParameter("sheetId", sheet.getSheetId())
                                                .addParameter("sectionId", section.getSectionId())
                                                .executeAndFetch(QuotationBillProductInfo.class);

                                section.setProductList(productList);
                            }
                        }// 获取功能分类下的所有产品信息

                        sheet.setSectionList(sectionList);
                    }
                }//获取sheet下的所有功能分类

                result.setSheetList(sheetList);
            }
            return result;
        }
    }

    public static void insert(QuotationBillInfo bill) {

        val sql = "INSERT INTO  tbQuotationBill" +
                "(DepartmentId, AccountId ,BillId,BillName ,ProjectId ,UserId, UserName ,QuotationCode ,QuotationDate," +
                " TaxRate ,Fee ,Bonus ,LaborCost,OtherCost ,Quotation ,IsShowTotalOffer ,TotalProductAmount ,TotalServiceAmount ,TotalTax ,TotalAmount ,TemplateName ,UpdateTime ,Url)" +
                " VALUES(:departmentId, :accountId ,:billId, :billName, :projectId , :userId, :userName ,:quotationCode ,:quotationDate, " +
                " :taxRate , :fee ,:bonus , :laborCost, :otherCost , :quotation ,:isShowTotalOffer ,:totalProductAmount ,:totalServiceAmount ,:totalTax ,:totalAmount ,:templateName ,:updateTime ,:url)";

        val billId = bill.getBillId();
        val accountId = bill.getAccountId();

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql)
                    .bind(bill)
                    .addParameter("projectId", bill.getProject().getProjectId())
                    .addParameter("userId", bill.getUser().getUserId())
                    .addParameter("userName", bill.getUser().getUserName())
                    .addParameter("templateName", bill.getTemplate().getTemplateName())
                    .executeUpdate();

            insertDetail(con, accountId, billId, bill.getSheetList());

            con.createQuery("Update tbProject Set ModifyUserId = :userId,ModifyUserName = :userName,ModifyDateTime = :updateTime WHERE AccountId= :accountId And ProjectId = :projectId")
                    .addParameter("accountId", accountId)
                    .addParameter("updateTime", DateUtil.now())
                    .addParameter("userId", bill.getUser().getUserId())
                    .addParameter("userName", bill.getUser().getUserName())
                    .addParameter("projectId", bill.getProject().getProjectId())
                    .executeUpdate();

            con.commit();
        }
    }

    public static void update(String accountId, QuotationBillInfo bill) {

        val billId = bill.getBillId();

        String sql = " UPDATE tbQuotationBill SET" +
                " BillName= :billName ," +
                " QuotationCode= :quotationCode ," +
                " TaxRate= :taxRate ," +
                " Fee= :fee ," +
                " Bonus= :bonus ," +
                " LaborCost= :laborCost ," +
                " OtherCost= :otherCost ," +
                " Quotation= :quotation ," +
                " IsShowTotalOffer= :isShowTotalOffer ," +
                " TotalProductAmount= :totalProductAmount ," +
                " TotalServiceAmount= :totalServiceAmount ," +
                " TotalTax= :totalTax ," +
                " TotalAmount= :totalAmount ," +
                " TemplateName= :templateName ," +
                " UpdateTime = :updateTime ," +
                " Url = :url ," +
                " quotation_book_id = :quotationBookId" +
                " WHERE AccountId= :accountId And BillId = :billId ";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql)
                    .bind(bill)
                    .addParameter("accountId", accountId)
                    .addParameter("templateName", bill.getTemplate().getTemplateName())
                    .executeUpdate();

            Arrays.asList(
                    " DELETE FROM tbQuotationBillSheet WHERE AccountId= :accountId And BillId = :id ; ",
                    " DELETE FROM tbQuotationBillSection WHERE AccountId= :accountId And BillId = :id ; ",
                    " DELETE FROM tbQuotationBillProduct WHERE AccountId= :accountId And BillId = :id ; ")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("accountId", accountId)
                                    .addParameter("id", billId)
                                    .executeUpdate()
                    );

            insertDetail(con, accountId, billId, bill.getSheetList());

            con.createQuery("Update tbProject Set ModifyUserId = :userId,ModifyUserName = :userName,ModifyDateTime = :updateTime WHERE AccountId= :accountId And ProjectId = :projectId")
                    .addParameter("accountId", accountId)
                    .addParameter("updateTime", DateUtil.now())
                    .addParameter("userId", bill.getUser().getUserId())
                    .addParameter("userName", bill.getUser().getUserName())
                    .addParameter("projectId", bill.getProject().getProjectId())
                    .executeUpdate();

            con.commit();
        }
    }

    private static void insertDetail(Connection con, String accountId, String billId, List<QuotationBillSheetInfo> sheetList) {
        if (CollectionUtil.isNotEmpty(sheetList)) {
            int sheetId = 0;
            for (val sheet : sheetList) {
                sheetId += 1;
                String sql = "INSERT INTO tbQuotationBillSheet(AccountId ,BillId,SheetId,SheetName,ProductTotalAmount ,ServiceAmount,IsShowOfferAmount,OfferAmount , ServiceAmountRate , TaxRate ,Tax ,TotalAmount,DesignPrice,SoftwarePrice,MediaPrice,InstallationPrice,RemotePrice,ProjectPrice) " +
                        "VALUES (:accountId ,:billId, :sheetId, :sheetName, :productTotalAmount, :serviceAmount, :isShowOfferAmount, :offerAmount ,:serviceAmountRate ,:taxRate ,:tax ,:totalAmount, :designPrice,:softwarePrice,:mediaPrice,:installationPrice,:remotePrice,:projectPrice)";
                con.createQuery(sql).bind(sheet)
                        .addParameter("accountId", accountId)
                        .addParameter("billId", billId)
                        .addParameter("sheetId", sheetId)
                        .addParameter("sheetName", sheet.getSheetName())
                        .addParameter("productTotalAmount", sheet.getProductTotalAmount() == null ? 0 : sheet.getProductTotalAmount())
                        .addParameter("serviceAmount", sheet.getServiceAmount() == null ? 0 : sheet.getServiceAmount())
                        .addParameter("isShowOfferAmount", sheet.getIsShowOfferAmount() == null ? 0 : sheet.getIsShowOfferAmount())
                        .addParameter("offerAmount", sheet.getOfferAmount() == null ? 0 : sheet.getOfferAmount())
                        .addParameter("serviceAmountRate", sheet.getServiceAmountRate())
                        .addParameter("taxRate", sheet.getTaxRate())
                        .addParameter("tax", sheet.getTax() == null ? 0 : sheet.getTax())
                        .addParameter("totalAmount", sheet.getTotalAmount() == null ? 0 : sheet.getTotalAmount())
                        .executeUpdate();

                if (CollectionUtil.isNotEmpty(sheet.getSectionList())) {
                    int sectionId = 0;
                    for (val section : sheet.getSectionList()) {
                        sectionId += 1;
                        sql = "Insert Into tbQuotationBillSection(AccountId ,BillId,SheetId,SectionId,SectionName ,TotalAmount)" +
                                "Values(:accountId ,:billId, :sheetId, :sectionId, :sectionName, :totalAmount)";
                        con.createQuery(sql)
                                .addParameter("accountId", accountId)
                                .addParameter("billId", billId)
                                .addParameter("sheetId", sheetId)
                                .addParameter("sectionId", sectionId)
                                .addParameter("sectionName", section.getSectionName())
                                .addParameter("totalAmount", section.getTotalAmount())
                                .executeUpdate();

                        if (CollectionUtil.isNotEmpty(section.getProductList())) {
                            int sortNumber = 0;
                            for (val item : section.getProductList()) {
                                sortNumber += 1;
                                sql = "INSERT INTO tbQuotationBillProduct(AccountId ,BillId ,SheetId,SectionId,SortNumber,ProductId,ProductName,Role,SalesPrice,Quantity,Unit,PurchCost,SalesAmount,IsManual)" +
                                        "Values(:accountId ,:billId , :sheetId, :sectionId, :sortNumber, :productId, :productName, :role, :salesPrice, :quantity, :unit, :purchCost, :salesAmount, :isManual)";
                                con.createQuery(sql)
                                        .addParameter("accountId", accountId)
                                        .addParameter("billId", billId)
                                        .addParameter("sheetId", sheetId)
                                        .addParameter("sectionId", sectionId)
                                        .addParameter("sortNumber", sortNumber)
                                        .addParameter("productId", item.getIsManual() ? IdGenerator.NewId() : item.getProduct().getProductId())
                                        .addParameter("productName", item.getProduct().getProductName())
                                        .addParameter("role", item.getProduct().getRole())
                                        .addParameter("salesPrice", item.getProduct().getSalePrice())
                                        .addParameter("quantity", item.getQuantity())
                                        .addParameter("unit", item.getProduct().getUnit())
                                        .addParameter("purchCost", item.getPurchCost())
                                        .addParameter("salesAmount", item.getSalesAmount())
                                        .addParameter("isManual", item.getIsManual())
                                        .executeUpdate();
                            }
                        }
                    }
                }
            }
        }
    }

    public static void delete(String accountId, String id) {

        try (val con = db.sql2o.beginTransaction()) {
            Arrays.asList(
                    " DELETE FROM tbQuotationBill WHERE AccountId= :accountId And BillId = :id ; ",
                    " DELETE FROM tbQuotationBillSheet WHERE AccountId= :accountId And BillId = :id ; ",
                    " DELETE FROM tbQuotationBillSection WHERE AccountId= :accountId And BillId = :id ; ",
                    " DELETE FROM tbQuotationBillProduct WHERE AccountId= :accountId And BillId = :id ; ")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("accountId", accountId)
                                    .addParameter("id", id)
                                    .executeUpdate()
                    );
            con.commit();
        }
    }

    public static String[] insertAll(UserInfo user) {
        String accountId = user.getAccountId();
        QuotationBillInfo quotationBill = new QuotationBillInfo();
        quotationBill.setBillId(IdGenerator.NewId());
        quotationBill.setBillName(accountId + "全部产品");
        quotationBill.setQuotationCode("全部产品");
        QuotationTemplateInfo template = new QuotationTemplateInfo();
        template.setTemplateId("DEFAULT");
        template.setTemplateName("默认模板");
        quotationBill.setTemplate(template);
        quotationBill.setUser(user);
        quotationBill.setQuotationDate(DateUtil.now());
        quotationBill.setUpdateTime(DateUtil.now());
        try (val con = db.sql2o.open()) {
            List<ProductCategoryInfo> parentCategory = con.createQuery("SELECT Id ,Name ,ParentId ,SortNumber " +
                    " FROM tbproductcategory WHERE AccountId = :accountId AND (ParentId = '' OR ParentId IS NULL) " +
                    " ORDER BY SortNumber")
                    .addParameter("accountId", accountId)
                    .executeAndFetch(ProductCategoryInfo.class);
            List<QuotationBillSheetInfo> quotationBillSheets = new ArrayList<>();
            int sheetIndex = 1;
            for (ProductCategoryInfo category : parentCategory) {
                QuotationBillSheetInfo quotationBillSheet = new QuotationBillSheetInfo();
                quotationBillSheet.setBillId(quotationBill.getBillId());
                quotationBillSheet.setSheetId(sheetIndex++);
                if (category.getName() == null) continue;
                quotationBillSheet.setSheetName(category.getName().replace("/", "_")
                        .replace(" ", "_").replace(" ", "_"));

                List<ProductCategoryInfo> quotationBillSections = con.createQuery("SELECT Id ,Name ,ParentId ,SortNumber FROM tbproductcategory " +
                        " WHERE AccountId = :accountId AND ParentId = :ParentId ORDER BY SortNumber ")
                        .addParameter("accountId", accountId)
                        .addParameter("ParentId", category.getId())
                        .executeAndFetch(ProductCategoryInfo.class);
                List<QuotationBillSectionInfo> quotationBillSectionInfos = new ArrayList<>();
                int sectionIndex = 1;
                for (ProductCategoryInfo c : quotationBillSections) {
                    QuotationBillSectionInfo sectionInfo = new QuotationBillSectionInfo();
                    sectionInfo.setBillId(quotationBill.getBillId());
                    sectionInfo.setSheetId(quotationBillSheet.getSheetId());
                    sectionInfo.setSectionId(sectionIndex++);
                    sectionInfo.setSectionName(c.getName());

                    List<ProductInfo> products = con.createQuery("SELECT P.ProductId ,P.ProductName ," +
                            " P.ProductCategoryId 'category.id', C.Name 'category.name', P.Brand ," +
                            " P.Remark, P.Brief ,P.Feature ,P.ProductDetail ,P.ProductDetail2 ,P.ProductArea, P.Color, P.GuidePrice ,P.PurchPrice , P.SalePrice, P.Unit, P.Role ," +
                            " P.IsShow ,P.SortNumber ,P.PictureUrl ,P.PictureScale ,P.ProductType ,P.MinDistance ,P.MaxDistance ,P.CurtainSize ,P.curtainProportion ," +
                            " P.IsSharedProduct ,P.FromAccountId 'fromAccount.accountId' ,P.FromProductId ,P.OriginAccountId 'originAccount.accountId' ,P.OriginProductId " +
                            " FROM tbproduct P LEFT JOIN tbProductCategory C on P.AccountId=C.AccountId AND P.ProductCategoryId = C.Id  WHERE P.AccountId = :AccountId " +
                            "AND P.IsShow = 1 AND P.ProductCategoryId = :ProductCategoryId ")
                            .addParameter("AccountId", accountId)
                            .addParameter("ProductCategoryId", c.getId())
                            .executeAndFetch(ProductInfo.class);
                    List<QuotationBillProductInfo> quotationBillProductInfos = new ArrayList<>();
                    for (ProductInfo product : products) {
                        QuotationBillProductInfo quotationBillProduct = new QuotationBillProductInfo();
                        quotationBillProduct.setProduct(product);
                        quotationBillProduct.setBillId(quotationBill.getBillId());
                        quotationBillProduct.setSheetId(quotationBillSheet.getSheetId());
                        quotationBillProduct.setSectionId(sectionInfo.getSectionId());
                        quotationBillProduct.setSortNumber(product.getSortNumber());
                        quotationBillProduct.setIsManual(false);
                        quotationBillProduct.setQuantity(BigDecimal.ONE);
                        quotationBillProductInfos.add(quotationBillProduct);
                    }
                    sectionInfo.setProductList(quotationBillProductInfos);
                    quotationBillSectionInfos.add(sectionInfo);
                }
                List<ProductInfo> products = con.createQuery("SELECT P.ProductId ,P.ProductName ," +
                        " P.ProductCategoryId 'category.id', C.Name 'category.name', P.Brand ," +
                        " P.Remark, P.Brief ,P.Feature ,P.ProductDetail ,P.ProductDetail2 ,P.ProductArea, P.Color, P.GuidePrice ,P.PurchPrice , P.SalePrice, P.Unit, P.Role ," +
                        " P.IsShow ,P.SortNumber ,P.PictureUrl ,P.PictureScale ,P.ProductType ,P.MinDistance ,P.MaxDistance ,P.CurtainSize ,P.curtainProportion ," +
                        " P.IsSharedProduct ,P.FromAccountId 'fromAccount.accountId' ,P.FromProductId ,P.OriginAccountId 'originAccount.accountId' ,P.OriginProductId " +
                        " FROM tbproduct P LEFT JOIN tbProductCategory C on P.AccountId=C.AccountId AND P.ProductCategoryId = C.Id  WHERE P.AccountId = :AccountId " +
                        "AND P.IsShow = 1 AND P.ProductCategoryId = :ProductCategoryId ")
                        .addParameter("AccountId", accountId)
                        .addParameter("ProductCategoryId", category.getId())
                        .executeAndFetch(ProductInfo.class);
                QuotationBillSectionInfo sectionInfo = new QuotationBillSectionInfo();
                List<QuotationBillProductInfo> ps = new ArrayList<>();
                for (ProductInfo product : products) {
                    QuotationBillProductInfo quotationBillProduct = new QuotationBillProductInfo();
                    quotationBillProduct.setProduct(product);
                    quotationBillProduct.setBillId(quotationBill.getBillId());
                    quotationBillProduct.setSheetId(quotationBillSheet.getSheetId());
                    quotationBillProduct.setSectionId(sectionIndex);
                    quotationBillProduct.setSortNumber(product.getSortNumber());
                    quotationBillProduct.setIsManual(false);
                    quotationBillProduct.setQuantity(BigDecimal.ONE);
                    ps.add(quotationBillProduct);
                }
                if (products.size() != 0) {
                    sectionInfo.setProductList(ps);
                    sectionInfo.setBillId(quotationBill.getBillId());
                    sectionInfo.setSheetId(quotationBillSheet.getSheetId());
                    sectionInfo.setSectionId(sectionIndex);
                    sectionInfo.setSectionName("父目录下产品");
                    quotationBillSectionInfos.add(sectionInfo);
                }
                quotationBillSheet.setSectionList(quotationBillSectionInfos);

                AtomicReference<Boolean> isAdd = new AtomicReference<>(false);
                quotationBillSheet.getSectionList().forEach(section -> {
                    section.getProductList().forEach(product -> {
                        if (product != null && product.getProduct() != null) {
                            isAdd.set(true);
                            return;
                        }
                    });
                });
                if (!isAdd.get()) continue;
                quotationBillSheets.add(quotationBillSheet);
            }
            List<ProductInfo> products = con.createQuery("SELECT P.ProductId ,P.ProductName ," +
                    " P.ProductCategoryId 'category.id', P.Brand ," +
                    " P.Remark, P.Brief ,P.Feature ,P.ProductDetail ,P.ProductDetail2 ,P.ProductArea, P.Color, P.GuidePrice ,P.PurchPrice , P.SalePrice, P.Unit, P.Role ," +
                    " P.IsShow ,P.SortNumber ,P.PictureUrl ,P.PictureScale ,P.ProductType ,P.MinDistance ,P.MaxDistance ,P.CurtainSize ,P.curtainProportion ," +
                    " P.IsSharedProduct ,P.FromAccountId 'fromAccount.accountId' ,P.FromProductId ,P.OriginAccountId 'originAccount.accountId' ,P.OriginProductId " +
                    " FROM tbproduct P  WHERE P.AccountId = :AccountId " +
                    "AND P.IsShow = 1 AND (P.ProductCategoryId = '' OR P.ProductCategoryId IS NULL) ").addParameter("AccountId", accountId).executeAndFetch(ProductInfo.class);
            QuotationBillSheetInfo quotationBillSheet;
            if (products.size() != 0) {
                quotationBillSheet = new QuotationBillSheetInfo();
                quotationBillSheet.setBillId(quotationBill.getBillId());
                quotationBillSheet.setSheetId(sheetIndex);
                quotationBillSheet.setSheetName("未分类");
                List<QuotationBillSectionInfo> quotationBillSectionInfos = new ArrayList<>();
                QuotationBillSectionInfo sectionInfo = new QuotationBillSectionInfo();
                sectionInfo.setBillId(quotationBill.getBillId());
                sectionInfo.setSheetId(sheetIndex);
                sectionInfo.setSectionId(1);
                sectionInfo.setSectionName("未分类");
                List<QuotationBillProductInfo> quotationBillProductInfos = new ArrayList<>();
                for (ProductInfo product : products) {
                    QuotationBillProductInfo quotationBillProduct = new QuotationBillProductInfo();
                    quotationBillProduct.setProduct(product);
                    quotationBillProduct.setBillId(quotationBill.getBillId());
                    quotationBillProduct.setSheetId(sheetIndex);
                    quotationBillProduct.setSectionId(1);
                    quotationBillProduct.setSortNumber(product.getSortNumber());
                    quotationBillProduct.setIsManual(false);
                    quotationBillProduct.setQuantity(BigDecimal.ONE);
                    quotationBillProductInfos.add(quotationBillProduct);
                }
                sectionInfo.setProductList(quotationBillProductInfos);
                quotationBillSectionInfos.add(sectionInfo);
                quotationBillSheet.setSectionList(quotationBillSectionInfos);
                quotationBillSheets.add(quotationBillSheet);
            }
            quotationBill.setSheetList(quotationBillSheets);
        }


        val sql = "INSERT INTO  tbQuotationBill" +
                "(AccountId ,BillId,BillName ,ProjectId ,UserId, UserName ,QuotationCode ,QuotationDate," +
                " TaxRate ,Fee ,Bonus ,LaborCost,OtherCost ,Quotation ,IsShowTotalOffer ,TotalProductAmount ,TotalServiceAmount ,TotalTax ,TotalAmount ,TemplateName ,UpdateTime ,Url)" +
                " VALUES(:accountId ,:billId, :billName, :projectId , :userId, :userName ,:quotationCode ,:quotationDate, " +
                " :taxRate , :fee ,:bonus , :laborCost, :otherCost , :quotation ,:isShowTotalOffer ,:totalProductAmount ,:totalServiceAmount ,:totalTax ,:totalAmount ,:templateName ,:updateTime ,:url)";

        val billId = quotationBill.getBillId();

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql)
                    .bind(quotationBill)
                    .addParameter("accountId", accountId)
                    .addParameter("projectId", 0)
                    .addParameter("userId", quotationBill.getUser().getUserId())
                    .addParameter("userName", quotationBill.getUser().getUserName())
                    .addParameter("templateName", quotationBill.getTemplate().getTemplateName())
                    .executeUpdate();

            insertDetail(con, accountId, billId, quotationBill.getSheetList());

            con.commit();
            long timeMillis = System.currentTimeMillis();
            HttpRequestUtil.sendPost(Program.templateServer + template.getTemplateId() +
                    "/" + billId + "/" + timeMillis, null);
            return new String[]{billId, Long.toString(timeMillis)};
        }
    }
}
