package com.forcpacebj.api.business;

import com.alibaba.fastjson.JSONObject;
import com.forcpacebj.api.entity.*;
import com.forcpacebj.api.utils.CollectionUtil;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.halt;

public class ProjectBusiness {

    public static List<ProjectInfo> quotedProjectFind(Map conditions) {

        val sql = " SELECT P.ProjectId,P.ProjectName,PS.StageName 'stage.stageName',PS.Type 'stage.type'," +
                  "P.OwnerName 'owner.peopleName',P.CreateUserId 'createUser.userId'," +
                  "P.CreateUserName 'createUser.userName',P.ModifyDateTime,P.IsImportant,CS.Name 'constructStage.name'" +
                  " FROM tbProject P " +
                  " LEFT JOIN ProjectStage PS ON PS.Id = P.StageId AND PS.IsShow=1 " +
                  " LEFT JOIN ConstructStage CS ON CS.Id = P.ConstructStageId " +
                  " WHERE P.AccountId= :accountId AND P.State = :state AND P.StageId IS NOT NULL " +
                  "   AND (P.DepartmentId=:departmentId OR :departmentId IS NULL)" +
                  "   AND (P.ProjectName LIKE CONCAT('%',:projectName,'%') OR :projectName is null) " +
                  "   AND (P.OwnerName LIKE CONCAT('%',:owner,'%') OR :owner is null) " +
                  "   AND ( P.CreateUserId = :userId OR P.ProjectId in (select ProjectId from tbProjectUser where UserId = :userId ) OR :userId is null ) " +
                  "   AND ( P.ProjectName LIKE CONCAT('%',:all,'%') OR P.OwnerName LIKE CONCAT('%',:all,'%') OR P.CreateUserName LIKE CONCAT('%',:all,'%') OR :all is null ) " +
                  "   AND ( P.ProjectName LIKE CONCAT('%',:projectName,'%') OR :projectName is null ) " +
                  "   AND ( P.ProjectType = :projectType OR :projectType is null )" +
                  "   AND ( P.OwnerId = :ownerId OR :ownerId is null ) " +
                  "   AND ( P.SalesManId = :salesManId OR :salesManId is null ) " +
                  "   AND ( P.SalesManId = :userId OR P.ChargerId = :userId OR P.DesignerId = :userId OR P.HandoverUserId=:userId OR P.CreateUserId = :userId OR P.ProjectId in (select ProjectId from tbProjectUser where UserId = :userId ) OR :userId is null ) " +
                  "   AND ( P.CreateUserId = :createUserId OR :createUserId is null)" +
                  "   AND ( ( P.CreateDateTime Between :createStartDate and :createEndDate ) Or (:createStartDate is null and :createEndDate is null)) " +
                  "   AND ( IfNull(P.StageId,'') = :stageId OR :stageId is null ) " +
                  "   AND ( PS.Type = :stageType OR :stageType is null ) " +
                  "   AND ( P.IsImportant = :isImportant OR :isImportant IS NULL) " +
                  " ORDER BY P.ModifyDateTime DESC " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            Object createStartDate = null, createEndDate = null;

            if (conditions.containsKey("createDateTime")) {
                val dateRangeValue = (Map) conditions.get("createDateTime");
                createStartDate = dateRangeValue.get("startDate");
                createEndDate = dateRangeValue.get("endDate");
            }
            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("projectName", conditions.get("projectName"))
                    .addParameter("owner", conditions.get("owner"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("all", conditions.get("all"))
                    .addParameter("projectType", conditions.get("projectType"))
                    .addParameter("ownerId", conditions.get("ownerId"))
                    .addParameter("salesManId", conditions.get("salesManId"))
                    .addParameter("createUserId", conditions.get("createUserId"))
                    .addParameter("createStartDate", createStartDate)
                    .addParameter("createEndDate", createEndDate)
                    .addParameter("stageId", conditions.get("stageId"))
                    .addParameter("stageType", conditions.get("stageType"))
                    .addParameter("isImportant", conditions.get("isImportant"))
                    .addParameter("state", conditions.get("state") == null ? 1 : conditions.get("state"))
                    .executeAndFetch(ProjectInfo.class);
        }
    }

    public static int quotedProjectCount(Map conditions) {

        val sql = " SELECT count(1) xcount FROM tbProject P " +
                  " LEFT JOIN ProjectStage PS ON PS.Id = P.StageId AND PS.IsShow=1 " +
                  " WHERE P.AccountId= :accountId AND P.State = :state AND P.StageId IS NOT NULL " +
                  "   AND (P.DepartmentId=:departmentId OR :departmentId IS NULL) " +
                  "   AND (P.ProjectName LIKE CONCAT('%',:projectName,'%') OR :projectName is null) " +
                  "   AND (P.CreateUserId = :userId OR P.ProjectId in (select ProjectId from tbProjectUser where UserId = :userId ) OR :userId is null ) " +
                  "   AND (P.OwnerName LIKE CONCAT('%',:owner,'%') OR :owner is null) " +
                  "   AND ( P.ProjectName LIKE CONCAT('%',:all,'%') OR P.OwnerName LIKE CONCAT('%',:all,'%') OR P.CreateUserName LIKE CONCAT('%',:all,'%') OR :all is null ) " +
                  "   AND ( P.ProjectName LIKE CONCAT('%',:projectName,'%') OR :projectName is null ) " +
                  "   AND ( P.ProjectType = :projectType OR :projectType is null )" +
                  "   AND ( P.OwnerId = :ownerId OR :ownerId is null ) " +
                  "   AND ( P.SalesManId = :salesManId OR :salesManId is null ) " +
                  "   AND ( P.SalesManId = :userId OR P.ChargerId = :userId OR P.DesignerId = :userId OR P.HandoverUserId=:userId OR P.CreateUserId = :userId OR P.ProjectId in (select ProjectId from tbProjectUser where UserId = :userId ) OR :userId is null ) " +
                  "   AND ( P.CreateUserId = :createUserId OR :createUserId is null)" +
                  "   AND ( ( P.CreateDateTime Between :createStartDate and :createEndDate ) Or (:createStartDate is null and :createEndDate is null)) " +
                  "   AND ( IfNull(P.StageId,'') = :stageId OR :stageId is null ) " +
                  "   AND ( PS.Type = :stageType OR :stageType is null ) " +
                  "   AND ( P.IsImportant = :isImportant OR :isImportant IS NULL)";

        try (val con = db.sql2o.open()) {
            Object createStartDate = null, createEndDate = null;

            if (conditions.containsKey("createDateTime")) {
                val dateRangeValue = (Map) conditions.get("createDateTime");
                createStartDate = dateRangeValue.get("startDate");
                createEndDate = dateRangeValue.get("endDate");
            }
            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("projectName", conditions.get("projectName"))
                    .addParameter("owner", conditions.get("owner"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("all", conditions.get("all"))
                    .addParameter("projectType", conditions.get("projectType"))
                    .addParameter("ownerId", conditions.get("ownerId"))
                    .addParameter("salesManId", conditions.get("salesManId"))
                    .addParameter("createUserId", conditions.get("createUserId"))
                    .addParameter("createStartDate", createStartDate)
                    .addParameter("createEndDate", createEndDate)
                    .addParameter("stageId", conditions.get("stageId"))
                    .addParameter("stageType", conditions.get("stageType"))
                    .addParameter("isImportant", conditions.get("isImportant"))
                    .addParameter("state", conditions.get("state") == null ? 1 : conditions.get("state"))
                    .executeScalar(int.class);
        }
    }

    public static List<ProjectInfo> find(Map conditions) {

        val sql = " SELECT P.ProjectId ,P.ProjectName , P.ProjectType ,P.ProjectNature ,P.OwnerId 'owner.peopleId' ,P.OwnerName 'owner.peopleName'," +
                  " P.SalesManId 'salesMan.userId' ,P.SalesManName 'salesMan.userName' ,P.DesignerId 'designer.userId' ,P.DesignerName 'designer.userName' ," +
                  " P.ChargerId 'charger.userId' ,P.ChargerName 'charger.userName' ,P.ProjectSource ," +
                  " P.ProvinceId 'province.provinceId',V.ProvinceName 'province.provinceName' ,P.CityId 'city.cityId',C.CityName 'city.cityName' ,P.Area ," +
                  " P.ProjectProgress ,PS.Id 'stage.id', PS.StageName 'stage.stageName',PS.Type 'stage.type' ,P.ContractDate ,P.EstimateContractDate ,P.EstimateContractMoney ,P.DealRate ,P.DealContractMoney ,P.Commision ,P.OtherCost ,P.DeviceCost ," +
                  " P.BuildCost ,P.TotalCost ,P.Remark ," +
                  " P.CreateUserId 'createUser.userId' ,P.CreateUserName 'createUser.userName' ,P.CreateDateTime , " +
                  " P.ModifyUserId 'modifyUser.userId' ,P.ModifyUserName 'modifyUser.userName' ,P.ModifyDateTime ," +
                  " CS.Name 'constructStage.name'" +
                  " From tbProject P " +
                  " LEFT JOIN ProjectStage PS ON PS.Id = P.StageId " +
                  " LEFT JOIN ConstructStage CS ON CS.Id = P.ConstructStageId " +
                  " left join tbProvince V on P.ProvinceId=V.ProvinceId " +
                  " left join tbCity C on P.CityId=C.CityId " +
                  " WHERE P.AccountId= :accountId AND P.State = :state " +
                  "   AND (P.DepartmentId=:departmentId OR :departmentId IS NULL)" +
                  "   AND ( P.ProjectName LIKE CONCAT('%',:projectName,'%') OR :projectName is null ) " +
                  "   AND ( P.ProjectType = :projectType OR :projectType is null )" +
                  "   AND ( P.OwnerId = :ownerId OR :ownerId is null ) " +
                  "   AND ( P.OwnerName LIKE CONCAT('%',:ownerName,'%') OR :ownerName is null ) " +
                  "   AND ( P.SalesManId = :salesManId OR :salesManId is null ) " +
                  "   AND ( P.SalesManId = :userId OR P.ChargerId = :userId OR P.DesignerId = :userId OR P.HandoverUserId=:userId OR P.CreateUserId = :userId OR P.ProjectId in (select ProjectId from tbProjectUser where UserId = :userId ) OR :userId is null ) " +
                  "   AND (P.CreateUserId = :createUserId OR :createUserId is null)" +
                  "   AND ( ( P.CreateDateTime Between :createStartDate and :createEndDate ) Or (:createStartDate is null and :createEndDate is null)) " +
                  "   AND ( IfNull(P.StageId,'') = :stageId OR :stageId is null ) " +
                  "   AND ( PS.Type = :stageType OR :stageType is null ) " +
                  "   AND ( P.IsImportant = :isImportant OR :isImportant IS NULL) " +
                  " ORDER BY P.ModifyDateTime DESC " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {

            Object createStartDate = null, createEndDate = null;

            if (conditions.containsKey("createDateTime")) {
                val dateRangeValue = (Map) conditions.get("createDateTime");
                createStartDate = dateRangeValue.get("startDate");
                createEndDate = dateRangeValue.get("endDate");
            }

            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("projectName", conditions.get("projectName"))
                    .addParameter("projectType", conditions.get("projectType"))
                    .addParameter("ownerId", conditions.get("ownerId"))
                    .addParameter("ownerName", conditions.get("ownerName"))
                    .addParameter("salesManId", conditions.get("salesManId"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("createUserId", conditions.get("createUserId"))
                    .addParameter("createStartDate", createStartDate)
                    .addParameter("createEndDate", createEndDate)
                    .addParameter("stageId", conditions.get("stageId"))
                    .addParameter("stageType", conditions.get("stageType"))
                    .addParameter("isImportant", conditions.get("isImportant"))
                    .addParameter("state", conditions.get("state") == null ? 1 : conditions.get("state"))
                    .executeAndFetch(ProjectInfo.class);
        }
    }

    public static int count(Map conditions) {

        val sql = " SELECT count(1) xcount FROM tbProject P" +
                  " LEFT JOIN ProjectStage PS ON PS.Id = P.StageId " +
                  " WHERE P.AccountId= :accountId AND P.State = :state " +
                  "   AND (P.DepartmentId=:departmentId OR :departmentId IS NULL) " +
                  "   AND (P.ProjectName LIKE CONCAT('%',:projectName,'%') OR :projectName is null) " +
                  "   AND ( P.ProjectType = :projectType OR :projectType is null )" +
                  "   AND ( P.OwnerId = :ownerId OR :ownerId is null ) " +
                  "   AND ( P.OwnerName LIKE CONCAT('%',:ownerName,'%') OR :ownerName is null ) " +
                  "   AND ( P.SalesManId = :salesManId OR :salesManId is null ) " +
                  "   AND ( P.SalesManId = :userId OR P.ChargerId = :userId OR P.DesignerId = :userId OR P.HandoverUserId=:userId OR P.CreateUserId = :userId OR P.ProjectId in (select ProjectId from tbProjectUser where UserId = :userId ) OR :userId is null ) " +
                  "   AND ( P.CreateUserId = :createUserId OR :createUserId is null)" +
                  "   AND ( ( P.CreateDateTime Between :createStartDate and :createEndDate ) Or (:createStartDate is null and :createEndDate is null)) " +
                  "   AND ( IfNull(P.StageId,'') = :stageId OR :stageId is null ) " +
                  "   AND ( PS.Type = :stageType OR :stageType is null ) " +
                  "   AND ( P.IsImportant = :isImportant OR :isImportant IS NULL)";

        try (val con = db.sql2o.open()) {

            Object createStartDate = null, createEndDate = null;

            if (conditions.containsKey("createDateTime")) {
                val dateRangeValue = (Map) conditions.get("createDateTime");
                createStartDate = dateRangeValue.get("startDate");
                createEndDate = dateRangeValue.get("endDate");
            }

            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("projectName", conditions.get("projectName"))
                    .addParameter("projectType", conditions.get("projectType"))
                    .addParameter("ownerId", conditions.get("ownerId"))
                    .addParameter("ownerName", conditions.get("ownerName"))
                    .addParameter("salesManId", conditions.get("salesManId"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("createUserId", conditions.get("createUserId"))
                    .addParameter("createStartDate", createStartDate)
                    .addParameter("createEndDate", createEndDate)
                    .addParameter("stageId", conditions.get("stageId"))
                    .addParameter("stageType", conditions.get("stageType"))
                    .addParameter("isImportant", conditions.get("isImportant"))
                    .addParameter("state", conditions.get("state") == null ? 1 : conditions.get("state"))
                    .executeScalar(int.class);
        }
    }

    public static List<ProjectInfo> findKanbanProjects(Map conditions) {

        val sql = " SELECT P.ProjectId ,P.ProjectName , P.ProjectType ,P.ProjectNature ,P.OwnerId 'owner.peopleId' ,P.OwnerName 'owner.peopleName'," +
                  " P.SalesManId 'salesMan.userId' ,P.SalesManName 'salesMan.userName' ,P.DesignerId 'designer.userId' ,P.DesignerName 'designer.userName' ," +
                  " P.ChargerId 'charger.userId' ,P.ChargerName 'charger.userName' ,P.ProjectSource ," +
                  " P.ProvinceId 'province.provinceId',V.ProvinceName 'province.provinceName' ,P.CityId 'city.cityId',C.CityName 'city.cityName' ,P.Area ," +
                  " P.ProjectProgress ,PS.StageName 'stage.stageName',PS.Type 'stage.type',P.ContractDate ,P.EstimateContractDate ,P.EstimateContractMoney ,P.DealRate ,P.DealContractMoney ,P.Commision ,P.OtherCost ,P.DeviceCost ," +
                  " P.BuildCost ,P.TotalCost ,P.Remark ," +
                  " P.CreateUserId 'createUser.userId' ,P.CreateUserName 'createUser.userName' ,P.CreateDateTime , " +
                  " P.ModifyUserId 'modifyUser.userId' ,P.ModifyUserName 'modifyUser.userName' ,P.ModifyDateTime," +
                  " P.IsImportant,CS.Name 'constructStage.name'" +
                  " From tbProject P " +
                  " LEFT JOIN ProjectStage PS ON PS.Id = P.StageId " +
                  " LEFT JOIN ConstructStage CS ON CS.Id = P.ConstructStageId " +
                  " left join tbProvince V on P.ProvinceId=V.ProvinceId " +
                  " left join tbCity C on P.CityId=C.CityId " +
                  " WHERE P.AccountId= :accountId AND P.State = 1 " +
                  "   AND (P.DepartmentId=:departmentId OR :departmentId IS NULL) " +
                  "   AND ( P.ProjectName LIKE CONCAT('%',:projectName,'%') OR :projectName is null ) " +
                  "   AND ( P.ProjectType = :projectType OR :projectType is null )" +
                  "   AND ( P.OwnerId = :ownerId OR :ownerId is null ) " +
                  "   AND ( P.projectStage is not null ) " +
                  "   AND ( P.SalesManId = :salesManId OR :salesManId is null ) " +
                  "   AND ( P.SalesManId = :userId OR P.ChargerId = :userId OR P.HandoverUserId=:userId OR P.DesignerId = :userId OR P.CreateUserId = :userId OR P.ProjectId in (select ProjectId from tbProjectUser where UserId = :userId ) OR :userId is null ) " +
                  "   AND (P.CreateUserId = :createUserId OR :createUserId is null)" +
                  "   AND ( ( P.CreateDateTime Between :createStartDate and :createEndDate ) Or (:createStartDate is null and :createEndDate is null)) " +
                  "   AND ( IfNull(P.StageId,'') = :stageId OR :stageId is null ) " +
                  "   AND ( PS.Type = :stageType OR :stageType is null ) " +
                  "   AND ( IsImportant = :isImportant OR :isImportant IS NULL) " +
                  " ORDER BY P.ModifyDateTime DESC " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {

            Object createStartDate = null, createEndDate = null;

            if (conditions.containsKey("createDateTime")) {
                val dateRangeValue = (Map) conditions.get("createDateTime");
                createStartDate = dateRangeValue.get("startDate");
                createEndDate = dateRangeValue.get("endDate");
            }

            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("projectName", conditions.get("projectName"))
                    .addParameter("projectType", conditions.get("projectType"))
                    .addParameter("ownerId", conditions.get("ownerId"))
                    .addParameter("salesManId", conditions.get("salesManId"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("createUserId", conditions.get("createUserId"))
                    .addParameter("createStartDate", createStartDate)
                    .addParameter("createEndDate", createEndDate)
                    .addParameter("stageId", conditions.get("stageId"))
                    .addParameter("stageType", conditions.get("stageType"))
                    .addParameter("isImportant", conditions.get("isImportant"))
                    .executeAndFetch(ProjectInfo.class);
        }
    }

    public static List<ProjectInfo> list(Map conditions) {

        val sql = " SELECT P.ProjectId ,P.ProjectName , P.ProjectType ,P.ProjectNature ,P.OwnerId 'owner.peopleId' ,P.OwnerName 'owner.peopleName'," +
                  " P.SalesManId 'salesMan.userId' ,P.SalesManName 'salesMan.userName' ,P.DesignerId 'designer.userId' ,P.DesignerName 'designer.userName' ," +
                  " P.ChargerId 'charger.userId' ,P.ChargerName 'charger.userName' ,P.ProjectSource ," +
                  " P.ProvinceId 'province.provinceId',V.ProvinceName 'province.provinceName' ,P.CityId 'city.cityId',C.CityName 'city.cityName' ,P.Area ," +
                  " P.ProjectProgress ,PS.StageName 'stage.stageName',PS.Type 'stage.type', P.ContractDate ,P.EstimateContractDate ,P.EstimateContractMoney ,P.DealRate ,P.DealContractMoney ,P.Commision ,P.OtherCost ,P.DeviceCost ," +
                  " P.BuildCost ,P.TotalCost ,P.Remark ," +
                  " P.CreateUserId 'createUser.userId' ,P.CreateUserName 'createUser.userName' ,P.CreateDateTime , " +
                  " P.ModifyUserId 'modifyUser.userId' ,P.ModifyUserName 'modifyUser.userName' ,P.ModifyDateTime," +
                  " P.IsImportant,CS.Name 'constructStage.name' " +
                  " From tbProject P " +
                  " LEFT JOIN ProjectStage PS ON PS.Id = P.StageId " +
                  " LEFT JOIN ConstructStage CS ON CS.Id = P.ConstructStageId " +
                  " left join tbProvince V on P.ProvinceId=V.ProvinceId " +
                  " left join tbCity C on P.CityId=C.CityId " +
                  " where P.AccountId= :accountId AND P.State = 1 " +
                  "   AND (P.DepartmentId=:departmentId OR :departmentId IS NULL) " +
                  "   AND ( IfNull(P.StageId,'') = :stageId OR :stageId is null ) " +
                  "   AND ( P.SalesManId = :userId OR P.ChargerId = :userId OR P.DesignerId = :userId OR P.HandoverUserId=:userId OR P.CreateUserId = :userId OR P.ProjectId in (select ProjectId from tbProjectUser where UserId = :userId ) OR :userId is null ) ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("stageId", conditions.get("stageId"))
                    .executeAndFetch(ProjectInfo.class);
        }
    }

    public static ProjectInfo load(String accountId, String id) {

        val sql = " SELECT P.AccountId,P.ProjectId ,P.ProjectName , P.ProjectType ,P.ProjectNature ,P.OwnerId 'owner.peopleId' ,P.OwnerName 'owner.peopleName'," +
                  " P.SalesManId 'salesMan.userId' ,P.SalesManName 'salesMan.userName' ,P.DesignerId 'designer.userId' ,P.DesignerName 'designer.userName' ," +
                  " P.ChargerId 'charger.userId' ,P.ChargerName 'charger.userName' ,P.ProjectSource ," +
                  " P.ProvinceId 'province.provinceId',V.ProvinceName 'province.provinceName' ,P.CityId 'city.cityId',C.CityName 'city.cityName' ,P.Area ," +
                  " P.ProjectProgress ,PS.Id 'stage.id', PS.StageName 'stage.stageName',PS.Type 'stage.type',P.ContractDate ,P.EstimateContractDate ,P.EstimateContractMoney ,P.DealRate ,P.DealContractMoney ,P.Commision ,P.OtherCost ,P.DeviceCost ," +
                  " P.BuildCost ,P.TotalCost ,P.Remark ," +
                  " P.CreateUserId 'createUser.userId' ,P.CreateUserName 'createUser.userName' ,P.CreateDateTime , " +
                  " P.ModifyUserId 'modifyUser.userId' ,P.ModifyUserName 'modifyUser.userName' ,P.ModifyDateTime," +
                  " P.IsImportant, P.Installment, P.PaidInstallment, P.ContractFileUrl, P.ContractFileName, CS.Id 'constructStage.id', CS.Name 'constructStage.name' " +
                  " From tbProject P " +
                  " LEFT JOIN ProjectStage PS ON PS.Id = P.StageId " +
                  " LEFT JOIN ConstructStage CS ON CS.Id = P.ConstructStageId " +
                  " left join tbProvince V on P.ProvinceId=V.ProvinceId " +
                  " left join tbCity C on P.CityId=C.CityId " +
//                  " WHERE (P.AccountId= :accountId OR :id LIKE '%Stage') AND P.ProjectId = :id";
                  " WHERE  P.ProjectId = :id";

        try (val con = db.sql2o.open()) {

            val project = con.createQuery(sql)
//                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeAndFetchFirst(ProjectInfo.class);

            if (project != null) {
                val projectCooperatorList = con.createQuery(
                        "select UserId 'user.userId' ,UserName 'user.userName'  " +
                        " from tbProjectCooperator" +
                        " where AccountId= :accountId And ProjectId = :id order by sortNumber")
                        .addParameter("accountId", accountId)
                        .addParameter("id", id)
                        .executeAndFetch(ProjectCooperatorInfo.class);
                if (CollectionUtil.isNotEmpty(projectCooperatorList)) {
                    project.setProjectCooperatorList(projectCooperatorList);
                }
                val projectRelatedList = con.createQuery(
                        "select RelatedPeopleId 'relatedPeople.peopleId' ,RelatedPeopleName 'relatedPeople.peopleName' ,RelatedPeopleRole  " +
                        " from tbProjectRelated" +
                        " where AccountId= :accountId And ProjectId = :id order by sortNumber")
                        .addParameter("accountId", accountId)
                        .addParameter("id", id)
                        .executeAndFetch(ProjectRelatedInfo.class);
                if (CollectionUtil.isNotEmpty(projectRelatedList)) {
                    project.setProjectRelatedList(projectRelatedList);
                }
                val projectUserList = con.createQuery(
                        "select UserId 'user.userId' ,UserName 'user.userName'  " +
                        " from tbProjectUser" +
                        " where AccountId= :accountId And ProjectId = :id order by sortNumber")
                        .addParameter("accountId", accountId)
                        .addParameter("id", id)
                        .executeAndFetch(ProjectUserInfo.class);
                if (CollectionUtil.isNotEmpty(projectUserList)) {
                    project.setProjectUserList(projectUserList);
                }
            }

            return project;
        }
    }

    public static void insert(ProjectInfo project) {
        if (checkProjectLimit(project.getAccountId())) throw halt(400, "项目数量超过限制");
        setDefaultOfNullValue(project);
        val sql = "INSERT INTO tbProject(AccountId, DepartmentId,ProjectId ,ProjectName ,ProjectType ,ProjectNature ,OwnerId  ,OwnerName ," +
                  " SalesManId ,SalesManName ,DesignerId ,DesignerName ,ChargerId ,ChargerName ," +
                  " ProjectSource ,ProvinceId ,CityId ,Area ,ProjectProgress ,StageId ," +
                  " ContractDate ,EstimateContractDate ,EstimateContractMoney ,DealRate ,DealContractMoney ,Commision ,OtherCost ,DeviceCost ," +
                  " BuildCost ,TotalCost ,Remark ,CreateUserId ,CreateUserName ,CreateDateTime ,ModifyUserId ,ModifyUserName ,ModifyDateTime," +
                  " IsImportant, Installment, PaidInstallment, ContractFileUrl, ContractFileName, ConstructStageId)" +
                  " values (:accountId, :departmentId,:projectId ,:projectName ,:projectType ,:projectNature ,:ownerId  ,:ownerName ," +
                  " :salesManId ,:salesManName ,:designerId ,:designerName ,:chargerId ,:chargerName ," +
                  " :projectSource ,:provinceId ,:cityId ,:area ,:projectProgress ,:stageId ," +
                  " :contractDate ,:estimateContractDate ,:estimateContractMoney ,:dealRate ,:dealContractMoney ,:commision ,:otherCost ,:deviceCost ," +
                  " :buildCost ,:totalCost ,:remark ,:createUserId ,:createUserName ,:createDateTime ,:modifyUserId ,:modifyUserName ,:modifyDateTime," +
                  " :isImportant, :installment, :paidInstallment, :contractFileUrl, :contractFileName, :constructStageId) ";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql).bind(project)
                    .addParameter("ownerId", project.getOwner().getPeopleId())
                    .addParameter("ownerName", project.getOwner().getPeopleName())
                    .addParameter("salesManId", project.getSalesMan().getUserId())
                    .addParameter("salesManName", project.getSalesMan().getUserName())
                    .addParameter("designerId", project.getDesigner().getUserId())
                    .addParameter("designerName", project.getDesigner().getUserName())
                    .addParameter("chargerId", project.getCharger().getUserId())
                    .addParameter("chargerName", project.getCharger().getUserName())
                    .addParameter("createUserId", project.getCreateUser().getUserId())
                    .addParameter("createUserName", project.getCreateUser().getUserName())
                    .addParameter("createDateTime", project.getCreateDateTime())
                    .addParameter("modifyUserId", project.getModifyUser().getUserId())
                    .addParameter("modifyUserName", project.getModifyUser().getUserName())
                    .addParameter("modifyDateTime", project.getModifyDateTime())
                    .addParameter("provinceId", project.getProvince().getProvinceId())
                    .addParameter("cityId", project.getCity().getCityId())
                    .addParameter("stageId", project.getStage().getId())
                    .addParameter("constructStageId", project.getConstructStage().getId())
                    .executeUpdate();

            val id = project.getProjectId();
            val accountId = project.getAccountId();

            if (CollectionUtil.isNotEmpty(project.getProjectCooperatorList())) {

                val cooperatorSql = "INSERT INTO tbProjectCooperator(AccountId ,ProjectId , SortNumber, UserId ,UserName) " +
                                    "Values(:accountId ,:id , :sortNumber ,:userId ,:userName  )";

                val query = con.createQuery(cooperatorSql);

                for (int i = 0; i < project.getProjectCooperatorList().size(); i++) {

                    val cooperator = project.getProjectCooperatorList().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("id", id);
                    query.addParameter("sortNumber", i);
                    query.addParameter("userId", cooperator.getUser().getUserId());
                    query.addParameter("userName", cooperator.getUser().getUserName());
                    query.addToBatch();
                }

                query.executeBatch();
            }

            if (CollectionUtil.isNotEmpty(project.getProjectRelatedList())) {

                val relatedSql = "INSERT INTO tbProjectRelated(AccountId ,ProjectId , SortNumber, RelatedPeopleId ,RelatedPeopleName ,RelatedPeopleRole) " +
                                 "Values( :accountId ,:id , :sortNumber ,:relatedPeopleId ,:relatedPeopleName ,:relatedPeopleRole  )";

                val query = con.createQuery(relatedSql);

                for (int i = 0; i < project.getProjectRelatedList().size(); i++) {

                    val related = project.getProjectRelatedList().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("id", id);
                    query.addParameter("sortNumber", i);
                    query.addParameter("relatedPeopleId", related.getRelatedPeople().getPeopleId());
                    query.addParameter("relatedPeopleName", related.getRelatedPeople().getPeopleName());
                    query.addParameter("relatedPeopleRole", related.getRelatedPeopleRole());
                    query.addToBatch();
                }

                query.executeBatch();
            }

            if (CollectionUtil.isNotEmpty(project.getProjectUserList())) {

                val userSql = "INSERT INTO tbProjectUser(AccountId ,ProjectId , SortNumber, UserId ,UserName) " +
                              "Values(:accountId ,:id , :sortNumber ,:userId ,:userName  )";

                val query = con.createQuery(userSql);

                for (int i = 0; i < project.getProjectUserList().size(); i++) {

                    val user = project.getProjectUserList().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("id", id);
                    query.addParameter("sortNumber", i);
                    query.addParameter("userId", user.getUser().getUserId());
                    query.addParameter("userName", user.getUser().getUserName());
                    query.addToBatch();
                }

                query.executeBatch();
            }

            con.commit();
        }
    }

    public static void update(String accountId, ProjectInfo project) {

        setDefaultOfNullValue(project);

        val sql = "UPDATE tbProject SET " +
                  " ProjectId= :projectId ," +
                  " ProjectName= :projectName ," +
                  " ProjectNature=:projectNature ," +
                  " OwnerId=:ownerId ," +
                  " OwnerName=:ownerName ," +
                  " SalesManId=:salesManId ," +
                  " SalesManName=:salesManName ," +
                  " DesignerId=:designerId ," +
                  " DesignerName=:designerName ," +
                  " ChargerId=:chargerId ," +
                  " ChargerName=:chargerName ," +
                  " ProjectSource=:projectSource," +
                  " ProvinceId=:provinceId ," +
                  " CityId=:cityId ," +
                  " Area=:area ," +
                  " ProjectProgress=:projectProgress ," +
                  " StageId=:stageId ," +
                  " ContractDate= :contractDate ," +
                  " EstimateContractDate= :estimateContractDate ," +
                  " EstimateContractMoney=:estimateContractMoney ," +
                  " DealRate=:dealRate ," +
                  " DealContractMoney=:dealContractMoney ," +
                  " Commision=:commision ," +
                  " OtherCost=:otherCost ," +
                  " DeviceCost=:deviceCost ," +
                  " BuildCost=:buildCost ," +
                  " TotalCost=:totalCost ," +
                  " Remark=:remark ," +
                  " ModifyUserId=:modifyUserId ," +
                  " ModifyUserName=:modifyUserName ," +
                  " ModifyDateTime=:modifyDateTime," +
                  " IsImportant=:isImportant," +
                  " Installment=:installment," +
                  " PaidInstallment=:paidInstallment," +
                  " ContractFileUrl=:contractFileUrl," +
                  " ContractFileName=:contractFileName," +
                  " ConstructStageId=:constructStageId " +
                  " WHERE AccountId= :accountId And ProjectId = :projectId ";

        try (val con = db.sql2o.beginTransaction()) {

            con.createQuery(sql).bind(project)
                    .addParameter("accountId", accountId)
                    .addParameter("ownerId", project.getOwner().getPeopleId())
                    .addParameter("ownerName", project.getOwner().getPeopleName())
                    .addParameter("salesManId", project.getSalesMan().getUserId())
                    .addParameter("salesManName", project.getSalesMan().getUserName())
                    .addParameter("designerId", project.getDesigner().getUserId())
                    .addParameter("designerName", project.getDesigner().getUserName())
                    .addParameter("chargerId", project.getCharger().getUserId())
                    .addParameter("chargerName", project.getCharger().getUserName())
                    .addParameter("modifyUserId", project.getModifyUser().getUserId())
                    .addParameter("modifyUserName", project.getModifyUser().getUserName())
                    .addParameter("modifyDateTime", project.getModifyDateTime())
                    .addParameter("provinceId", project.getProvince().getProvinceId())
                    .addParameter("cityId", project.getCity().getCityId())
                    .addParameter("stageId", project.getStage().getId())
                    .addParameter("constructStageId", project.getConstructStage().getId())
                    .executeUpdate();

            val id = project.getProjectId();
            Arrays.asList(" DELETE FROM tbProjectCooperator WHERE AccountId= :accountId And ProjectId = :id ;",
                    " DELETE FROM tbProjectRelated WHERE AccountId= :accountId And ProjectId = :id ; ",
                    " DELETE FROM tbProjectUser WHERE AccountId= :accountId And ProjectId = :id ; ")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("accountId", accountId)
                                    .addParameter("id", id)
                                    .executeUpdate()
                    );

            if (CollectionUtil.isNotEmpty(project.getProjectCooperatorList())) {

                val cooperatorSql = "INSERT INTO tbProjectCooperator(AccountId ,ProjectId , SortNumber, UserId ,UserName) " +
                                    "Values(:accountId ,:id , :sortNumber ,:userId ,:userName  )";

                val query = con.createQuery(cooperatorSql);

                for (int i = 0; i < project.getProjectCooperatorList().size(); i++) {

                    val cooperator = project.getProjectCooperatorList().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("id", id);
                    query.addParameter("sortNumber", i);
                    query.addParameter("userId", cooperator.getUser().getUserId());
                    query.addParameter("userName", cooperator.getUser().getUserName());
                    query.addToBatch();
                }

                query.executeBatch();
            }

            if (CollectionUtil.isNotEmpty(project.getProjectRelatedList())) {

                val relatedSql = "INSERT INTO tbProjectRelated(AccountId ,ProjectId , SortNumber, RelatedPeopleId ,RelatedPeopleName ,RelatedPeopleRole) " +
                                 "Values( :accountId ,:id , :sortNumber ,:relatedPeopleId ,:relatedPeopleName ,:relatedPeopleRole  )";

                val query = con.createQuery(relatedSql);

                for (int i = 0; i < project.getProjectRelatedList().size(); i++) {

                    val related = project.getProjectRelatedList().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("id", id);
                    query.addParameter("sortNumber", i);
                    query.addParameter("relatedPeopleId", related.getRelatedPeople().getPeopleId());
                    query.addParameter("relatedPeopleName", related.getRelatedPeople().getPeopleName());
                    query.addParameter("relatedPeopleRole", related.getRelatedPeopleRole());
                    query.addToBatch();
                }

                query.executeBatch();
            }

            if (CollectionUtil.isNotEmpty(project.getProjectUserList())) {

                val userSql = "INSERT INTO tbProjectUser(AccountId ,ProjectId , SortNumber, UserId ,UserName) " +
                              "Values( :accountId ,:id , :sortNumber ,:userId ,:userName  )";

                val query = con.createQuery(userSql);

                for (int i = 0; i < project.getProjectUserList().size(); i++) {

                    val user = project.getProjectUserList().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("id", id);
                    query.addParameter("sortNumber", i);
                    query.addParameter("userId", user.getUser().getUserId());
                    query.addParameter("userName", user.getUser().getUserName());
                    query.addToBatch();
                }

                query.executeBatch();
            }
            con.commit();
        }
    }

    public static void updateProjectStage(String projectId, String stageId) {
        val sql = "UPDATE tbProject SET StageId = :stageId WHERE ProjectId = :projectId ";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("stageId", stageId)
                    .addParameter("projectId", projectId)
                    .executeUpdate();
        }
    }

    private static void setDefaultOfNullValue(ProjectInfo project) {

        if (project.getOwner() == null) {
            project.setOwner(new PeopleInfo());
        }
        if (project.getSalesMan() == null) {
            project.setSalesMan(new UserInfo());
        }
        if (project.getDesigner() == null) {
            project.setDesigner(new UserInfo());
        }
        if (project.getCharger() == null) {
            project.setCharger(new UserInfo());
        }
        if (project.getProvince() == null) {
            project.setProvince(new ProvinceInfo());
        }
        if (project.getCity() == null) {
            project.setCity(new CityInfo());
        }
        if (project.getStage() == null) project.setStage(new ProjectStageInfo());
        if (project.getConstructStage() == null) project.setConstructStage(new ConstructStageInfo());
    }

    public static void delete(String accountId, String id) {

        try (val con = db.sql2o.open()) {
            Arrays.asList(" DELETE FROM tbProjectCooperator WHERE AccountId= :accountId And ProjectId = :id ;",
                    " DELETE FROM tbProjectRelated WHERE AccountId= :accountId And ProjectId = :id ; ",
                    " DELETE FROM tbProjectUser WHERE AccountId= :accountId And ProjectId = :id ; ",
                    " DELETE FROM tbProjectRecord WHERE AccountId= :accountId And ProjectId = :id ; ",
                    " DELETE FROM tbProject WHERE AccountId= :accountId And ProjectId = :id ;")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("accountId", accountId)
                                    .addParameter("id", id)
                                    .executeUpdate()
                    );
        }
    }

    public static List<ProjectInfo> getToDelList() {
        val sql = "SELECT ProjectId,AccountId from tbProject WHERE state = 0 AND DATE_ADD(ModifyDateTime,INTERVAL 7 day) <= NOW()";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(ProjectInfo.class);
        }
    }

    public static void safeDelete(String accountId, String projectIds, Boolean state) {
        if (state != null && (state && checkProjectLimit(accountId))) throw halt(400, "项目数量超过限制");

        val sql = " UPDATE tbProject SET state = :state, ModifyDateTime=NOW() WHERE AccountId = :accountId AND ProjectId IN " + projectIds.replace("[", "(").replace("]", ")");

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).addParameter("state", state == null ? false : state)
                    .addParameter("accountId", accountId)
                    .executeUpdate();
        }
    }

    public static JSONObject getStageCount(Map condition) {
        val sql = "SELECT PS.StageName, COUNT(P.ProjectId) count FROM ProjectStage PS " +
                  " LEFT JOIN tbProject P ON PS.Id = P.StageId AND P.AccountId = :accountId " +
                  " AND ( P.DepartmentId=:departmentId OR :departmentId IS NULL) " +
                  " AND ( P.SalesManId = :userId OR P.ChargerId = :userId OR P.DesignerId = :userId OR P.CreateUserId = :userId OR P.ProjectId in (select ProjectId from tbProjectUser where UserId = :userId ) OR :userId is null ) " +
                  " WHERE (PS.type = :type OR :type IS NULL) AND PS.AccountId = :accountId GROUP BY PS.StageName ORDER BY PS.SortNum;";
        try (val con = db.sql2o.open()) {
            val list = con.createQuery(sql)
                    .addParameter("accountId", condition.get("accountId"))
                    .addParameter("departmentId", condition.get("departmentId"))
                    .addParameter("userId", condition.get("userId"))
                    .addParameter("type", condition.get("type"))
                    .executeAndFetch(ProjectStageInfo.class);
            JSONObject json = new JSONObject();
            for (ProjectStageInfo stage : list)
                json.put(stage.getStageName(), stage.getCount());
            return json;
        }
    }

    public static JSONObject getStageAllCount(Map condition) {
        val sql = "SELECT PS.StageName, COUNT(P.ProjectId) count FROM ProjectStage PS " +
                " LEFT JOIN tbProject P ON PS.Id = P.StageId AND P.AccountId = :accountId " +
                " AND ( P.DepartmentId=:departmentId OR :departmentId IS NULL) " +
//                " AND ( P.SalesManId = :userId OR P.ChargerId = :userId OR P.DesignerId = :userId OR P.CreateUserId = :userId OR P.ProjectId in (select ProjectId from tbProjectUser where UserId = :userId ) OR :userId is null ) " +
                " WHERE (PS.type = :type OR :type IS NULL) AND PS.AccountId = :accountId GROUP BY PS.StageName ORDER BY PS.SortNum;";
        try (val con = db.sql2o.open()) {
            val list = con.createQuery(sql)
                    .addParameter("accountId", condition.get("accountId"))
                    .addParameter("departmentId", condition.get("departmentId"))
//                    .addParameter("userId", condition.get("userId"))
                    .addParameter("type", condition.get("type"))
                    .executeAndFetch(ProjectStageInfo.class);
            JSONObject json = new JSONObject();
            for (ProjectStageInfo stage : list)
                json.put(stage.getStageName(), stage.getCount());
            return json;
        }
    }

    public static Boolean checkProjectLimit(String accountId) {
        val sql = "SELECT COUNT(P.ProjectId) >= IFNULL(A.ProjectLimit,0) value FROM tbAccount A " +
                  " LEFT JOIN tbProject P ON A.AccountId = P.AccountId " +
                  " WHERE A.AccountId = :accountId AND P.state = 1";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetchFirst(Boolean.class);
        }
    }

    public static void insertPublish(ProjectInfo project) {
        UserInfo user = project.getCreateUser();
        JSONObject json = new JSONObject();
        JSONObject container = new JSONObject();
        container.put("projectId", project.getProjectId());
        container.put("projectName", project.getProjectName());
        container.put("msgType", "系统通知");
        json.put("container", container);
        if (CollectionUtil.isNotEmpty(project.getProjectCooperatorList())) {
            json.put("msg", user.getUserName() + "创建了项目[" + project.getProjectName() + "],并设置你为配合人");
            GoEasyMessageBusiness.publish(
                    project.getProjectCooperatorList().stream().map(ProjectCooperatorInfo::getUser).distinct().collect(Collectors.toList())
                            .stream().map(UserInfo::getUserId).distinct().collect(Collectors.toList())
                    , json.toJSONString());
        }
        if (CollectionUtil.isNotEmpty(project.getProjectUserList())) {
            json.put("msg", user.getUserName() + "创建了项目[" + project.getProjectName() + "],并设置你为配合人");
            GoEasyMessageBusiness.publish(
                    project.getProjectUserList().stream().map(ProjectUserInfo::getUser).distinct().collect(Collectors.toList())
                            .stream().map(UserInfo::getUserId).distinct().collect(Collectors.toList())
                    , json.toJSONString());
        }
        json.put("msg", user.getUserName() + "创建了项目[" + project.getProjectName() + "],并设置你为业务员");
        GoEasyMessageBusiness.publish(project.getSalesMan().getUserId(), json.toJSONString());
        json.put("msg", user.getUserName() + "创建了项目[" + project.getProjectName() + "],并设置你为设计师");
        GoEasyMessageBusiness.publish(project.getDesigner().getUserId(), json.toJSONString());
        json.put("msg", user.getUserName() + "创建了项目[" + project.getProjectName() + "],并设置你为工程负责人");
        GoEasyMessageBusiness.publish(project.getCharger().getUserId(), json.toJSONString());
    }

    public static void insertP(ProjectInfo projectInfo){
        UserInfo user = projectInfo.getCreateUser();
        JSONObject JSONObject = new JSONObject();



    }

    public static void updatePublish(ProjectInfo project) {
        UserInfo user = project.getModifyUser();
        List<String> userIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(project.getProjectCooperatorList()))
            userIds.addAll(project.getProjectCooperatorList().stream().map(ProjectCooperatorInfo::getUser).distinct().collect(Collectors.toList())
                    .stream().map(UserInfo::getUserId).distinct().collect(Collectors.toList()));


        if (CollectionUtil.isNotEmpty(project.getProjectUserList()))
            userIds.addAll(project.getProjectUserList().stream().map(ProjectUserInfo::getUser).distinct().collect(Collectors.toList())
                    .stream().map(UserInfo::getUserId).distinct().collect(Collectors.toList()));

        userIds.add(project.getSalesMan().getUserId());
        userIds.add(project.getDesigner().getUserId());
        userIds.add(project.getCharger().getUserId());
        userIds.add(project.getCreateUser().getUserId());
        userIds = userIds.stream().distinct().collect(Collectors.toList());
        userIds.remove(null);
        userIds.remove(user.getUserId());

        JSONObject json = new JSONObject();
        JSONObject container = new JSONObject();
        container.put("projectId", project.getProjectId());
        container.put("projectName", project.getProjectName());
        container.put("msgType", "系统通知");
        json.put("container", container);
        json.put("msg", user.getUserName() + "修改了项目[" + project.getProjectName() + "]");
        GoEasyMessageBusiness.publish(userIds, json.toJSONString());
    }

    public static void applyCooperation(String accountId, String projectId, String cooperativeAccountId) {
        val sql = "insert into cooperation(accountId,projectId,cooperativeAccountId) " +
                  "values (:accountId,:projectId,:cooperativeAccountId)";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("projectId", projectId)
                    .addParameter("cooperativeAccountId", cooperativeAccountId)
                    .executeUpdate();
        }
    }

    public static void auditCooperation(boolean isOK, String id) {
        val sql = "update  cooperation " +
                  "set isOK=:isOK " +
                  "where id=:id";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("isOK", isOK)
                    .addParameter("id", id)
                    .executeUpdate();
        }
    }

    public static int countCooperation(Map<String, Object> params) {
        val sql = " SELECT count(1)  FROM cooperation C" +
                  " LEFT JOIN tbProject P ON P.projectId = C.projectId " +
                  " LEFT JOIN tbAccount A ON A.accountId = C.accountId " +
                  " WHERE C.isOK= :isOK" +
                  "   AND ( A.accountName= :companyName OR :companyName is null ) " +
                  "   AND ( P.projectName= :projectName OR :projectName is null )";


        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("isOK", params.get("isOK"))
                    .addParameter("companyName", params.get("companyName"))
                    .addParameter("projectName", params.get("projectName"))
                    .executeScalar(int.class);
        }
    }

    public static List<Cooperation> cooperations(Map<String, Object> params) {
        val sql = " SELECT A.accountName companyName,P.projectName,C.projectId,C.id ,S.type stageType FROM cooperation C" +
                  " LEFT JOIN tbProject P ON P.projectId = C.projectId " +
                  " LEFT JOIN tbAccount A ON A.accountId = C.accountId " +
                  " LEFT JOIN projectStage S ON S.id = P.stageId " +
                  " WHERE C.isOK= :isOK" +
                  "   AND ( C.CooperativeAccountId = :accountId ) " +
                  "   AND ( A.accountName= :companyName OR :companyName is null ) " +
                  "   AND ( P.projectName= :projectName OR :projectName is null )" +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";


        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("isOK", params.get("isOK"))
                    .addParameter("accountId", params.get("accountId"))
                    .addParameter("companyName", params.get("companyName"))
                    .addParameter("projectName", params.get("projectName"))
                    .addParameter("PAGEOFFSET", params.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", params.get("PAGESIZE"))
                    .executeAndFetch(Cooperation.class);
        }
    }


}