package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.RecordMsgInfo;
import lombok.val;

import java.util.List;

/**
 * 评论业务逻辑
 */
public class RecordMsgBusiness {

    public static List<RecordMsgInfo> list(String accountId, String recordId) {

        val sql = " SELECT MsgId ,RecordId 'record.recordId' ,PostContent ,UserId 'postUser.userId' ,UserName 'postUser.userName' ,PostTime " +
                "     FROM tbRecordMsg " +
                " WHERE AccountId= :accountId And RecordId= :recordId";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("recordId", recordId)
                    .executeAndFetch(RecordMsgInfo.class);
        }
    }

    /**
     * 插入日志评论
     *
     * @param msg
     * @return 日志评论自增ID
     */
    public static int insert(String accountId, RecordMsgInfo msg) {

        val sql = " INSERT INTO tbRecordMsg(AccountId ,RecordId ,PostContent ,UserId ,UserName ,PostTime) " +
                " values (:accountId ,:recordId ,:postContent ,:userId ,:userName ,:postTime) ";


        try (val con = db.sql2o.open()) {
            return con.createQuery(sql).bind(msg)
                    .addParameter("accountId", accountId)
                    .addParameter("recordId", msg.getRecord().getRecordId())
                    .addParameter("userId", msg.getPostUser().getUserId())
                    .addParameter("userName", msg.getPostUser().getUserName())
                    .executeUpdate()
                    .getKey(Integer.class);
        }
    }

    public static void delete(String accountId, String id) {

        val sql = " DELETE FROM tbRecordMsg WHERE MsgId = :id and AccountId= :accountId";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeUpdate();
        }
    }
}