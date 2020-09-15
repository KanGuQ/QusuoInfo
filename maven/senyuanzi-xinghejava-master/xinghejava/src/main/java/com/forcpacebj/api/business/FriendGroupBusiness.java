package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.FriendGroupInfo;
import lombok.val;

import java.util.Arrays;
import java.util.List;

public class FriendGroupBusiness {

    public static List<FriendGroupInfo> list(String accountId) {

        val sql = " SELECT GroupId ,GroupName FROM tbFriendGroup where AccountId= :accountId ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(FriendGroupInfo.class);
        }
    }

    public static void insert(String accountId, FriendGroupInfo obj) {

        val sql = "INSERT INTO tbFriendGroup(AccountId ,GroupId ,GroupName) " +
                " values (:accountId ,:groupId ,:groupName)";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(obj)
                    .addParameter("accountId", accountId)
                    .executeUpdate();
        }
    }

    public static void update(String accountId, FriendGroupInfo obj) {

        val sql = "UPDATE tbFriendGroup SET GroupName = :groupName " +
                " WHERE AccountId = :accountId AND GroupId = :groupId ";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(obj)
                    .addParameter("accountId", accountId)
                    .executeUpdate();
        }
    }

    public static void delete(String accountId, String groupId) {

        try (val con = db.sql2o.beginTransaction()) {

            Arrays.asList(
                    " DELETE FROM tbFriendGroup WHERE AccountId= :accountId AND GroupId = :groupId ",
                    " UPDATE tbFriend SET GroupId = '' WHERE AccountId= :accountId And GroupId = :groupId ",
                    " DELETE FROM tbFriendSalePrice WHERE AccountId= :accountId And FriendGroupId = :groupId ")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("accountId", accountId)
                                    .addParameter("groupId", groupId)
                                    .executeUpdate()
                    );

            con.commit();
        }
    }
}