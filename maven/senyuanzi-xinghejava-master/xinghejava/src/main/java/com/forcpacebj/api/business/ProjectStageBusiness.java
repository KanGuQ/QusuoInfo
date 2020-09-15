package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.ProjectStageInfo;
import lombok.val;
import lombok.var;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pc on 2020/2/19.
 */
public class ProjectStageBusiness {

    /**
     * 商机阶段
     */
    public static List<ProjectStageInfo> getStage(String accountId, Integer type) {
        val sql = "SELECT Id,StageName,Type,IsShow,AccountId,SortNum FROM ProjectStage WHERE accountId = :accountId AND TYPE = :type ORDER BY SortNum";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("type", type)
                    .executeAndFetch(ProjectStageInfo.class);
        }
    }

    public static void insert(ProjectStageInfo stage) {
        val sql = "INSERT INTO ProjectStage (StageName,Type,AccountId,SortNum,IsShow)" +
                "VALUES(:stageName,:type,:accountId,:sortNum,:isShow)";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(stage)
                    .executeUpdate();
        }
    }

    public static void update(List<ProjectStageInfo> stages) {
        val sql = "UPDATE ProjectStage SET " +
                "StageName=:stageName," +
                "Type=:type," +
                "IsShow=:isShow," +
                "SortNum=:sortNum " +
                " WHERE Id = :id";
        try (val con = db.sql2o.beginTransaction()) {
            var query = con.createQuery(sql);
            for (ProjectStageInfo stage : stages) {
                query.bind(stage).addToBatch();
            }
            query.executeBatch();
            con.commit();
        }
    }

    public static void delete(Integer id) {
        try (val con = db.sql2o.beginTransaction()) {
            Arrays.asList(
                    " DELETE FROM ProjectStage WHERE Id = :id",
                    " UPDATE tbProject SET stageId = null WHERE stageId = :id")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("id", id)
                                    .executeUpdate()
                    );
            con.commit();
        }
    }

    public static Integer getStageType(Object id){
        val sql = "SELECT Type FROM ProjectStage WHERE Id = :id";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql).addParameter("id", id)
                    .executeScalar(Integer.class);
        }
    }

}
