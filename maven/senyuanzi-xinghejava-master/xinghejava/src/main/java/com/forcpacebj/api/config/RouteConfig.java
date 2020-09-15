package com.forcpacebj.api.config;

import com.forcpacebj.api.Program;
import com.forcpacebj.api.controller.*;
import com.forcpacebj.api.newModule.controller.NewProductController;
import lombok.extern.slf4j.Slf4j;

import static spark.Spark.*;

@Slf4j
public class RouteConfig {

    public static void config() {

        staticFiles.location("/static");
        staticFiles.expireTime(600);

        staticFiles.externalLocation(Program.fileUploadDirectory);

        path("/api", () -> {

            path("/data", () -> {
                path("/wholesalerData", () -> {
                    get("/findWholesalerData", WholesalerController.findWholesalerData);
                    post("/findProductData", WholesalerController.findProductData);
                });
            });

            path("/common", () -> {

                post("/fileupload", FileUploadController.uploadOSS);

                path("/login", () -> {
                    post("/check", LoginController.check);
                    post("/modify", LoginController.modify);
                    post("/reset", LoginController.reset);
                    get("/update_content", LoginController.updateContent);
                });

                path("/user-role", () -> {
                    post("/find", UserRoleController.find);
                    post("/findRoles", UserRoleController.findRoles);
                    post("/count", UserRoleController.count);
                    get("/load/:roleId", UserRoleController.load);
                    get("/list", UserRoleController.list);
                    post("/delete/:roleId", UserRoleController.delete);
                    post("/update", UserRoleController.update);
                    post("/insert", UserRoleController.insert);
                });

                path("/user", () -> {
                    post("/find", UserController.find);
                    get("/findAllByAccountId", UserController.findAllByAccountId);
                    post("/count", UserController.count);
                    get("/load/:id", UserController.load);
                    get("/list", UserController.list);
                    post("/delete/:id", UserController.delete);
                    post("/update", UserController.update);
                    post("/trial", UserController.trial);
                    post("/findEmployees", UserController.findEmployees);
                    post("/findAll", UserController.findAllUsers);
                    post("/getUser", UserController.getUser);
                    post("/expel", UserController.expel);
                    post("/changeDepartment", UserController.changeDepartment);
                    get("/check_company/:accountName", UserController.checkCompany);
                    post("/register", UserController.register);
                    post("/isAlreadyReg", UserController.isAlreadyReg);
                    post("/get_register_vcode", UserController.getRegisterVcode);
                    post("/register_by_invite", UserController.registerByCode);
                    post("/apply_list", UserController.getApplyForList);
                    post("/accept_join", UserController.isAcceptJoinCompany);
                    post("/to_apply", UserController.toApplyFor);
                    post("/apply_count", UserController.getApplyForCount);
                    post("/invite_list", UserController.getInviteList);
                    post("/invite_to_company", UserController.inviteToCompany);
                    post("/join_by_code", UserController.toJoinCompany);
                    post("/invite_count", UserController.getInviteCount);

                    post("/loginRecordByDate", UserController.loginRecordByDate);
                });
                path("/company", () -> {
                    post("/register", CompanyController.register);
                    post("/check", CompanyController.checkCompanyName);
                    post("/list", CompanyController.getCompanyList);
                    post("/companyOrderByProductCount", CompanyController.companyOrderByProductCount);
                    post("/public_list", CompanyController.getPublicCompanyList);
                    get("/getCompanyDetail", CompanyController.getCompanyDetail);
                    post("/count", CompanyController.getCompanyCount);
                    post("/agree_create", CompanyController.agreeToCreate);
                    post("/payForRenew", CompanyController.payForRenew);
                    post("/update", CompanyController.updateCompany);

                    path("/department", () -> {
                        post("/list", DepartmentController.list);
                        post("/optionalDepartment", DepartmentController.optionalDepartment);
                        post("/chooseDepartment", DepartmentController.chooseDepartment);
                        post("/load", DepartmentController.load);
                        post("/insert", DepartmentController.insert);
                        post("/update", DepartmentController.update);
                        post("/delete", DepartmentController.delete);
                    });
                });
                path("/smart-lookup", () -> {
                    post("/product", SmartLookupController.product);
                    post("/brand", SmartLookupController.brand);
                    post("/company", SmartLookupController.companyName);
                    get("/load-user-list", SmartLookupController.loadUserList);
                    post("/project", SmartLookupController.project);
                    post("/people", SmartLookupController.people);
                    post("/people-unit", SmartLookupController.peopleUnit);
                    post("/people-name", SmartLookupController.peopleName);
                    post("/project-name", SmartLookupController.projectName);
                });
                path("/community", () -> {
                    post("/insert", CommunityController.insert);
                    post("/load", CommunityController.load);
                    post("/update", CommunityController.update);
                    post("/count", CommunityController.count);
                    post("/delete/:id", CommunityController.delete);
                    post("/list", CommunityController.find);

                });
            });

            path("/project", () -> {
                post("/find", ProjectController.find);
                post("/cooperations", ProjectController.cooperations);
                post("/applyCooperation", ProjectController.applyCooperation);
                post("/auditCooperation", ProjectController.auditCooperation);
                post("/findKanbanProjects", ProjectController.findKanbanProjects);
                post("/count", ProjectController.count);
                get("/load/:id", ProjectController.load);
                get("/list", ProjectController.list);
                post("/cooperativeProviders", ProjectController.cooperativeProviders);
                post("/delete/:id", ProjectController.delete);
                post("/safeDelete", ProjectController.safeDelete);
                post("/update", ProjectController.update);
                post("/insert", ProjectController.insert);
                post("/updateProjectStage", ProjectController.updateProjectStage);
                post("/getStageCount", ProjectController.getStageCount);

                path("/file", () -> {
                    get("/list/:projectId", ProjectFileController.list);
                    post("/insert", ProjectFileController.insert);
                    post("/update", ProjectFileController.update);
                    post("/delete/:id", ProjectFileController.delete);
                });

                path("/stage", () -> {
                    get("/list/:type", ProjectStageController.list);
                    post("/insert", ProjectStageController.insert);
                    post("/update", ProjectStageController.update);
                    post("/delete", ProjectStageController.delete);
                });

                path("/constructStage", () -> {
                    get("/list", ConstructStageController.list);
                    post("/insert", ConstructStageController.insert);
                    post("/update", ConstructStageController.update);
                    post("/delete", ConstructStageController.delete);
                });
            });

            path("/record", () -> {
                post("/find", RecordController.find);
                post("/findList", RecordController.findList);
                post("/findRecords", RecordController.findRecords);
                post("/count", RecordController.count);
                get("/load/:id", RecordController.load);
                post("/list", RecordController.list);
                post("/insert", RecordController.insert);
                post("/update", RecordController.update);
                post("/update-record-state", RecordController.updateRecordState);
                post("/delete/:id", RecordController.delete);
                post("/find-people-record", RecordController.findPeopleRecord);
                post("/find-project-record", RecordController.findProjectRecord);
                post("/delete-people-record/:recordId/:peopleId", RecordController.deletePeopleRecord);
                post("/delete-project-record/:recordId/:projectId", RecordController.deleteProjectRecord);

                get("/open-load/:accountId/:recordId", RecordController.openLoad);
                get("/create-mp-qrcode/:accountId/:recordId", RecordController.createMPQRCode);
            });

            path("/product", () -> {
                path("/product-category", () -> {
                    get("/list", ProductCategoryController.list);
                    get("/tList", ProductCategoryController.tList);
                    post("/associate_list", ProductCategoryController.associateList);
                    get("/public_list", ProductCategoryController.publicList);
                    get("/upload_category", ProductCategoryController.getPublicCategory);
                    post("/delete/:id", ProductCategoryController.delete);
                    post("/update", ProductCategoryController.update);
                    post("/insert", ProductCategoryController.insert);
                    post("/associate_insert", ProductCategoryController.associateInsert);
                });
                path("/product", () -> {
                    post("/find", ProductController.find);
                    post("/count", ProductController.count);
                    get("/load/:id", ProductController.load);
                    get("/loadByIdSimplify/:id", ProductController.loadByIdHidePrice);
                    post("/delete/:id", ProductController.delete);
                    post("/safeDelete", ProductController.safeDelete);
                    post("/update", ProductController.update);
                    post("/updateSpecialProduct", ProductController.updateSpecialProduct);
                    post("/insert", ProductController.insert);
                    post("/down_from_public", ProductController.downloadFromPublic);
                    post("/move-category/:sourceId/:targetId", ProductController.moveCategory);
                    post("/batchMove", ProductController.batchMoveCategory);
                    post("/batchMoveCatalog", ProductController.batchMoveCatalog);
                    post("/batchMovePublic", ProductController.batchMovePublicCategory);//移动公海产品
                    get("/getProductDetail", ProductController.getProductDetail);
                    post("/findProductToHelpModify", NewProductController.findProductToHelpModify);
                    post("/findProductAlreadyHelpModify", NewProductController.findProductAlreadyHelpModify);
                    post("/findProductByCondition", NewProductController.findProductByCondition);
                    post("/explantationProduct", NewProductController.explantationProduct);//企业公海管理中心 移除产品
                    post("/batchExplantationProduct", NewProductController.batchExplantationProduct);//批量 移除产品
                    post("/putawayProduct", NewProductController.putawayProduct);//上架产品 去审核
                    post("/putawayProductToHelp", NewProductController.putawayProductToHelp);//上架产品 去协助
                    post("/batchPutawayProduct", NewProductController.batchPutawayProduct);//批量 上架产品 去审核
                    post("/batchPutawayProductToHelp", NewProductController.batchPutawayProductToHelp);//批量 上架产品 去协助
                    post("/recallProduct", NewProductController.recallProduct);//撤回产品
                    post("/batchRecallProduct", NewProductController.batchRecallProduct);//批量 撤回产品
                    post("/confirmHelpedProduct", NewProductController.confirmHelpedProduct);//确认产品
                    post("/batchConfirmHelpedProduct", NewProductController.batchConfirmHelpedProduct);//批量 确认产品
                    post("/findWillModifyProduct", NewProductController.findWillModifyProduct);//获取 待更新（倒计时改变，包括下架）的产品
                    post("/findAllProductsInPublicAdmin", NewProductController.findAllProductsInPublicAdmin);//查询公海管理中心所有产品
                    post("/uploadProduct", NewProductController.uploadProduct);//上传产品
                    post("/batchUploadProduct", NewProductController.batchUploadProduct);//批量 上传产品
                    post("/offShelvesProduct", NewProductController.offShelvesProductNoWait);//下架产品
                    post("/batchOffShelvesProduct", NewProductController.batchOffShelvesProductNoWait);//批量下架
                    post("/findMyProducts", ProductController.findMyProducts);//我的产品库
                    post("/listIncludeFriendSalePrice", ProductController.listIncludeFriendSalePrice);//带有供货价的产品库
                    post("/importExcel", ProductController.importExcel);
                    post("/updateSortNum", ProductController.updateSortNum);
                    get("/inExclusive", ProductController.inExclusive);
                    get("/downloadExcel", ProductController.downloadExcel);
                });
                path("/product-set-category", () -> {
                    get("/list", ProductSetCategoryController.list);
                    post("/delete/:id", ProductSetCategoryController.delete);
                    post("/update", ProductSetCategoryController.update);
                    post("/insert", ProductSetCategoryController.insert);
                });
                path("/product-set", () -> {
                    post("/find", ProductSetController.find);
                    post("/count", ProductSetController.count);
                    get("/load/:id", ProductSetController.load);
                    post("/delete/:id", ProductSetController.delete);
                    post("/update", ProductSetController.update);
                    post("/insert", ProductSetController.insert);
                });
                path("/product-tag", () -> {
                    get("/list", ProductTagController.list);
                    post("/insert", ProductTagController.insert);
                    post("/delete/:id", ProductTagController.delete);
                    post("/update", ProductTagController.update);
                });
                path("/product-tags", () -> {
                    post("/insert/:productId", ProductTagsController.insert);
                });
                path("/catalog_param", () -> {
                    get("/list/:catalogId", CatalogParameterController.list);
                    post("/save", CatalogParameterController.update);
                    post("/delete/:id", CatalogParameterController.delete);
                    post("/params_list", CatalogParameterController.listForProductValues);
                    post("/params_concat", CatalogParameterController.listGroupConcat);
                });
                path("/product_catalog", () -> {
                    get("/allList", CatalogController.allList);
                    get("/list", CatalogController.list);
                    post("/effective_list", CatalogController.effectiveList);
                    post("/public_effective_list", CatalogController.publicEffectiveList);
                    post("/delete/:id", CatalogController.delete);
                    post("/update", CatalogController.update);
                    post("/insert", CatalogController.insert);
                });
                path("/record", () -> {
                    post("/find", ProductRecordController.find);
                    post("/count", ProductRecordController.count);
                    post("/accountBeDownloadedCount", ProductRecordController.accountBeDownloadedCount);
                    post("/accountDownloadDetail", ProductRecordController.accountDownloadDetail);
                    post("/productData", ProductRecordController.productData);
                    post("/downloadedProductDetail", ProductRecordController.downloadedProductDetail);
                });
            });


            path("/quo", () -> {
                path("/project", () -> {
                    post("/find", ProjectController.quotedProjectFind);
                    post("/count", ProjectController.quotedProjectCount);
                    get("/list", ProjectController.list);
                    get("/load/:id", ProjectController.load);
                    post("/delete/:id", ProjectController.delete);
                    post("/update", ProjectController.update);
                    post("/insert", ProjectController.insert);
                });
                path("/quotation-bill", () -> {
                    get("/list/:projectId", QuotationBillController.list);
                    get("/displayList", QuotationBillController.displayList);
                    get("/count/:projectId", QuotationBillController.count);
                    get("/load/:id", QuotationBillController.load);
                    post("/delete/:id", QuotationBillController.delete);
                    post("/update", QuotationBillController.update);
                    post("/insert", QuotationBillController.insert);
                    get("/insertAll", QuotationBillController.insertAll);
                    get("/create-mp-qrcode/:id", QuotationBillController.createMPQRCode);
                });
                path("/quotation-template", () -> {
                    get("/list", QuotationTemplateController.list);
                    post("/clearQuotationBookId", QuotationTemplateController.clearQuotationBookId);
                });
                path("/present-bill", () -> {
                    get("/list/:billId", PresentBillController.list);
                    post("/insert", PresentBillController.insert);
                    post("/find", PresentBillController.find);
                    post("/count", PresentBillController.count);
                    get("/list-details/:toAccountId/:fromAccountId/:quotationBillId", PresentBillController.listDetails);
                    post("/delete/:toAccountId/:fromAccountId/:quotationBillId", PresentBillController.delete);
                });
            });


            path("/friend", () -> {
                path("/friend-group", () -> {
                    get("/list", FriendGroupController.list);
                    post("/delete/:groupId", FriendGroupController.delete);
                    post("/update", FriendGroupController.update);
                    post("/insert", FriendGroupController.insert);
                });
                path("/friend", () -> {
                    post("/find", FriendController.find);
                    post("/count", FriendController.count);
                    get("/load/:friendAccountId", FriendController.load);
                    post("/delete/:friendAccountId", FriendController.delete);
                    post("/accept/:friendAccountId", FriendController.accept);
                    post("/update", FriendController.update);
                    post("/insert", FriendController.insert);
                    get("/my-invite-code", FriendController.myInviteCode);
                    post("/canVisit", FriendController.canVisit);
                });
                path("/friend-sale-price", () -> {
                    post("/update", FriendSalePriceController.update);
                    post("/insert", FriendSalePriceController.insert);
                    post("/delete/:id", FriendSalePriceController.delete);
                    post("/batchDelete", FriendSalePriceController.batchDelete);
                    post("/batchInsertOrUpdate", FriendSalePriceController.batchInsertOrUpdate);
                });
                path("/share-product", () -> {
                    get("/list-product-tag/:accountId", ShareProductController.listProductTag);
                    post("/find-product/:accountId", ShareProductController.findProduct);
                    post("/count-product/:accountId", ShareProductController.countProduct);
                    get("/load-product/:accountId/:productId", ShareProductController.loadProduct);
                });

                path("/productShare", () -> {
                    post("/insert", ProductShareController.insert);
                    post("/update", ProductShareController.update);
                    post("/batchDelete", ProductShareController.batchDelete);
                    post("/findShareProductByAccountBySelf", ProductShareController.findShareProductByAccountBySelf);//我发送给别人
                    post("/findSharedProductByAccount", ProductShareController.findSharedProductByAccount);//获取指定公司分享给我的产品
                    post("/findSharedProduct", ProductShareController.findSharedProduct);
                    get("/load-product/:accountId/:productId", ProductShareController.loadProduct);
                    post("/batchInsertByShare", ProductShareController.batchInsertByShare);
                });
            });

            path("/people", () -> {
                post("/find", PeopleController.find);
                post("/count", PeopleController.count);
                get("/load/:id", PeopleController.load);
                get("/load-people-baseInfo/:id", PeopleController.loadPeopleBaseInfo);
                get("/list", PeopleController.list);
                post("/delete/:id", PeopleController.delete);
                post("/safeDelete", PeopleController.safeDelete);
                post("/update", PeopleController.update);
                post("/insert", PeopleController.insert);
            });

            path("/content", () -> {
                path("/wx-news", () -> {
                    post("/gather", WxNewsController.gatherOSS);
                });
            });

            path("/record-msg", () -> {
                get("/list/:recordId", RecordMsgController.list);
                post("/insert", RecordMsgController.insert);
                post("/delete/:id", RecordMsgController.delete);
            });

            path("/province", () -> {
                get("/list", ProvinceController.list);
            });

            path("/city", () -> {
                post("/find", CityController.find);
            });

            path("/record-static", () -> {
                post("/recordStaticByUser", RecordStaticController.recordStaticByUser);
                post("/recordDayStatic", RecordStaticController.recordDayStatic);
            });

            path("/record-report", () -> {
                get("/load-user/:accountId/:userId", RecordReportController.loadUser);
                post("/find/:accountId", RecordReportController.find);
                get("/load/:accountId/:id", RecordReportController.load);
            });

            //管理中心
            path("/manage-center", () -> {
                post("/audit-list/count", AuditProductController.count);
                post("/audit-list/find", AuditProductController.find);
                post("/audit-list/audit", AuditProductController.audit);
            });
            //消息中心
            path("/message-center", () -> {
                post("/count", MessageController.count);
                post("/find", MessageController.find);
            });
            path("/message", () -> {
                post("/find", GoEasyMessageController.find);
                post("/alreadyRead", GoEasyMessageController.alreadyRead);
                get("/getSecretKey", GoEasyMessageController.getSecretKey);
                post("/publish", GoEasyMessageController.publish);
                post("/publishAll", GoEasyMessageController.publishAll);
            });
            //城市信息
            path("/county", () -> {
                post("/list", CountyController.list);
            });

            //区域查询
            path("/area", () -> {
                post("/list", AreaInfoController.findAll);
                get("/adCode", AreaInfoController.findByAdCode);
                post("/findList", AreaInfoController.findList); //获取所有的数据信息

            });
        });
    }
}
