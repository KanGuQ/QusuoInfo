package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.ConstructStageInfo;
import lombok.val;
import lombok.var;

import java.util.List;

/**
 * Created by pc on 2020/3/17.
 */
public class ConstructStageBusiness {

    public static List<ConstructStageInfo> list(String accountId) {
        var sql = "select Id,AccountId,Name,SortNum,CreateTime,UpdateTime from ConstructStage " +
                " where AccountId=:accountId order by SortNum";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(ConstructStageInfo.class);
        }
    }

    public static void insert(ConstructStageInfo stage){
        val sql = "INSERT INTO ConstructStage (Name,AccountId,SortNum,CreateTime)" +
                "VALUES(:name,:accountId,:sortNum,NOW())";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(stage)
                    .executeUpdate();
        }
    }

    public static void update(List<ConstructStageInfo> list){
        var sql = "UPDATE ConstructStage SET Name=:name,SortNum=:sortNum WHERE Id = :id";
        try (val con = db.sql2o.beginTransaction()) {
            var query = con.createQuery(sql);
            for (ConstructStageInfo s : list) {
                query.bind(s).addToBatch();
            }
            con.commit();
        }
    }

    public static void delete(Integer id){
        var sql = "DELETE FROM ConstructStage WHERE Id = :id";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql).addParameter("id", id)
                    .executeUpdate();
        }
    }

}
