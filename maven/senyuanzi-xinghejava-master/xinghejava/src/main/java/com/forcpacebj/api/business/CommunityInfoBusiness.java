package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.*;
import com.forcpacebj.api.utils.CollectionUtil;
import lombok.val;
import lombok.var;
import org.sql2o.Connection;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * {@link CommunityInfoBusiness}
 * Author: ACL
 * Date:2020/02/22
 * Description:小区  业务类
 * Created by ACL on 2020/02/22.
 */
public class CommunityInfoBusiness {

    public static CommunityInfo load(String accountId,Map condition) {

        val sql ="select c.id, c.accountId,name,createUserId 'createUser.userId',CreateUserName 'createUser" +
                ".userName',provinceName,cityName,countyName,address,lat,lng,c.remarks,c.createDate,c.lastUpdateDate," +
                "filePath," +
                "pictureUrl," +
                "chargerId " +
                "'charger.userId',t.userName 'charger.UserName' " +
                "from tbCommunity c   " +
                "left join tbuser t on t.userId=c.chargerId " +
                "where c.createUserId=:userId  and c.accountId=:accountId " +
                " and c.id=:id" ;

        try (val con = db.sql2o.open()) {
            CommunityInfo communityInfo= con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", condition.get( "id" ))
                    .addParameter( "userId",condition.get( "userId" ) )
                    .executeAndFetchFirst( CommunityInfo.class);
            // 添加 项目成员
            communityInfo.setCommunitiesVisibleUsers(  CommunityVisibleUserBusiness.findByCommunityIdAndAccountId(communityInfo.getId(),
                    accountId  ));

            // 添加相关的 项目列表
            communityInfo.setCommunityProjectRelatedInfos( CommunityProjectRelatedBusiness.findByCommunityIdAndAccountId( communityInfo.getId(),accountId ) );

            // 查询干系人
            communityInfo.setCommunityPeopleRelatedInfos( CommunityPeopleRelatedBusiness.findByCommunityIdAndAccountId(communityInfo.getId(),accountId) );

            return communityInfo;
        }


    }

    /**
     * 查询所有 小区信息
     * @param conditions  查询条件
     * @param accountId 租户id
     *
     * @return 小区列表
     */
    public static List<CommunityInfo> find(Map conditions, String accountId) {
        val sql = "select c.id, c.accountId,name,createUserId 'createUser.userId',CreateUserName 'createUser" +
                ".userName',provinceName,cityName,countyName,address,lat,lng,c.remarks,c.createDate,c.lastUpdateDate," +
                "filePath," +
                "pictureUrl," +
                "chargerId " +
                "'charger.userId',t.userName 'charger.UserName' " +
                "from tbCommunity c   " +
                "left join tbuser t on t.userId=c.chargerId " +
                "where c.createUserId=:userId  and c.accountId=:accountId " +
                " and (c.name  like :name or :name is null) "+
                " and (c.address like :address or :address is null) "+
                " order by lastUpdateDate desc "+
                "LIMIT :PAGEOFFSET ,:PAGESIZE";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId) //租户id
                    .addParameter("userId", conditions.get( "userId" )) //用户id
                    .addParameter("name", conditions.get( "name" )!=null?"%"+conditions.get( "name" )+"%":null) //小区名称
                    .addParameter("address", conditions.get( "address" )!=null ?"%"+conditions.get( "address" )+"%":
                            null) //小区地址
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET")) //分页
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch( CommunityInfo.class);
        }
    }


    //删除小区信息
    public static void delete(String accountId, String id) {
        try (val con = db.sql2o.beginTransaction()) {
            Arrays.asList( " delete from tbCommunity where id=:id and accountId=:accountId;",
                    " delete from tbCommunityVisibleUser where communityId=:id and accountId=:accountId ; "
                    )
                    .forEach( s ->
                            con.createQuery( s )
                                    .addParameter( "accountId", accountId )
                                    .addParameter( "id", id )
                                    .executeUpdate()
                    );
            con.commit();
        }
    }

    //更新小区信息
    public static void update(String accountId, CommunityInfo communityInfo){
        // 保持小区信息
        var sql=" update tbCommunity set name=:name,provinceName=:provinceName,cityName=:cityName, " +
                "countyName=:cityName,address=:address,lat=:lat,lng=:lng,remarks=:remarks, " +
                "lastUpdateDate=:lastUpdateDate ,pictureUrl=:pictureUrl,chargerId=:chargerId where id=:id and " +
                "accountId=:accountId";
        try (val con = db.sql2o.beginTransaction()) {
             con.createQuery( sql ).bind( communityInfo )
                    .addParameter( "lastUpdateDate", new Date() )
                    .addParameter( "chargerId", communityInfo.getCharger().getUserId() )
                    .addParameter( "accountId", accountId )
                    .executeUpdate();
            //删除 对谁可见
            var visibleUserSqlDelete="delete from tbCommunityVisibleUser where accountId=:accountId and " +
                    "communityId=:id";
            con.createQuery( visibleUserSqlDelete ).addParameter( "accountId",accountId )
                    .addParameter( "id",communityInfo.getId() )
                    .executeUpdate();
            // 保存 项目成员  对谁可见
            if (CollectionUtil.isNotEmpty( communityInfo.getCommunitiesVisibleUsers() )) {
                val visibleUserSql = "INSERT INTO tbCommunityVisibleUser(AccountId ,communityId , SortNumber, UserId " +
                        "," +
                        "UserName) " +
                        "Values(:accountId ,:communityId , :sortNumber ,:userId ,:userName  )";
                val query = con.createQuery(visibleUserSql);

                for (int i = 0; i < communityInfo.getCommunitiesVisibleUsers().size(); i++) {
                    val user = communityInfo.getCommunitiesVisibleUsers().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("communityId", communityInfo.getId());
                    query.addParameter("sortNumber", i);
                    query.addParameter("userId", user.getUser().getUserId());
                    query.addParameter("userName", user.getUser().getUserName());
                    query.addToBatch();
                }
                query.executeBatch();
            }

            //删除 项目
            var visibleProjectSqlDelete="delete from tbCommunityProjectRelated where accountId=:accountId and " +
                    "communityId=:id";
            con.createQuery( visibleProjectSqlDelete ).addParameter( "accountId",accountId )
                    .addParameter( "id",communityInfo.getId() )
                    .executeUpdate();
            //添加项目
            if (CollectionUtil.isNotEmpty( communityInfo.getCommunityProjectRelatedInfos() )) {
                val visibleProjectSql = "INSERT INTO tbCommunityProjectRelated(AccountId ,communityId , SortNumber, " +
                        "projectId,projectName,status)" +
                        "Values(:accountId ,:communityId , :sortNumber ,:projectId ,:projectName,:status  )";
                val query = con.createQuery(visibleProjectSql);

                for (int i = 0; i < communityInfo.getCommunityProjectRelatedInfos().size(); i++) {
                    val projectRelatedInfo = communityInfo.getCommunityProjectRelatedInfos().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("communityId", communityInfo.getId());
                    query.addParameter("sortNumber", i);
                    query.addParameter("projectId", projectRelatedInfo.getProject().getProjectId());
                    query.addParameter("projectName", projectRelatedInfo.getProject().getProjectName());
                    query.addParameter("status",projectRelatedInfo.getStatus());
                    query.addToBatch();
                }
                query.executeBatch();
            }

            //相关的干系人
            var visiblePeopleSqlDelete="delete from tbCommunityPeopleRelated where accountId=:accountId and " +
                    "communityId=:id";
            con.createQuery( visiblePeopleSqlDelete ).addParameter( "accountId",accountId )
                    .addParameter( "id",communityInfo.getId() )
                    .executeUpdate();
            //添加项目
            if (CollectionUtil.isNotEmpty( communityInfo.getCommunityPeopleRelatedInfos() )) {
                val visibleProjectSql = "INSERT INTO tbCommunityPeopleRelated(AccountId ,communityId , SortNumber, " +
                        "peopleId,peopleName,relatedPeopleRole)" +
                        "Values(:accountId ,:communityId , :sortNumber ,:peopleId ,:peopleName,:relatedPeopleRole  )";
                val query = con.createQuery(visibleProjectSql);

                for (int i = 0; i < communityInfo.getCommunityPeopleRelatedInfos().size(); i++) {
                    val peopleRelatedInfo = communityInfo.getCommunityPeopleRelatedInfos().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("communityId", communityInfo.getId());
                    query.addParameter("sortNumber", i);
                    query.addParameter("peopleId", peopleRelatedInfo.getRelatedPeople().getPeopleId());
                    query.addParameter("peopleName", peopleRelatedInfo.getRelatedPeople().getPeopleName());
                    query.addParameter("relatedPeopleRole",peopleRelatedInfo.getRelatedPeopleRole());
                    query.addToBatch();
                }
                query.executeBatch();
            }
             con.commit();
        }


    }
    public static int count(Map<String, Object> conditions, String accountId) {

        val sql = " select count(1) from (select c.id, c.accountId,name,createUserId 'createUser.userId',CreateUserName 'createUser" +
                ".userName',provinceName,cityName,countyName,address,lat,lng,c.remarks,c.createDate,c.lastUpdateDate," +
                "filePath," +
                "pictureUrl," +
                "chargerId " +
                "'charger.userId',t.userName 'charger.UserName' " +
                "from tbCommunity c   " +
                "left join tbuser t on t.userId=c.chargerId " +
                "where c.createUserId=:userId  and c.accountId=:accountId " +
                " and (c.name  like :name or :name is null) "+
                " and (c.address like :address or :address is null) ) s ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId) //租户id
                    .addParameter("userId", conditions.get( "userId" )) //用户id
                    .addParameter("name", conditions.get( "name" )!=null?"%"+conditions.get( "name" )+"%":null) //小区名称
                    .addParameter("address", conditions.get( "address" )!=null ?"%"+conditions.get( "address" )+"%":
                            null) //小区地址
                    .executeScalar(int.class);
        }
    }


    public static void insert(String accountId, CommunityInfo communityInfo) {
        //获取用户信息
        val spSql = "insert into tbCommunity(id,accountId,name,provinceName,cityName,countyName,address,lat,lng," +
                "remarks," +
                "pictureUrl,createUserId,createUserName,chargerId" +
                ")" +
                " values(:id,:accountId,:name,:provinceName,:cityName,:countyName,:address,:lat,:lng,:remarks," +
                ":pictureUrl,:createUserId,:createUserName,:chargerId)";

        try (val con = db.sql2o.open()) {
            con.createQuery( spSql ).bind(communityInfo)
                    .addParameter( "accountId", accountId )
                    .addParameter( "name",communityInfo.getName() )
                    .addParameter( "provinceName",communityInfo.getProvinceName() )
                    .addParameter( "cityName",communityInfo.getCityName() )
                    .addParameter( "countyName",communityInfo.getCountyName() )
                    .addParameter( "address",communityInfo.getAddress() )
                    .addParameter( "lat",communityInfo.getLat() )
                    .addParameter( "lng",communityInfo.getLng() )
                    .addParameter( "remarks",communityInfo.getRemarks() )
                    .addParameter( "pictureUrl",communityInfo.getPictureUrl() )
                    .addParameter( "createUserId",communityInfo.getCreateUser().getUserId() ) //设置创建用户信息
                    .addParameter( "createUserName",communityInfo.getCreateUser().getUserName() )
                    .addParameter( "chargerId",communityInfo.getCharger().getUserId() ) //设置内部负责人信息
                    .executeUpdate();

            String id=communityInfo.getId();
            // 保存 项目成员  对谁可见
            if (CollectionUtil.isNotEmpty( communityInfo.getCommunitiesVisibleUsers() )) {
                val visibleUserSql = "INSERT INTO tbCommunityVisibleUser(AccountId ,communityId , SortNumber, UserId " +
                        "," +
                        "UserName) " +
                        "Values(:accountId ,:communityId , :sortNumber ,:userId ,:userName  )";
                val query = con.createQuery(visibleUserSql);

                for (int i = 0; i < communityInfo.getCommunitiesVisibleUsers().size(); i++) {
                    val user = communityInfo.getCommunitiesVisibleUsers().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("communityId", id);
                    query.addParameter("sortNumber", i);
                    query.addParameter("userId", user.getUser().getUserId());
                    query.addParameter("userName", user.getUser().getUserName());
                    query.addToBatch();
                }

                query.executeBatch();
            }

            // 保存 项目信息
            if (CollectionUtil.isNotEmpty( communityInfo.getCommunityProjectRelatedInfos() )) {
                val visibleUserSql = "INSERT INTO tbCommunityProjectRelated(AccountId ,communityId , SortNumber, " +
                        "projectId, projectName,status)" +
                        "Values(:accountId ,:communityId , :sortNumber ,:projectId ,:projectName,:status  )";
                val query = con.createQuery(visibleUserSql);

                for (int i = 0; i < communityInfo.getCommunityProjectRelatedInfos().size(); i++) {
                    val projectRelatedInfo = communityInfo.getCommunityProjectRelatedInfos().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("communityId", id);
                    query.addParameter("sortNumber", i);
                    query.addParameter("projectId", projectRelatedInfo.getProject().getProjectId());
                    query.addParameter("projectName", projectRelatedInfo.getProject().getProjectName());
                    query.addParameter("status", projectRelatedInfo.getStatus());
                    query.addToBatch();
                }

                query.executeBatch();
            }

            // 保存 干系人
            if (CollectionUtil.isNotEmpty( communityInfo.getCommunityPeopleRelatedInfos() )) {
                val visibleSql = "INSERT INTO tbCommunityPeopleRelated(AccountId ,communityId , SortNumber, " +
                        "peopleId, peopleName,relatedPeopleRole)" +
                        "Values(:accountId ,:communityId , :sortNumber ,:peopleId ,:peopleName,:relatedPeopleRole  )";
                val query = con.createQuery(visibleSql);

                for (int i = 0; i < communityInfo.getCommunityPeopleRelatedInfos().size(); i++) {
                    val peopleRelatedInfo = communityInfo.getCommunityPeopleRelatedInfos().get(i);
                    query.addParameter("accountId", accountId);
                    query.addParameter("communityId", id);
                    query.addParameter("sortNumber", i);
                    query.addParameter("peopleId", peopleRelatedInfo.getRelatedPeople().getPeopleId());
                    query.addParameter("peopleName", peopleRelatedInfo.getRelatedPeople().getPeopleName());
                    query.addParameter("relatedPeopleRole", peopleRelatedInfo.getRelatedPeopleRole());
                    query.addToBatch();
                }

                query.executeBatch();
            }
//            con.commit();
        }
    }
}
