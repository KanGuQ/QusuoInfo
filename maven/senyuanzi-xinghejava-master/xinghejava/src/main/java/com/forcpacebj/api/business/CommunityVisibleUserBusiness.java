package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.CommunityInfo;
import com.forcpacebj.api.entity.CommunityVisibleUser;
import lombok.val;
import lombok.var;

import java.util.List;

/**
 * {@link CommunityVisibleUserBusiness}
 * Author: ACL
 * Date:2020/03/05
 * Description:
 * Created by ACL on 2020/03/05.
 */
public class CommunityVisibleUserBusiness {

    public static List<CommunityVisibleUser> findByCommunityIdAndAccountId(String communityId, String accountId){
        var sql="select  c.userId 'user.userId' ,u.userName 'user.userName' from tbCommunityVisibleUser c left join " +
                "tbUser u on " +
                "u.userId=c.userId " +
                "where  communityId=:communityId and " +
                "c.accountId=:accountId " +
                "order by sortNumber ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("communityId", communityId)
                    .executeAndFetch(CommunityVisibleUser.class);
        }
    }
}
