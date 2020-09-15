package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.ProjectFileInfo;
import lombok.val;
import lombok.var;

import java.util.List;

/**
 * Created by pc on 2020/2/8.
 */
public class ProjectFileBusiness {

    public static List<ProjectFileInfo> list(String projectId, String accountId) {
        val sql = "SELECT Id,FileUrl,Name,CreateTime FROM tbProjectFile WHERE ProjectId = :projectId AND AccountId = :accountId ORDER BY SortNumber";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("projectId", projectId)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(ProjectFileInfo.class);
        }
    }

    public static void insert(ProjectFileInfo attachFile) {
        val sql = "INSERT INTO tbProjectFile (ProjectId,AccountId,FileUrl,Name,SortNumber) " +
                " VALUES (:projectId, :accountId, :fileUrl, :name, :sortNumber)";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(attachFile).executeUpdate();
        }
    }

    public static void update(List<ProjectFileInfo> files) {
        val sql = "UPDATE tbProjectFile " +
                " SET SortNumber=:sortNumber " +
                " WHERE Id = :id";
        try (val con = db.sql2o.beginTransaction()) {
            var query = con.createQuery(sql);
            for (ProjectFileInfo file : files) {
                query.bind(file).addToBatch();
            }
            query.executeBatch();
            con.commit();
        }
    }

    public static void delete(Integer id) {
        val sql = "DELETE FROM tbProjectFile WHERE Id = :id";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql).addParameter("id", id).executeUpdate();
        }
    }
}
