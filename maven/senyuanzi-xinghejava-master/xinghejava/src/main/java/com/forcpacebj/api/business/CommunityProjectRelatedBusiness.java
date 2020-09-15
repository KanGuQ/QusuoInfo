package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.CommunityProjectRelatedInfo;
import com.forcpacebj.api.entity.CommunityVisibleUser;
import lombok.val;
import lombok.var;

import java.util.List;

/**
 * {@link CommunityProjectRelatedBusiness}
 * Author: ACL
 * Date:2020/03/08
 * Description:
 * Created by ACL on 2020/03/08.
 */
public class CommunityProjectRelatedBusiness {

    public static List<CommunityProjectRelatedInfo> findByCommunityIdAndAccountId(String communityId,
                                                                                   String accountId) {
        var sql = " select c.projectId 'project.projectId',p.projectName 'project.projectName',status from " +
                "tbCommunityProjectRelated c left join tbProject p on c.projectId=p.projectId" +
                " where communityId=:communityId and c.accountId=:accountId";
        try (val con = db.sql2o.open()) {
            return con.createQuery( sql )
                    .addParameter( "accountId", accountId )
                    .addParameter( "communityId", communityId )
                    .executeAndFetch( CommunityProjectRelatedInfo.class );
        }
    }
}
