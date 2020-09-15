package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.*;
import com.forcpacebj.api.utils.CollectionUtil;
import com.forcpacebj.api.utils.DateUtil;
import com.forcpacebj.api.utils.IdGenerator;
import lombok.val;
import lombok.var;

import java.util.*;

/**
 * 日志业务逻辑
 */
public class RecordBusiness {

    public static List<RecordInfo> find(Map conditions) {
//        var showActivity = conditions.get("showActivity") != null && (Boolean) conditions.get("showActivity") ? "" : "AND R.RecordType <> 'activity'";
        var showActivity = conditions.get("showActivity") != null && (Boolean) conditions.get("showActivity") ?
                "  ( (R.UpdateTime Between :updateStartDate and :updateEndDate ) Or (:updateStartDate is null and :updateEndDate is null))" :
                " R.RecordType <> 'activity'" + " And ( (R.UpdateTime Between :updateStartDate and :updateEndDate ) Or (:updateStartDate is null and :updateEndDate is null))";

        val sql = " SELECT R.RecordId ,R.RecordContent ,R.RecordState ,R.RecordDate ,R.NextDateTime ,R.NextNature ,R.Cost ,R.TimeCost ," +
                  " R.UserId 'user.userId' ,R.UserName 'user.userName' ,R.UpdateTime ,R.RecordType ,A.MsgCount " +
                  " FROM tbRecord R" +
                  " Left Join ( select AccountId ,RecordId, Count(1) MsgCount " +
                  "               from tbRecordMsg  " +
                  "           GROUP BY AccountId,RecordId )A On R.AccountId=A.AccountId And R.RecordId=A.RecordId" +
//                " WHERE R.AccountId= :accountId " + showActivity +
                  " WHERE " + showActivity +
                " AND (R.AccountId= :accountId OR :accountId  IS NULL )"+ //该企业下的
                " AND (R.DepartmentId=:departmentId OR :departmentId IS NULL)" +
//                  " And ( (R.UpdateTime Between :updateStartDate and :updateEndDate ) Or (:updateStartDate is null and :updateEndDate is null)) " +
                  " And ( ( R.RecordDate Between :recordStartDate and :recordEndDate ) Or (:recordStartDate is null and :recordEndDate is null)) " +
                  " And ( R.RecordContent LIKE CONCAT('%',:record,'%') OR :record is null) " +
                  " And ( R.UserId= :userId OR :userId is null) " +
                  " And ( R.UserId= :createUserId OR :createUserId is null) " + //界面按创建人查询
                  " And ( R.RecordState= :recordState Or :recordState is Null)" +
                  " And ( ( R.NextDateTime Between :nextStartDate and :nextEndDate ) Or (:nextStartDate is null and :nextEndDate is null)) " +
                  " And ( R.NextNature= :nextNature Or :nextNature is Null)" +
                  " And ( R.RecordId in (SELECT Distinct R.RecordId from tbProjectRecord R ,tbProject P where R.AccountId=P.AccountId And R.ProjectId= P.ProjectId And (P.ProjectName like CONCAT('%',:projectName,'%') OR P.ProjectId = :projectId) ) Or (:projectName is null AND :projectId IS NULL)) " +
                  " And ( R.RecordId in (SELECT Distinct R.RecordId from tbPeopleRecord R ,tbPeople P where R.AccountId=P.AccountId And R.PeopleId=P.PeopleId AND (P.PeopleName like CONCAT('%',:peopleName,'%') OR P.PeopleId = :peopleId) ) Or (:peopleName is null AND :peopleId IS NULL) ) " +
                  " Order By " + conditions.get("OrderBy") +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {

            Object recordStartDate = null, recordEndDate = null,
                    updateStartDate = null, updateEndDate = null,
                    nextStartDate = null, nextEndDate = null;

            if (conditions.containsKey("recordDateRange")) {
                val dateRangeValue = (Map) conditions.get("recordDateRange");
                recordStartDate = dateRangeValue.get("startDate");
                recordEndDate = dateRangeValue.get("endDate");
            }
            if (conditions.containsKey("updateTimeRange")) {
                val dateRangeValue = (Map) conditions.get("updateTimeRange");
                updateStartDate = dateRangeValue.get("startDate");
                updateEndDate = dateRangeValue.get("endDate");
            }
            if (conditions.containsKey("nextDateRange")) {
                val dateRangeValue = (Map) conditions.get("nextDateRange");
                nextStartDate = dateRangeValue.get("startDate");
                nextEndDate = dateRangeValue.get("endDate");
            }
            val accountId = (String) conditions.get("accountId");

            val list = con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("recordState", conditions.get("recordState"))
                    .addParameter("recordStartDate", recordStartDate)
                    .addParameter("recordEndDate", recordEndDate)
                    .addParameter("updateStartDate", updateStartDate)
                    .addParameter("updateEndDate", updateEndDate)
                    .addParameter("nextStartDate", nextStartDate)
                    .addParameter("nextEndDate", nextEndDate)
                    .addParameter("record", conditions.get("record"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("createUserId", conditions.get("createUserId"))
                    .addParameter("nextNature", conditions.get("nextNature"))
                    .addParameter("projectName", conditions.get("projectName"))
                    .addParameter("projectId", conditions.get("projectId"))
                    .addParameter("peopleName", conditions.get("peopleName"))
                    .addParameter("peopleId", conditions.get("peopleId"))
                    .executeAndFetch(RecordInfo.class);

            if (CollectionUtil.isNotEmpty(list)) {
                for (val record : list) {

                    val recordId = record.getRecordId();
                    val hoursApart = DateUtil.hoursApart(record.getUpdateTime(), DateUtil.now());
                    record.setIsAllowUpdate(hoursApart <= 3 * 24); //只允许修改3天之内的日志
                    val recordPictureList = con.createQuery(" select PictureUrl,Name from tbRecordPicture where AccountId= :accountId And RecordId= :recordId  order by sortNumber")
                            .addParameter("accountId", accountId)
                            .addParameter("recordId", recordId)
                            .executeAndFetch(RecordPictureInfo.class);
                    if (CollectionUtil.isNotEmpty(recordPictureList)) {
                        record.setRecordPictureList(recordPictureList);
                    }
                    val peopleRecordList = con.createQuery(" select P.PeopleId 'people.peopleId' , P.PeopleName 'people.peopleName'" +
                                                           " from tbPeopleRecord R " +
                                                           " left join tbPeople P on R.AccountId=P.AccountId And R.PeopleId=P.PeopleId" +
                                                           " Where R.AccountId= :accountId And R.RecordId= :recordId order by R.SortNumber")
                            .addParameter("accountId", accountId)
                            .addParameter("recordId", recordId)
                            .executeAndFetch(PeopleRecordInfo.class);
                    if (CollectionUtil.isNotEmpty(peopleRecordList)) {
                        record.setPeopleRecordList(peopleRecordList);
                    }
                    val projectRecordList = con.createQuery(" select P.ProjectId 'project.projectId' ,P.ProjectName 'project.projectName',PS.StageName 'project.stage.stageName', PS.Type 'project.stage.type' " +
                                                            " from tbProjectRecord R " +
                                                            " left join tbProject P on R.AccountId=P.AccountId And R.ProjectId=P.ProjectId" +
                                                            " LEFT JOIN ProjectStage PS ON PS.Id = P.StageId AND PS.AccountId=P.AccountId " +
                                                            " Where R.AccountId= :accountId And R.RecordId= :recordId order by R.SortNumber")
                            .addParameter("accountId", accountId)
                            .addParameter("recordId", recordId)
                            .executeAndFetch(ProjectRecordInfo.class);
                    if (CollectionUtil.isNotEmpty(projectRecordList)) {
                        record.setProjectRecordList(projectRecordList);
                    }
                }
            }

            return list;
        }
    }

    public static List<RecordInfo> findCheckIsAdmin(Map conditions) {
        var showActivity = conditions.get("showActivity") != null && (Boolean) conditions.get("showActivity") ?
                "  ( (R.UpdateTime Between :updateStartDate and :updateEndDate ) Or (:updateStartDate is null and :updateEndDate is null))" :
                " R.RecordType <> 'activity'" + " And ( (R.UpdateTime Between :updateStartDate and :updateEndDate ) Or (:updateStartDate is null and :updateEndDate is null))";

        var isAdmin = (boolean)conditions.get("isAdmin") ? " And (:userId = :userId) " : "And ( R.UserId= :userId OR :userId is null)";

        val sql = " SELECT R.RecordId ,R.RecordContent ,R.RecordState ,R.RecordDate ,R.NextDateTime ,R.NextNature ,R.Cost ,R.TimeCost ," +
                " R.UserId 'user.userId' ,R.UserName 'user.userName' ,R.UpdateTime ,R.RecordType ,A.MsgCount " +
                " FROM tbRecord R" +
                " Left Join ( select AccountId ,RecordId, Count(1) MsgCount " +
                "               from tbRecordMsg  " +
                "           GROUP BY AccountId,RecordId )A On R.AccountId=A.AccountId And R.RecordId=A.RecordId" +
//                " WHERE R.AccountId= :accountId " + showActivity +
                " WHERE " + showActivity +
                " AND (R.AccountId= :accountId OR :accountId  IS NULL )"+ //该企业下的
                " AND (R.DepartmentId=:departmentId OR :departmentId IS NULL)" +
//                  " And ( (R.UpdateTime Between :updateStartDate and :updateEndDate ) Or (:updateStartDate is null and :updateEndDate is null)) " +
                " And ( ( R.RecordDate Between :recordStartDate and :recordEndDate ) Or (:recordStartDate is null and :recordEndDate is null)) " +
                " And ( R.RecordContent LIKE CONCAT('%',:record,'%') OR :record is null) " +
                //如果是管理员则显示所有的，否则显示个人的
                isAdmin +
                " And ( R.UserId= :createUserId OR :createUserId is null) " + //界面按创建人查询
                " And ( R.RecordState= :recordState Or :recordState is Null)" +
                " And ( ( R.NextDateTime Between :nextStartDate and :nextEndDate ) Or (:nextStartDate is null and :nextEndDate is null)) " +
                " And ( R.NextNature= :nextNature Or :nextNature is Null)" +
                " And ( R.RecordId in (SELECT Distinct R.RecordId from tbProjectRecord R ,tbProject P where R.AccountId=P.AccountId And R.ProjectId= P.ProjectId And (P.ProjectName like CONCAT('%',:projectName,'%') OR P.ProjectId = :projectId) ) Or (:projectName is null AND :projectId IS NULL)) " +
                " And ( R.RecordId in (SELECT Distinct R.RecordId from tbPeopleRecord R ,tbPeople P where R.AccountId=P.AccountId And R.PeopleId=P.PeopleId AND (P.PeopleName like CONCAT('%',:peopleName,'%') OR P.PeopleId = :peopleId) ) Or (:peopleName is null AND :peopleId IS NULL) ) " +
                " Order By " + conditions.get("OrderBy") +
                " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {

            Object recordStartDate = null, recordEndDate = null,
                    updateStartDate = null, updateEndDate = null,
                    nextStartDate = null, nextEndDate = null;

            if (conditions.containsKey("recordDateRange")) {
                val dateRangeValue = (Map) conditions.get("recordDateRange");
                recordStartDate = dateRangeValue.get("startDate");
                recordEndDate = dateRangeValue.get("endDate");
            }
            if (conditions.containsKey("updateTimeRange")) {
                val dateRangeValue = (Map) conditions.get("updateTimeRange");
                updateStartDate = dateRangeValue.get("startDate");
                updateEndDate = dateRangeValue.get("endDate");
            }
            if (conditions.containsKey("nextDateRange")) {
                val dateRangeValue = (Map) conditions.get("nextDateRange");
                nextStartDate = dateRangeValue.get("startDate");
                nextEndDate = dateRangeValue.get("endDate");
            }
            val accountId = (String) conditions.get("accountId");

            val list = con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("recordState", conditions.get("recordState"))
                    .addParameter("recordStartDate", recordStartDate)
                    .addParameter("recordEndDate", recordEndDate)
                    .addParameter("updateStartDate", updateStartDate)
                    .addParameter("updateEndDate", updateEndDate)
                    .addParameter("nextStartDate", nextStartDate)
                    .addParameter("nextEndDate", nextEndDate)
                    .addParameter("record", conditions.get("record"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("createUserId", conditions.get("createUserId"))
                    .addParameter("nextNature", conditions.get("nextNature"))
                    .addParameter("projectName", conditions.get("projectName"))
                    .addParameter("projectId", conditions.get("projectId"))
                    .addParameter("peopleName", conditions.get("peopleName"))
                    .addParameter("peopleId", conditions.get("peopleId"))
                    .executeAndFetch(RecordInfo.class);

            if (CollectionUtil.isNotEmpty(list)) {
                for (val record : list) {

                    val recordId = record.getRecordId();
                    val hoursApart = DateUtil.hoursApart(record.getUpdateTime(), DateUtil.now());
                    record.setIsAllowUpdate(hoursApart <= 3 * 24); //只允许修改3天之内的日志
                    val recordPictureList = con.createQuery(" select PictureUrl,Name from tbRecordPicture where AccountId= :accountId And RecordId= :recordId  order by sortNumber")
                            .addParameter("accountId", accountId)
                            .addParameter("recordId", recordId)
                            .executeAndFetch(RecordPictureInfo.class);
                    if (CollectionUtil.isNotEmpty(recordPictureList)) {
                        record.setRecordPictureList(recordPictureList);
                    }
                    val peopleRecordList = con.createQuery(" select P.PeopleId 'people.peopleId' , P.PeopleName 'people.peopleName'" +
                            " from tbPeopleRecord R " +
                            " left join tbPeople P on R.AccountId=P.AccountId And R.PeopleId=P.PeopleId" +
                            " Where R.AccountId= :accountId And R.RecordId= :recordId order by R.SortNumber")
                            .addParameter("accountId", accountId)
                            .addParameter("recordId", recordId)
                            .executeAndFetch(PeopleRecordInfo.class);
                    if (CollectionUtil.isNotEmpty(peopleRecordList)) {
                        record.setPeopleRecordList(peopleRecordList);
                    }
                    val projectRecordList = con.createQuery(" select P.ProjectId 'project.projectId' ,P.ProjectName 'project.projectName',PS.StageName 'project.stage.stageName', PS.Type 'project.stage.type' " +
                            " from tbProjectRecord R " +
                            " left join tbProject P on R.AccountId=P.AccountId And R.ProjectId=P.ProjectId" +
                            " LEFT JOIN ProjectStage PS ON PS.Id = P.StageId AND PS.AccountId=P.AccountId " +
                            " Where R.AccountId= :accountId And R.RecordId= :recordId order by R.SortNumber")
                            .addParameter("accountId", accountId)
                            .addParameter("recordId", recordId)
                            .executeAndFetch(ProjectRecordInfo.class);
                    if (CollectionUtil.isNotEmpty(projectRecordList)) {
                        record.setProjectRecordList(projectRecordList);
                    }
                }
            }

            return list;
        }
    }

    public static int count(Map conditions) {
        var showActivity = conditions.get("showActivity") != null && (Boolean) conditions.get("showActivity") ? "" : "AND RecordType <> 'activity'";

        val sql = " SELECT count(1) xcount FROM tbRecord " +
                  " WHERE AccountId= :accountId " + showActivity +
                  " AND (DepartmentId=:departmentId OR :departmentId IS NULL)" +
                  " And ( (UpdateTime Between :updateStartDate and :updateEndDate ) Or (:updateStartDate is null and :updateEndDate is null)) " +
                  " And ( ( RecordDate Between :recordStartDate and :recordEndDate ) Or (:recordStartDate is null and :recordEndDate is null)) " +
                  " And ( RecordContent LIKE CONCAT('%',:record,'%') OR :record is null) " +
                  " And ( RecordState= :recordState Or :recordState is Null)" +
                  " And ( UserId= :userId OR :userId is null) " +
                  " And ( UserId= :createUserId OR :createUserId is null) " +
                  " And ( ( NextDateTime Between :nextStartDate and :nextEndDate ) Or (:nextStartDate is null and :nextEndDate is null)) " +
                  " And ( NextNature= :nextNature Or :nextNature is Null)" +
                  " And ( RecordId in (SELECT Distinct R.RecordId from tbProjectRecord R ,tbProject P where R.AccountId=P.AccountId And R.ProjectId= P.ProjectId And (P.ProjectName like CONCAT('%',:projectName,'%') OR P.ProjectId = :projectId) ) Or (:projectName is null AND :projectId IS NULL)) " +
                  " And ( RecordId in (SELECT Distinct R.RecordId from tbPeopleRecord R ,tbPeople P where R.AccountId=P.AccountId And R.PeopleId=P.PeopleId AND (P.PeopleName like CONCAT('%',:peopleName,'%') OR P.PeopleId = :peopleId) ) Or (:peopleName is null AND :peopleId IS NULL) ) ";

        try (val con = db.sql2o.open()) {

            Object recordStartDate = null, recordEndDate = null,
                    updateStartDate = null, updateEndDate = null,
                    nextStartDate = null, nextEndDate = null;

            if (conditions.containsKey("recordDateRange")) {
                val dateRangeValue = (Map) conditions.get("recordDateRange");
                recordStartDate = dateRangeValue.get("startDate");
                recordEndDate = dateRangeValue.get("endDate");
            }
            if (conditions.containsKey("updateTimeRange")) {
                val dateRangeValue = (Map) conditions.get("updateTimeRange");
                updateStartDate = dateRangeValue.get("startDate");
                updateEndDate = dateRangeValue.get("endDate");
            }
            if (conditions.containsKey("nextDateRange")) {
                val dateRangeValue = (Map) conditions.get("nextDateRange");
                nextStartDate = dateRangeValue.get("startDate");
                nextEndDate = dateRangeValue.get("endDate");
            }
            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("recordState", conditions.get("recordState"))
                    .addParameter("recordStartDate", recordStartDate)
                    .addParameter("recordEndDate", recordEndDate)
                    .addParameter("updateStartDate", updateStartDate)
                    .addParameter("updateEndDate", updateEndDate)
                    .addParameter("nextStartDate", nextStartDate)
                    .addParameter("nextEndDate", nextEndDate)
                    .addParameter("record", conditions.get("record"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("createUserId", conditions.get("createUserId"))
                    .addParameter("nextNature", conditions.get("nextNature"))
                    .addParameter("projectName", conditions.get("projectName"))
                    .addParameter("projectId", conditions.get("projectId"))
                    .addParameter("peopleName", conditions.get("peopleName"))
                    .addParameter("peopleId", conditions.get("peopleId"))
                    .executeScalar(int.class);
        }
    }

    public static List<RecordInfo> list(Map conditions) {

        String sql = " SELECT RecordId ,RecordContent ,RecordState ,RecordDate ,NextDateTime ,NextNature ,Cost ,TimeCost , RecordType ," +
                     " UserId 'user.userId' ,UserName 'user.userName' ,UpdateTime" +
                     " FROM tbRecord " +
                     " WHERE AccountId= :accountId " +
                     "   AND (DepartmentId=:departmentId OR :departmentId IS NULL) " +
                     "   And (UserId= :userId  Or :userId is Null)" +
                     "   And ((RecordDate Between :recordStartDate and :recordEndDate) or (:recordStartDate is null and :recordEndDate is null))" +
                     "   And (DATE_FORMAT(RecordDate,'%Y-%m')= :month OR :month is null) " +
                     "   And ( RecordState= :recordState Or :recordState is Null)";


        if (conditions.containsKey("peopleStr") && conditions.containsKey("projectStr")) {
            sql += "   And (    RecordId in (SELECT RecordId from tbpeoplerecord where AccountId=:accountId And PeopleId in (:peopleStr))" +
                   "       or  RecordId in (SELECT R.RecordId from tbprojectrecord R ,tbproject P " +
                   "                           where R.AccountId=P.AccountId " +
                   "                             And R.ProjectId= P.ProjectId " +
                   "                             And P.AccountId= :accountId " +
                   "                             And P.ProjectId in (:projectStr) " +
                   "                       ) )";
        } else if (conditions.containsKey("peopleStr")) {
            sql += " And RecordId in (SELECT RecordId from tbpeoplerecord where AccountId= :accountId And PeopleId in (:peopleStr))";
        } else if (conditions.containsKey("projectStr")) {
            sql += " And RecordId in (SELECT R.RecordId from tbprojectrecord R ,tbproject P " +
                   "                           where R.AccountId=P.AccountId" +
                   "                             And R.ProjectId= P.ProjectId " +
                   "                             And P.AccountId= :accountId " +
                   "                             And P.ProjectId in (:projectStr) " +
                   "                       )";
        }
        sql += " Order By " + conditions.get("OrderBy");

        val accountId = (String) conditions.get("accountId");

        try (val con = db.sql2o.open()) {

            val conQuery = con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("recordStartDate", conditions.get("recordStartDate"))
                    .addParameter("recordEndDate", conditions.get("recordEndDate"))
                    .addParameter("month", conditions.get("month"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("recordState", conditions.get("recordState"));

            if (conditions.containsKey("peopleStr") && conditions.containsKey("projectStr")) {
                conQuery.addParameter("peopleStr", conditions.get("peopleStr"))
                        .addParameter("projectStr", conditions.get("projectStr"));
            } else if (conditions.containsKey("peopleStr")) {
                conQuery.addParameter("peopleStr", conditions.get("peopleStr"));
            } else if (conditions.containsKey("projectStr")) {
                conQuery.addParameter("projectStr", conditions.get("projectStr"));
            }

            val list = conQuery.executeAndFetch(RecordInfo.class);

            if (CollectionUtil.isNotEmpty(list)) {
                for (val record : list) {

                    val recordId = record.getRecordId();
                    val hoursApart = DateUtil.hoursApart(record.getUpdateTime(), DateUtil.now());
                    record.setIsAllowUpdate(hoursApart <= 3 * 24); //只允许修改3天之内的日志
                    val recordPictureList = con.createQuery(" select PictureUrl,Name from tbRecordPicture where AccountId= :accountId And RecordId= :recordId  order by sortNumber")
                            .addParameter("accountId", accountId)
                            .addParameter("recordId", recordId)
                            .executeAndFetch(RecordPictureInfo.class);
                    if (CollectionUtil.isNotEmpty(recordPictureList)) {
                        record.setRecordPictureList(recordPictureList);
                    }
                    val peopleRecordList = con.createQuery(" select P.PeopleId 'people.peopleId' , P.PeopleName 'people.peopleName'" +
                                                           " from tbPeopleRecord R " +
                                                           " left join tbPeople P on R.AccountId=P.AccountId And R.PeopleId=P.PeopleId" +
                                                           " Where R.AccountId= :accountId And R.RecordId= :recordId order by R.SortNumber")
                            .addParameter("accountId", accountId)
                            .addParameter("recordId", recordId)
                            .executeAndFetch(PeopleRecordInfo.class);
                    if (CollectionUtil.isNotEmpty(peopleRecordList)) {
                        record.setPeopleRecordList(peopleRecordList);
                    }
                    val projectRecordList = con.createQuery(" select P.ProjectId 'project.projectId' ,P.ProjectName 'project.projectName' " +
                                                            " from tbProjectRecord R " +
                                                            " left join tbProject P on R.AccountId=P.AccountId And R.ProjectId=P.ProjectId" +
                                                            " Where R.AccountId= :accountId And R.RecordId= :recordId order by R.SortNumber")
                            .addParameter("accountId", accountId)
                            .addParameter("recordId", recordId)
                            .executeAndFetch(ProjectRecordInfo.class);
                    if (CollectionUtil.isNotEmpty(projectRecordList)) {
                        record.setProjectRecordList(projectRecordList);
                    }
                }
            }

            return list;
        }
    }

    public static RecordInfo load(String accountId, String id) {

        val sql = " select RecordId ,RecordContent ,RecordState ,RecordDate ,NextDateTime ,NextNature ,Cost ,TimeCost ,RecordType ," +
                  "   UserId 'user.userId' ,UserName 'user.userName' ,UpdateTime " +
                  "   from tbRecord Where AccountId= :accountId And RecordId= :id ";
        try (val con = db.sql2o.open()) {
            val record = con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeAndFetchFirst(RecordInfo.class);

            if (record != null) {
                val recordPictureList = con.createQuery(" select PictureUrl,Name from tbRecordPicture where AccountId= :accountId And RecordId= :id  order by sortNumber")
                        .addParameter("accountId", accountId)
                        .addParameter("id", id)
                        .executeAndFetch(RecordPictureInfo.class);
                if (CollectionUtil.isNotEmpty(recordPictureList)) {
                    record.setRecordPictureList(recordPictureList);
                }
                val peopleRecordList = con.createQuery(" select PeopleId 'people.peopleId' ,RelationWithCharger 'people.relationWithCharger' ,Cost 'people.cost'" +
                                                       " from tbPeopleRecord " +
                                                       " Where AccountId= :accountId And RecordId= :id order by SortNumber")
                        .addParameter("accountId", accountId)
                        .addParameter("id", id)
                        .executeAndFetch(PeopleRecordInfo.class);
                if (CollectionUtil.isNotEmpty(peopleRecordList)) {
                    for (val peopleRecord : peopleRecordList) {

                        val loadPeople = PeopleBusiness.loadPeopleBaseInfo(accountId, peopleRecord.getPeople().getPeopleId());
                        if (loadPeople != null) {
                            loadPeople.setCost(peopleRecord.getPeople().getCost());
                            loadPeople.setRelationWithCharger(peopleRecord.getPeople().getRelationWithCharger());
                            peopleRecord.setPeople(loadPeople);
                        }
                    }

                    record.setPeopleRecordList(peopleRecordList);
                }
                val projectRecordList = con.createQuery(" select ProjectId 'project.projectId' ,ProjectStage 'project.stage.stageName', StageType 'project.stage.typeName', " +
                                                        " EstimateContractMoney 'project.estimateContractMoney' ,DealRate 'project.dealRate' " +
                                                        " from tbProjectRecord " +
                                                        " Where AccountId= :accountId And RecordId= :id order by SortNumber")
                        .addParameter("accountId", accountId)
                        .addParameter("id", id)
                        .executeAndFetch(ProjectRecordInfo.class);
                if (CollectionUtil.isNotEmpty(projectRecordList)) {

                    for (val projectRecord : projectRecordList) {

                        val loadProject = ProjectBusiness.load(accountId, projectRecord.getProject().getProjectId());
                        if (loadProject != null) {
                            loadProject.setStage(projectRecord.getProject().getStage());
                            loadProject.setEstimateContractMoney(projectRecord.getProject().getEstimateContractMoney());
                            loadProject.setDealRate(projectRecord.getProject().getDealRate());

                            projectRecord.setProject(loadProject);
                        }
                    }

                    record.setProjectRecordList(projectRecordList);
                }
                record.setRecordMsgList(RecordMsgBusiness.list(accountId, id));
            }
            return record;
        }
    }

    public static List<String> insert(List<RecordInfo> recordList, UserInfo user) {

        if (CollectionUtil.isNotEmpty(recordList)) {

            val newRecordIdArray = new ArrayList<String>();
            val accountId = user.getAccountId();

            try (val con = db.sql2o.beginTransaction()) {
                for (RecordInfo record : recordList) {

                    val newRecordId = IdGenerator.NewId();

                    record.setUpdateTime(DateUtil.now());

                    val sql = " Insert Into tbRecord(DepartmentId, AccountId ,RecordId ,RecordContent ,RecordState ,RecordDate ,NextDateTime ,NextNature ,Cost ,TimeCost ,PreRecordId ,UserId ,UserName ,UpdateTime,ToEngineer ,RecordType)" +
                              " Values(:departmentId, :accountId ,:recordId ,:recordContent ,:recordState ,:recordDate ,:nextDateTime ,:nextNature ,:cost ,:timeCost ,:preRecordId ,:userId ,:userName ,:updateTime,:toEngineer ,:recordType)";

                    con.createQuery(sql)
                            .bind(record)
                            .addParameter("accountId", accountId)
                            .addParameter("departmentId", user.getDepartment().getId())
                            .addParameter("recordId", newRecordId)
                            .addParameter("userId", user.getUserId())
                            .addParameter("userName", user.getUserName())
                            .executeUpdate();
                    if (CollectionUtil.isNotEmpty(record.getRecordPictureList())) {
                        val pictureSql = " Insert into tbRecordPicture(AccountId ,RecordId,SortNumber,PictureUrl,Name)" +
                                         " Values(:accountId ,:recordId ,:sortNumber ,:pictureUrl, :name)";

                        val query = con.createQuery(pictureSql);

                        for (int i = 0; i < record.getRecordPictureList().size(); i++) {

                            val picture = record.getRecordPictureList().get(i);
                            query.addParameter("accountId", accountId);
                            query.addParameter("recordId", newRecordId);
                            query.addParameter("sortNumber", i);
                            query.addParameter("pictureUrl", picture.getPictureUrl());
                            query.addParameter("name", picture.getName());
                            query.addToBatch();
                        }

                        query.executeBatch();
                    }
                    if (CollectionUtil.isNotEmpty(record.getPeopleRecordList())) {

                        val peopleRecordSql = " Insert into tbPeopleRecord(AccountId ,RecordId,PeopleId,SortNumber,Cost,RelationWithCharger)" +
                                              " Values(:accountId ,:recordId ,:peopleId ,:sortNumber ,:cost ,:relationWithCharger )";

                        val query = con.createQuery(peopleRecordSql);

                        for (int i = 0; i < record.getPeopleRecordList().size(); i++) {

                            val peopleRecord = record.getPeopleRecordList().get(i);
                            val cost = peopleRecord.getPeople().getCost() + record.getCost();

                            query.addParameter("accountId", accountId);
                            query.addParameter("recordId", newRecordId);
                            query.addParameter("peopleId", peopleRecord.getPeople().getPeopleId());
                            query.addParameter("sortNumber", i);
                            query.addParameter("cost", cost);
                            query.addParameter("relationWithCharger", peopleRecord.getPeople().getRelationWithCharger());
                            query.addToBatch();
                            peopleRecord.getPeople().setUser(user);
                            peopleRecord.getPeople().setCost(cost);
                            peopleRecord.getPeople().setUpdateTime(record.getUpdateTime());
                            PeopleBusiness.update(accountId, peopleRecord.getPeople());
                        }

                        query.executeBatch();
                    }

                    //保存ProjectRecord
                    if (CollectionUtil.isNotEmpty(record.getProjectRecordList())) {

                        val projectRecordSql = " Insert Into tbProjectRecord(AccountId ,RecordId,ProjectId,SortNumber," +
                                               " ProjectStage,StageType,EstimateContractMoney,DealRate,Cost)" +
                                               " Values(:accountId ,:recordId ,:projectId ,:sortNumber," +
                                               " :projectStage ,:stageType ,:estimateContractMoney ,:dealRate ,:cost )";

                        val projectRecordQuery = con.createQuery(projectRecordSql);

                        for (int i = 0; i < record.getProjectRecordList().size(); i++) {
                            val projectRecord = record.getProjectRecordList().get(i);
                            double cost = record.getCost() + projectRecord.getProject().getOtherCost();

                            projectRecordQuery.addParameter("accountId", accountId);
                            projectRecordQuery.addParameter("recordId", newRecordId);
                            projectRecordQuery.addParameter("projectId", projectRecord.getProject().getProjectId());
                            projectRecordQuery.addParameter("sortNumber", i);
                            projectRecordQuery.addParameter("projectStage", projectRecord.getProject().getStage().getStageName());
                            val stageType = projectRecord.getProject().getStage().getType();
                            projectRecordQuery.addParameter("stageType", stageType == null ? "无阶段" : (stageType == 0 ? "销售机会" : "项目管理"));
                            projectRecordQuery.addParameter("estimateContractMoney", projectRecord.getProject().getEstimateContractMoney());
                            projectRecordQuery.addParameter("dealRate", projectRecord.getProject().getDealRate());
                            projectRecordQuery.addParameter("cost", cost);
                            projectRecordQuery.addToBatch();
                            projectRecord.getProject().setModifyUser(user);
                            projectRecord.getProject().setOtherCost(cost);
                            projectRecord.getProject().setModifyDateTime(record.getUpdateTime());
                            ProjectBusiness.update(accountId, projectRecord.getProject());

                        }

                        projectRecordQuery.executeBatch();
                        if ("工程".equals(record.getNextNature()) && record.getToEngineer()) {
                            val projectUserSql = " INSERT INTO tbProjectUser(AccountId ,ProjectId , SortNumber, UserId ,UserName) " +
                                                 " select :accountId AccountId ,:projectId ProjectId, 99 SortNumber ,UserId ,UserName from tbuser " +
                                                 " where AccountId=:accountId and UserRole ='Engineer' " +
                                                 "   and UserId not in (select UserId from tbprojectuser where AccountId=:accountId and ProjectId=:projectId);";

                            val projectUserQuery = con.createQuery(projectUserSql);

                            for (int i = 0; i < record.getProjectRecordList().size(); i++) {

                                val projectRecord = record.getProjectRecordList().get(i);
                                projectUserQuery.addParameter("accountId", accountId);
                                projectUserQuery.addParameter("projectId", projectRecord.getProject().getProjectId());
                                projectUserQuery.addToBatch();
                            }

                            projectUserQuery.executeBatch();
                        }
                    }

                    newRecordIdArray.add(newRecordId);
                }
                val preRecordId = recordList.get(0).getPreRecordId();
                con.createQuery(" update tbRecord set RecordState='已完成' where AccountId= :accountId and RecordId=:preRecordId")
                        .addParameter("accountId", accountId)
                        .addParameter("preRecordId", preRecordId)
                        .executeUpdate();

                con.commit();
            }

            return newRecordIdArray;
        }
        return null;
    }

    public static List<RecordPictureInfo> loadRecordPicture(String accountId, String recordId) {

        try (val con = db.sql2o.open()) {
            return con.createQuery(" select PictureUrl,Name from tbrecordpicture where AccountId= :accountId And recordId= :id ")
                    .addParameter("accountId", accountId)
                    .addParameter("id", recordId)
                    .executeAndFetch(RecordPictureInfo.class);
        }
    }

    public static void update(String accountId, RecordInfo record) {
        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(" update tbRecord set " +
                            "   RecordDate=:recordDate , " +
                            "   RecordContent= :recordContent , " +
                            "   NextNature= :nextNature " +
                            "   where AccountId= :accountId And RecordId= :recordId")
                    .bind(record)
                    .addParameter("accountId", accountId)
                    .executeUpdate();
            con.createQuery(" DELETE FROM tbRecordPicture WHERE AccountId= :accountId And RecordId = :recordId ")
                    .bind(record)
                    .addParameter("accountId", accountId)
                    .executeUpdate();
            if (CollectionUtil.isNotEmpty(record.getRecordPictureList())) {
                val pictureSql = " Insert into tbRecordPicture(AccountId ,RecordId,SortNumber,PictureUrl,Name)" +
                                 " Values(:accountId ,:recordId ,:sortNumber ,:pictureUrl, :name)";

                val query = con.createQuery(pictureSql);

                for (int i = 0; i < record.getRecordPictureList().size(); i++) {

                    val picture = record.getRecordPictureList().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("recordId", record.getRecordId());
                    query.addParameter("sortNumber", i);
                    query.addParameter("pictureUrl", picture.getPictureUrl());
                    query.addParameter("name", picture.getName());
                    query.addToBatch();
                }

                query.executeBatch();
            }

            con.commit();
        }
    }

    public static void delete(String accountId, String id) {

        try (val con = db.sql2o.beginTransaction()) {

            val peopleSql = "  Update tbpeople P,( " +
                            " Select P.AccountId ,P.PeopleId,R.Cost From tbpeoplerecord P,tbrecord R " +
                            " where R.AccountId=P.AccountId" +
                            "   And R.RecordId=P.RecordId " +
                            "   And R.AccountId= :accountId " +
                            "   And R.RecordId = :id )R " +
                            " set P.Cost=IFNULL(P.Cost,0)- IFNULL(R.Cost,0) " +
                            " where P.AccountId=R.AccountId " +
                            "   and P.PeopleId=R.PeopleId " +
                            "   and P.AccountId= :accountId ; ";
            con.createQuery(peopleSql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeUpdate();


            val projectSql = "  Update tbproject P,( " +
                             " Select P.AccountId ,P.ProjectId,R.Cost From tbprojectrecord P,tbrecord R " +
                             " where R.AccountId=P.AccountId " +
                             "   And R.RecordId=P.RecordId " +
                             "   and R.AccountId= :accountId " +
                             "   And R.RecordId = :id )R " +
                             " set P.OtherCost=IFNULL(P.OtherCost,0)- IFNULL(R.Cost,0) " +
                             " where P.AccountId=R.AccountId " +
                             "   And P.ProjectId=R.ProjectId " +
                             "   And P.AccountId= :accountId ; ";
            con.createQuery(projectSql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeUpdate();

            Arrays.asList(
                    " DELETE FROM tbRecord WHERE AccountId= :accountId And RecordId = :id ; ",
                    " DELETE FROM tbRecordPicture WHERE AccountId= :accountId And RecordId = :id ; ",
                    " DELETE FROM tbPeopleRecord WHERE AccountId= :accountId And RecordId = :id ; ",
                    " DELETE FROM tbProjectRecord WHERE AccountId= :accountId And RecordId = :id ;",
                    " DELETE FROM tbRecordMsg WHERE AccountId= :accountId And RecordId = :id ;")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("accountId", accountId)
                                    .addParameter("id", id)
                                    .executeUpdate()
                    );

            con.commit();
        }
    }

    public static List<PeopleRecordInfo> findPeopleRecord(Map conditions) {

        val sql = "select R.RecordId , R.RecordContent ,R.RecordDate ,R.NextDateTime ,R.NextNature ,R.Cost ,R.TimeCost ,R.UserName 'user.userName' ,R.UpdateTime ," +
                  " P.Cost 'people.cost' ,P.RelationWithCharger 'people.relationWithCharger'" +
                  " from tbpeoplerecord P,tbrecord R" +
                  " where P.AccountId=R.AccountId " +
                  "   And P.RecordId=R.RecordId " +
                  "   And P.AccountId= :accountId" +
                  "   And P.PeopleId= :peopleId" +
                  "   AND (R.DepartmentId=:departmentId OR :departmentId IS NULL) " +
                  " order by R.UpdateTime Desc " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        val accountId = (String) conditions.get("accountId");
        try (val con = db.sql2o.open()) {
            val list = con.createQuery(sql)
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("accountId", accountId)
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("peopleId", conditions.get("peopleId"))
                    .executeAndFetch(PeopleRecordInfo.class);

            if (CollectionUtil.isNotEmpty(list)) {
                for (val peopleRecord : list) {

                    val recordId = peopleRecord.getRecordId();
                    peopleRecord.setRecordPictureList(RecordBusiness.loadRecordPicture(accountId, recordId));
                    peopleRecord.setRecordMsgList(RecordMsgBusiness.list(accountId, recordId));
                }
            }

            return list;
        }
    }

    public static List<ProjectRecordInfo> findProjectRecord(Map conditions) {

        val sql = " select R.RecordId ,R.RecordContent ,R.RecordDate ,R.NextDateTime ,R.NextNature ,R.Cost ,R.TimeCost ,R.UserName 'user.userName' ,R.RecordType ,R.UpdateTime ," +
                  " P.ProjectStage 'project.stage.stageName' , P.StageType 'project.stage.typeName' ,P.EstimateContractMoney 'project.estimateContractMoney' ,P.DealRate 'project.dealRate' " +
                  " from tbprojectrecord P,tbrecord R " +
                  " where P.AccountId=R.AccountId " +
                  "   And P.RecordId=R.RecordId " +
                  "   And P.AccountId= :accountId " +
                  "   And P.ProjectId = :projectId " +
                  "   AND (R.DepartmentId=:departmentId OR :departmentId IS NULL) " +
                  " order by R.UpdateTime Desc " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        val accountId = (String) conditions.get("accountId");
        try (val con = db.sql2o.open()) {
            val list = con.createQuery(sql)
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("accountId", accountId)
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("projectId", conditions.get("projectId"))
                    .executeAndFetch(ProjectRecordInfo.class);

            if (CollectionUtil.isNotEmpty(list)) {
                for (val projectRecord : list) {
                    val recordId = projectRecord.getRecordId();
                    projectRecord.setRecordPictureList(RecordBusiness.loadRecordPicture(accountId, recordId));
                    projectRecord.setRecordMsgList(RecordMsgBusiness.list(accountId, recordId));
                }
            }

            return list;
        }
    }

    public static void deletePeopleRecord(String accountId, String recordId, String peopleId) {

        try (val con = db.sql2o.beginTransaction()) {

            val peopleSql = "  Update tbpeople P,( " +
                            " Select P.AccountId ,P.PeopleId,R.Cost From tbpeoplerecord P,tbrecord R " +
                            " where R.AccountId=P.AccountId" +
                            "   And R.RecordId=P.RecordId " +
                            "   And R.AccountId= :accountId " +
                            "   And R.RecordId = :recordId " +
                            "   And P.PeopleId= :peopleId )R " +
                            " set P.Cost=IFNULL(P.Cost,0)- IFNULL(R.Cost,0) " +
                            " where P.AccountId=R.AccountId " +
                            "   And P.PeopleId=R.PeopleId " +
                            "   ANd P.AccountId= :accountId; ";
            con.createQuery(peopleSql)
                    .addParameter("accountId", accountId)
                    .addParameter("recordId", recordId)
                    .addParameter("peopleId", peopleId)
                    .executeUpdate();

            con.createQuery(" DELETE FROM tbPeopleRecord WHERE AccountId= :accountId And RecordId = :recordId And PeopleId= :peopleId ")
                    .addParameter("accountId", accountId)
                    .addParameter("recordId", recordId)
                    .addParameter("peopleId", peopleId)
                    .executeUpdate();

            con.commit();
        }
    }

    public static void deleteProjectRecord(String accountId, String recordId, String projectId) {

        try (val con = db.sql2o.beginTransaction()) {

            val projectSql = "  Update tbproject P,( " +
                             " Select P.AccountId ,P.ProjectId,R.Cost From tbprojectrecord P,tbrecord R " +
                             " where R.AccountId=P.AccountId " +
                             "   And R.RecordId=P.RecordId " +
                             "   And R.AccountId= :accountId" +
                             "   And R.RecordId = :recordId And P.ProjectId= :projectId )R " +
                             " set P.OtherCost=IFNULL(P.OtherCost,0)- IFNULL(R.Cost,0) " +
                             " where P.AccountId=R.AccountId " +
                             "   And P.ProjectId=R.ProjectId " +
                             "   And P.AccountId= :accountId ; ";
            con.createQuery(projectSql)
                    .addParameter("accountId", accountId)
                    .addParameter("recordId", recordId)
                    .addParameter("projectId", projectId)
                    .executeUpdate();

            con.createQuery("DELETE FROM tbProjectRecord WHERE AccountId= :accountId And RecordId = :recordId And projectId= :projectId ")
                    .addParameter("accountId", accountId)
                    .addParameter("recordId", recordId)
                    .addParameter("projectId", projectId)
                    .executeUpdate();


            con.commit();
        }
    }

    public static void updateRecordState(String accountId, RecordInfo record) {
        try (val con = db.sql2o.open()) {

            con.createQuery(" update tbRecord set RecordState=:recordState where AccountId= :accountId And  RecordId=:recordId")
                    .addParameter("accountId", accountId)
                    .addParameter("recordState", record.getRecordState())
                    .addParameter("recordId", record.getRecordId())
                    .executeUpdate();
        }
    }
}
