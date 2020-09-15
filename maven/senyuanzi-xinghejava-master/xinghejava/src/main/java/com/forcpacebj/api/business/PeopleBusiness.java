package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.PeopleInfo;
import com.forcpacebj.api.entity.PeopleRelatedInfo;
import com.forcpacebj.api.entity.ProjectRelatedInfo;
import com.forcpacebj.api.entity.UserInfo;
import com.forcpacebj.api.utils.CollectionUtil;
import lombok.val;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * "人"业务逻辑
 */
public class PeopleBusiness {

    public static List<PeopleInfo> find(Map conditions) {

        val sql = " SELECT PeopleId ,PeopleName ,PeopleSuffix ,Mobile ,WechatId ,Gender , AgeAlias ,Birthday ," +
                " ChargerId 'charger.userId' ,ChargerName 'charger.userName' ,MaritalStatus ,ChildrenStatus ," +
                " Unit ,Cost ,PeopleRole ,RelationWithCharger ,Remark ,UserId 'user.userId' ,UserName 'user.userName', Address,FirstContact,UpdateTime" +
                " FROM tbPeople " +
                " WHERE AccountId= :accountId AND State = :state " +
                "      AND (DepartmentId=:departmentId OR :departmentId IS NULL)" +
                "      AND (PeopleName LIKE CONCAT('%',:peopleName,'%') OR :peopleName is null) " +
                "      AND (IFNULL(PeopleRole,'') NOT LIKE CONCAT('%','业主','%') OR IFNULL(:filterOwner,false) = false) " +
                "      AND (Mobile LIKE CONCAT('%',:mobile,'%') OR :mobile is null)" +
                "      AND (PeopleRole LIKE CONCAT('%',:peopleRole,'%') OR :peopleRole is null)" +
                "      AND (ChargerId = :userId OR CreateUserId = :userId OR HandoverUserId=:userId OR :userId is null ) " +
                "      AND (ChargerId = :chargerId OR :chargerId is null ) " +
                "      AND (CreateUserId = :createUserId OR :createUserId is null)" +
                "      AND ( ( CreateDateTime Between :createStartDate and :createEndDate ) Or (:createStartDate is null and :createEndDate is null)) " +
                " Order By UpdateTime Desc" +
                " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {

            Object createStartDate = null, createEndDate = null;

            if (conditions.containsKey("createDateTime")) {
                val dateRangeValue = (Map) conditions.get("createDateTime");
                createStartDate = dateRangeValue.get("startDate");
                createEndDate = dateRangeValue.get("endDate");
            }

            val list = con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("departmentId", conditions.get("departmentId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("peopleName", conditions.get("peopleName"))
                    .addParameter("filterOwner", conditions.get("filterOwner"))
                    .addParameter("mobile", conditions.get("mobile"))
                    .addParameter("peopleRole", conditions.get("peopleRole"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("chargerId", conditions.get("chargerId"))
                    .addParameter("createUserId", conditions.get("createUserId"))
                    .addParameter("createStartDate", createStartDate)
                    .addParameter("createEndDate", createEndDate)
                    .addParameter("state", conditions.get("state") == null ? 1 : conditions.get("state"))
                    .executeAndFetch(PeopleInfo.class);

            for (val people : list) {
                val projectRelatedSqlList = con.createQuery("select p.ProjectId 'project.projectId',p.ProjectName 'project.projectName', p.ProjectType 'project.projectType' ," +
                        "r.RelatedPeopleRole 'relatedPeopleRole' " +
                        " from tbprojectrelated r ,tbproject p " +
                        " where p.ProjectId=r.ProjectId and r.RelatedPeopleId= :peopleId")
                        .addParameter("peopleId", people.getPeopleId())
                        .executeAndFetch(ProjectRelatedInfo.class);
                if (CollectionUtil.isNotEmpty(projectRelatedSqlList)) {
                    people.setProjectRelatedList(projectRelatedSqlList);
                }
            }


            return list;
        }
    }

    public static int count(Map conditions) {

        val sql = " SELECT count(1) xcount FROM tbPeople " +
                " WHERE AccountId= :accountId  AND State = :state " +
                "      AND (DepartmentId=:departmentId OR :departmentId IS NULL) " +
                "      AND (PeopleName LIKE CONCAT('%',:peopleName,'%') OR :peopleName is null) " +
                "      AND (IFNULL(PeopleRole,'') NOT LIKE CONCAT('%','业主','%') OR :filterOwner is null) " +
                "      AND (Mobile LIKE CONCAT('%',:mobile,'%') OR :mobile is null)" +
                "      AND (PeopleRole LIKE CONCAT('%',:peopleRole,'%') OR :peopleRole is null)" +
                "      AND (ChargerId = :userId OR CreateUserId = :userId OR HandoverUserId=:userId OR :userId is null ) " +       // 非管理员只能看自己负责或者创建的人员
                "      AND (ChargerId = :chargerId OR :chargerId is null ) " + //界面按负责人查询
                "      AND (CreateUserId = :createUserId OR :createUserId is null)" +
                "      AND ( ( CreateDateTime Between :createStartDate and :createEndDate ) Or (:createStartDate is null and :createEndDate is null)) ";

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
                    .addParameter("peopleName", conditions.get("peopleName"))
                    .addParameter("filterOwner", conditions.get("filterOwner"))
                    .addParameter("mobile", conditions.get("mobile"))
                    .addParameter("peopleRole", conditions.get("peopleRole"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("chargerId", conditions.get("chargerId"))
                    .addParameter("createUserId", conditions.get("createUserId"))
                    .addParameter("createStartDate", createStartDate)
                    .addParameter("createEndDate", createEndDate)
                    .addParameter("state", conditions.get("state") == null ? 1 : conditions.get("state"))
                    .executeScalar(int.class);
        }
    }

    public static List<PeopleInfo> list(String accountId, Integer departmentId) {

        val sql = " SELECT PeopleId ,PeopleName ,PeopleSuffix ,Mobile ,WechatId ,Gender ,AgeAlias ,Birthday ," +
                " ChargerId 'charger.userId' , ChargerName 'charger.userName' ,MaritalStatus ,ChildrenStatus ," +
                " Unit ,Cost ,PeopleRole ,RelationWithCharger ,Remark ,UserId 'user.userId' ,UserName 'user.userName',Address,FirstContact,UpdateTime" +
                " FROM tbPeople Where AccountId = :accountId AND State = 1 " +
                " AND (DepartmentId= :departmentId OR :departmentId IS NULL)";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("departmentId", departmentId)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(PeopleInfo.class);
        }
    }

    public static PeopleInfo load(String accountId, String id) {

        val sql = " SELECT PeopleId ,PeopleName ,PeopleSuffix ,Mobile ,WechatId ,Gender ,AgeAlias ,Birthday ," +
                " ChargerId 'charger.userId' , ChargerName 'charger.userName' ,MaritalStatus ,ChildrenStatus ," +
                " Unit ,Cost ,PeopleRole ,RelationWithCharger ,Remark ," +
                " CreateUserId 'createUser.userId' ,CreateUserName 'createUser.userName',CreateDateTime ," +
                " UserId 'user.userId' ,UserName 'user.userName' ,Address,FirstContact,UpdateTime" +
                " FROM tbPeople WHERE AccountId= :accountId AND PeopleId = :id";

        try (val con = db.sql2o.open()) {

            val people = con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeAndFetchFirst(PeopleInfo.class);

            if (people != null) {

                val peopleRelatedList = con.createQuery(
                        "select RelatedPeopleId 'relatedPeople.peopleId' ,RelatedPeopleName 'relatedPeople.peopleName' ,RelatedPeopleRole " +
                                " from tbPeopleRelated " +
                                " where AccountId= :accountId AND PeopleId = :id order by sortNumber")
                        .addParameter("accountId", accountId)
                        .addParameter("id", id)
                        .executeAndFetch(PeopleRelatedInfo.class);
                if (CollectionUtil.isNotEmpty(peopleRelatedList)) {
                    people.setPeopleRelatedList(peopleRelatedList);
                }

                //参与的项目（取项目干系人）
                val projectRelatedSqlList = con.createQuery("select p.ProjectId 'project.projectId',p.ProjectName 'project.projectName', p.ProjectType 'project.projectType' ," +
                        "r.RelatedPeopleRole 'relatedPeopleRole' " +
                        " from tbprojectrelated r ,tbproject p " +
                        " where p.AccountId=r.AccountId " +
                        "   and p.ProjectId=r.ProjectId " +
                        "   and r.AccountId= :accountId " +
                        "   and r.RelatedPeopleId= :id " +
                        //projectByPeopleAsOwner
                        "UNION SELECT p.ProjectId 'project.projectId',p.ProjectName 'project.projectName', p.ProjectType 'project.projectType' ," +
                        "'业主' AS 'relatedPeopleRole' " +
                        " from tbProject p " +
                        " where p.AccountId=:accountId AND p.OwnerId=:id ")
                        .addParameter("accountId", accountId)
                        .addParameter("id", id)
                        .executeAndFetch(ProjectRelatedInfo.class);
                if (CollectionUtil.isNotEmpty(projectRelatedSqlList)) {
                    people.setProjectRelatedList(projectRelatedSqlList);
                }
            }

            return people;
        }
    }

    public static PeopleInfo loadPeopleBaseInfo(String accountId, String id) {

        val sql = " SELECT PeopleId ,PeopleName ,PeopleSuffix ,Mobile ,WechatId ,Gender ,AgeAlias ,Birthday ," +
                " ChargerId 'charger.userId' , ChargerName 'charger.userName' ,MaritalStatus ,ChildrenStatus ," +
                " Unit ,Cost ,PeopleRole ,RelationWithCharger ,Remark ,UserId 'user.userId' ,UserName 'user.userName' ,Address,FirstContact,UpdateTime" +
                " FROM tbPeople WHERE AccountId= :accountId And PeopleId = :id";

        try (val con = db.sql2o.open()) {

            val people = con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeAndFetchFirst(PeopleInfo.class);

            if (people != null) {

                val peopleRelatedList = con.createQuery(
                        "select RelatedPeopleId 'relatedPeople.peopleId' ,RelatedPeopleName 'relatedPeople.peopleName' ,RelatedPeopleRole " +
                                " from tbPeopleRelated " +
                                " where AccountId= :accountId And PeopleId = :id order by sortNumber")
                        .addParameter("accountId", accountId)
                        .addParameter("id", id)
                        .executeAndFetch(PeopleRelatedInfo.class);
                if (CollectionUtil.isNotEmpty(peopleRelatedList)) {
                    people.setPeopleRelatedList(peopleRelatedList);
                }

                //参与的项目（取项目干系人）
                val projectRelatedSqlList = con.createQuery("select p.ProjectId 'project.projectId',p.ProjectName 'project.projectName', p.ProjectType 'project.projectType' ," +
                        "r.RelatedPeopleRole 'relatedPeopleRole' " +
                        " from tbprojectrelated r ,tbproject p " +
                        " where p.AccountId=r.AccountId " +
                        "   and p.ProjectId=r.ProjectId " +
                        "   and r.AccountId= :accountId " +
                        "   and r.RelatedPeopleId= :id")
                        .addParameter("accountId", accountId)
                        .addParameter("id", id)
                        .executeAndFetch(ProjectRelatedInfo.class);
                if (CollectionUtil.isNotEmpty(projectRelatedSqlList)) {
                    people.setProjectRelatedList(projectRelatedSqlList);
                }
            }

            return people;
        }
    }

    public static void insert(PeopleInfo people) {

        if (people.getCharger() == null) {
            people.setCharger(new UserInfo());
        }

        val sql = "INSERT INTO tbPeople(DepartmentId, AccountId ,PeopleId ,PeopleName ,PeopleSuffix ,Mobile ,WechatId ,Gender ,AgeAlias ,Birthday ," +
                "   ChargerId , ChargerName ,MaritalStatus ,ChildrenStatus ,Unit ,Cost ,PeopleRole ,RelationWithCharger ,Remark ," +
                "   CreateUserId ,CreateUserName ,CreateDateTime ,UserId, UserName ,Address,FirstContact,UpdateTime) " +
                " values (:departmentId, :accountId ,:peopleId ,:peopleName ,:peopleSuffix ,:mobile ,:wechatId ,:gender ,:ageAlias ,:birthday ," +
                "   :chargerId , :chargerName ,:maritalStatus ,:childrenStatus ,:unit ,:cost ,:peopleRole ,:relationWithCharger ,:remark ," +
                "   :createUserId ,:createUserName ,:createDateTime ,:userId ,:userName ,:address ,:firstContact ,:updateTime) ";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql).bind(people)
                    .addParameter("chargerId", people.getCharger().getUserId())
                    .addParameter("chargerName", people.getCharger().getUserName())
                    .addParameter("createUserId", people.getCreateUser().getUserId())
                    .addParameter("createUserName", people.getCreateUser().getUserName())
                    .addParameter("createDateTime", people.getCreateDateTime())
                    .addParameter("userId", people.getUser().getUserId())
                    .addParameter("userName", people.getUser().getUserName())
                    .executeUpdate();

            if (CollectionUtil.isNotEmpty(people.getPeopleRelatedList())) {

                val peopleId = people.getPeopleId();
                val accountId = people.getAccountId();

                //写新建人的干系人
                val relatedSql = " INSERT INTO tbPeopleRelated(AccountId ,PeopleId , SortNumber, RelatedPeopleId ,RelatedPeopleName,RelatedPeopleRole) " +
                        "Values( :accountId ,:peopleId , :sortNumber ,:relatedPeopleId ,:relatedPeopleName ,:relatedPeopleRole )";
                val query = con.createQuery(relatedSql);

                //将新建的人员插入干系人的干系人中
                val relatedSql2 = " INSERT INTO tbPeopleRelated(AccountId ,PeopleId , SortNumber, RelatedPeopleId ,RelatedPeopleName) " +
                        " select AccountId ,:relatedPeopleId, :sortNumber , PeopleId ,PeopleName  from tbPeople Where AccountId= :accountId and peopleId=:peopleId ;";
                val query2 = con.createQuery(relatedSql2);

                for (int i = 0; i < people.getPeopleRelatedList().size(); i++) {

                    val related = people.getPeopleRelatedList().get(i);
                    val relatedPeopleId = related.getRelatedPeople().getPeopleId();
                    val relatedPeopleName = related.getRelatedPeople().getPeopleName();
                    val relatedPeopleRole = related.getRelatedPeopleRole();

                    //写新建人的干系人
                    query.addParameter("accountId", accountId);
                    query.addParameter("peopleId", peopleId);
                    query.addParameter("sortNumber", i);
                    query.addParameter("relatedPeopleId", relatedPeopleId);
                    query.addParameter("relatedPeopleName", relatedPeopleName);
                    query.addParameter("relatedPeopleRole", relatedPeopleRole);
                    query.addToBatch();

                    //将新建的人员插不入干系人的干系人中

                    int maxSortNumber = con.createQuery(" Select COALESCE(Max(SortNumber),0) maxSortNumber from tbPeopleRelated where AccountId= :accountId And PeopleId= :relatedPeopleId")
                            .addParameter("accountId", accountId)
                            .addParameter("relatedPeopleId", relatedPeopleId)
                            .executeScalar(int.class);

                    query2.addParameter("accountId", accountId);
                    query2.addParameter("peopleId", peopleId);
                    query2.addParameter("sortNumber", maxSortNumber + 1);
                    query2.addParameter("relatedPeopleId", relatedPeopleId);
                    query2.addToBatch();

                }

                query.executeBatch();
                query2.executeBatch();
            }

            con.commit();
        }
    }

    public static void update(String accountId, PeopleInfo people) {

        if (people.getCharger() == null) {
            people.setCharger(new UserInfo());
        }

        val sql = "UPDATE tbPeople SET " +
                " PeopleId = :peopleId ," +
                " PeopleName = :peopleName ," +
                " PeopleSuffix = :peopleSuffix ," +
                " Mobile = :mobile ," +
                " WechatId = :wechatId ," +
                " Gender = :gender ," +
                " AgeAlias= :ageAlias ," +
                " Birthday = :birthday ," +
                " ChargerId = :chargerId , " +
                " ChargerName = :chargerName ," +
                " MaritalStatus = :maritalStatus ," +
                " ChildrenStatus = :childrenStatus," +
                " Unit = :unit ," +
                " Cost = :cost ," +
                " PeopleRole = :peopleRole ," +
                " RelationWithCharger = :relationWithCharger ," +
                " Remark = :remark ," +
                " UserId=:userId ," +
                " UserName=:userName ," +
                " UpdateTime = :updateTime, " +
                " Address = :address, " +
                " FirstContact = :firstContact " +
                " WHERE AccountId= :accountId And PeopleId = :peopleId ";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql).bind(people)
                    .addParameter("accountId", accountId)
                    .addParameter("chargerId", people.getCharger().getUserId())
                    .addParameter("chargerName", people.getCharger().getUserName())
                    .addParameter("userId", people.getUser().getUserId())
                    .addParameter("userName", people.getUser().getUserName())
                    .executeUpdate();

            con.createQuery("DELETE FROM tbPeopleRelated WHERE AccountId= :accountId And PeopleId = :id")
                    .addParameter("accountId", accountId)
                    .addParameter("id", people.getPeopleId())
                    .executeUpdate();

            if (CollectionUtil.isNotEmpty(people.getPeopleRelatedList())) {

                val peopleId = people.getPeopleId();

                val relatedSql = "INSERT INTO tbPeopleRelated(AccountId ,PeopleId , SortNumber, RelatedPeopleId ,RelatedPeopleName,RelatedPeopleRole) " +
                        "Values( :accountId ,:peopleId , :sortNumber ,:relatedPeopleId ,:relatedPeopleName ,:relatedPeopleRole )";
                val query = con.createQuery(relatedSql);

                //将人员插入干系人的干系人中
                val relatedSql2 = " INSERT INTO tbPeopleRelated(AccountId,PeopleId , SortNumber, RelatedPeopleId ,RelatedPeopleName) " +
                        " select AccountId ,:relatedPeopleId, :sortNumber , PeopleId ,PeopleName from tbPeople " +
                        " Where AccountId= :accountId And peopleId=:peopleId " +
                        "   And Not Exists(select * from tbPeopleRelated where AccountId= :accountId And peopleId=:relatedPeopleId and RelatedPeopleId=:peopleId );";
                val query2 = con.createQuery(relatedSql2);

                for (int i = 0; i < people.getPeopleRelatedList().size(); i++) {

                    val related = people.getPeopleRelatedList().get(i);
                    val relatedPeopleId = related.getRelatedPeople().getPeopleId();
                    val relatedPeopleName = related.getRelatedPeople().getPeopleName();
                    val relatedPeopleRole = related.getRelatedPeopleRole();

                    query.addParameter("accountId", accountId);
                    query.addParameter("peopleId", people.getPeopleId());
                    query.addParameter("sortNumber", i);
                    query.addParameter("relatedPeopleId", relatedPeopleId);
                    query.addParameter("relatedPeopleName", relatedPeopleName);
                    query.addParameter("relatedPeopleRole", relatedPeopleRole);
                    query.addToBatch();

                    //将新建的人员插不入干系人的干系人中

                    int maxSortNumber = con.createQuery(" Select COALESCE(Max(SortNumber),0) maxSortNumber from tbPeopleRelated where AccountId= :accountId And PeopleId= :relatedPeopleId")
                            .addParameter("accountId", accountId)
                            .addParameter("relatedPeopleId", relatedPeopleId)
                            .executeScalar(int.class);

                    query2.addParameter("accountId", accountId);
                    query2.addParameter("peopleId", peopleId);
                    query2.addParameter("sortNumber", maxSortNumber + 1);
                    query2.addParameter("relatedPeopleId", relatedPeopleId);
                    query2.addToBatch();
                }

                query.executeBatch();
                query2.executeBatch();
            }

            con.commit();
        }
    }

    public static void delete(String accountId, String id) {

        try (val con = db.sql2o.open()) {
            Arrays.asList(
                    " DELETE FROM tbPeopleRelated WHERE AccountId= :accountId And (PeopleId = :id or RelatedPeopleId = :id ); ",
                    " DELETE FROM tbProjectRelated WHERE AccountId= :accountId And RelatedPeopleId = :id ; ",
                    " DELETE FROM tbPeopleRecord WHERE AccountId= :accountId And PeopleId = :id; ",
                    " DELETE FROM tbPeople WHERE AccountId= :accountId And PeopleId = :id ; ")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("accountId", accountId)
                                    .addParameter("id", id)
                                    .executeUpdate()
                    );
        }
    }

    public static List<PeopleInfo> getToDelList() {
        val sql = "SELECT PeopleId,AccountId from tbPeople WHERE state = 0 AND DATE_ADD(UpdateTime,INTERVAL 7 day) <= NOW()";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql).executeAndFetch(PeopleInfo.class);
        }
    }

    /**
     * 批量安全删除与还原
     */
    public static void safeDelete(String accountId, String peopleIds, Boolean state) {

        val sql = " UPDATE tbPeople SET state = :state, UpdateTime=NOW() WHERE AccountId = :accountId AND PeopleId IN " + peopleIds.replace("[", "(").replace("]", ")");

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).addParameter("state", state == null ? false : state)
                    .addParameter("accountId", accountId)
                    .executeUpdate();
        }
    }
}