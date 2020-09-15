package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.AreaInfo;
import com.forcpacebj.api.entity.ProductInfo;
import lombok.val;
import lombok.var;

import java.util.List;
import java.util.Map;

/**
 * {@link AreaInfoBusiness}
 * Author: ACL
 * Date:2020/02/26
 * Description: 区域 Business
 * Created by ACL on 2020/02/26.
 */
public class AreaInfoBusiness {


    //根据父类节点 查询所有的子节点
    public  static List<AreaInfo> findAll(Map conditions){

        var sql="select  * from tbArea where parent_id=:parentId";
        try (val con = db.sql2o.open()) {

            return con.createQuery( sql )
                    .addParameter( "parentId", conditions.get( "parentId" ) )
                    .executeAndFetch( AreaInfo.class );
        }
    }
    //根据父类节点 查询所有的子节点
    public  static List<AreaInfo> findList(Map conditions){

        var sql="select  * from tbArea where parent_id!=:parentId";
        try (val con = db.sql2o.open()) {

            return con.createQuery( sql )
                    .addParameter( "parentId", conditions.get( "parentId" ) )
                    .executeAndFetch( AreaInfo.class );
        }
    }
    /**
     * 更加adcode 查询 父节点信息
     * @param id
     * @return
     */
    public static AreaInfo findByAdCode(String id){
        var sql="select id, parent_id ,level_type from  tbArea where id=:id";

        try (val con = db.sql2o.open()) {

            return con.createQuery( sql )
                    .addParameter( "id", id )
                    .executeAndFetchFirst( AreaInfo.class );
        }
    }
}
