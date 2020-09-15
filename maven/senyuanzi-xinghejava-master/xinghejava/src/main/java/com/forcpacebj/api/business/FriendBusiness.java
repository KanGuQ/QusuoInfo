package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.FriendInfo;
import com.forcpacebj.api.utils.DateUtil;
import lombok.val;

import java.util.List;
import java.util.Map;

public class FriendBusiness {

    public static List<FriendInfo> find(Map conditions, String accountId) {

        val sql = " SELECT F.AccountId ,F.FriendAccountId 'friendAccount.accountId' ,A.AccountName 'friendAccount.accountName' ," +
                  " F.CreateTime ,F.IsAcceptable ,F.IsAccepted ,F.AcceptTime ,F.GroupId 'friendGroup.groupId' ,G.GroupName 'friendGroup.groupName' ,F.Remark,F.isAuth" +
                  " FROM tbFriend F" +
                  " LEFT JOIN tbAccount A on F.FriendAccountId = A.AccountId " +
                  " LEFT JOIN tbFriendGroup G on F.AccountId = G.AccountId And F.GroupId = G.GroupId " +
                  " WHERE F.AccountId= :accountId " +
                  "   AND (F.GroupId = :friendGroupId  OR :friendGroupId is null) " +
                  "   AND (A.AccountName LIKE CONCAT('%',:friendAccountName,'%') OR :friendAccountName is null) " +
                  "   AND (F.Remark LIKE CONCAT('%',:remark,'%') OR :remark is null) " +
                  " Order By F.CreateTime DESC" +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("friendAccountName", conditions.get("friendAccountName"))
                    .addParameter("remark", conditions.get("remark"))
                    .addParameter("friendGroupId", conditions.get("friendGroupId"))
                    .executeAndFetch(FriendInfo.class);
        }
    }

    public static int count(Map conditions, String accountId) {

        val sql = " SELECT count(*) xcount FROM tbFriend F " +
                  "   LEFT JOIN tbAccount A on F.FriendAccountId = A.AccountId " +
                  "   WHERE F.AccountId= :accountId " +
                  "   AND (F.GroupId = :friendGroupId  OR :friendGroupId is null) " +
                  "   AND (A.AccountName LIKE CONCAT('%',:friendAccountName,'%') OR :friendAccountName is null) " +
                  "   AND (F.Remark LIKE CONCAT('%',:remark,'%') OR :remark is null) ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("friendAccountName", conditions.get("friendAccountName"))
                    .addParameter("remark", conditions.get("remark"))
                    .addParameter("friendGroupId", conditions.get("friendGroupId"))
                    .executeScalar(int.class);
        }
    }

    public static FriendInfo load(String accountId, String friendAccountId) {

        val sql = " SELECT AccountId ,FriendAccountId 'friendAccount.accountId', GroupId 'friendGroup.groupId' ,CreateTime ,IsAccepted ,AcceptTime " +
                  " FROM tbFriend " +
                  " WHERE AccountId= :accountId AND FriendAccountId = :friendAccountId";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("friendAccountId", friendAccountId)
                    .executeAndFetchFirst(FriendInfo.class);
        }
    }

    public static void insert(String accountId, FriendInfo obj) {

        val sql = "INSERT INTO tbFriend(AccountId ,FriendAccountId ,CreateTime ,IsAcceptable ,IsAccepted ) " +
                  " values(:accountId ,:friendAccountId ,:createTime ,:isAcceptable ,0)";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql).bind(obj)
                    .addParameter("accountId", accountId)
                    .addParameter("friendAccountId", obj.getFriendAccount().getAccountId())
                    .addParameter("isAcceptable", 0)
                    .executeUpdate();

            con.createQuery(sql).bind(obj)
                    .addParameter("accountId", obj.getFriendAccount().getAccountId())
                    .addParameter("friendAccountId", accountId)
                    .addParameter("isAcceptable", 1)
                    .executeUpdate();

            con.commit();
        }
    }

    public static void downloadProductBecomeFriend(String accountId, String friendAccountId, String remark) {

        val sql = "INSERT INTO tbFriend(AccountId ,FriendAccountId ,CreateTime ,AcceptTime ,IsAcceptable ,IsAccepted ,Remark) " +
                  " values(:accountId ,:friendAccountId ,now() ,now() ,:isAcceptable ,1 ,:remark)";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("friendAccountId", friendAccountId)
                    .addParameter("isAcceptable", 0)
                    .addParameter("remark", remark)
                    .executeUpdate();

            con.createQuery(sql)
                    .addParameter("accountId", friendAccountId)
                    .addParameter("friendAccountId", accountId)
                    .addParameter("isAcceptable", 1)
                    .addParameter("remark", remark)
                    .executeUpdate();

            con.commit();
        }
    }

    public static void accept(String accountId, String friendAccountId) {

        val now = DateUtil.now();

        val sql = "UPDATE tbFriend SET " +
                  "        IsAccepted = :isAccepted ," +
                  "        AcceptTime = :acceptTime" +
                  " WHERE AccountId= :accountId AND FriendAccountId = :friendAccountId ";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql)
                    .addParameter("isAccepted", true)
                    .addParameter("acceptTime", now)
                    .addParameter("accountId", accountId)
                    .addParameter("friendAccountId", friendAccountId)
                    .executeUpdate();

            con.createQuery(sql)
                    .addParameter("isAccepted", true)
                    .addParameter("acceptTime", now)
                    .addParameter("accountId", friendAccountId)
                    .addParameter("friendAccountId", accountId)
                    .executeUpdate();

            con.commit();
        }
    }

    public static void update(String accountId, FriendInfo obj) {

        val sql = "UPDATE tbFriend SET " +
                  "        GroupId = :groupId ," +
                  "        Remark = :remark " +
                  " WHERE AccountId= :accountId AND FriendAccountId = :friendAccountId ";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(obj)
                    .addParameter("accountId", accountId)
                    .addParameter("groupId", obj.getFriendGroup().getGroupId())
                    .addParameter("friendAccountId", obj.getFriendAccount().getAccountId())
                    .executeUpdate();
        }
    }

    public static void delete(String accountId, String friendAccountId) {

        val sql = "DELETE FROM tbFriend WHERE AccountId= :accountId AND FriendAccountId = :friendAccountId";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("friendAccountId", friendAccountId)
                    .executeUpdate();

            con.createQuery(sql)
                    .addParameter("accountId", friendAccountId)
                    .addParameter("friendAccountId", accountId)
                    .executeUpdate();

            con.commit();
        }
    }

    public static void canVisit(String accountId, String friendAccountIds, Boolean canVisit) {
        val sql = "UPDATE tbFriend SET " +
                  "        isAuth = :isAuth " +
                  " WHERE AccountId= :accountId AND FriendAccountId in "
                  + friendAccountIds.replace("[", "(").replace("]", ")");
        ;

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("isAuth", canVisit)
                    .addParameter("accountId", accountId)
                    .executeUpdate();
        }
    }
}
