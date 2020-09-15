package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.CommunityPeopleRelatedInfo;
import com.forcpacebj.api.entity.CommunityProjectRelatedInfo;
import com.forcpacebj.api.entity.CommunityVisibleUser;
import lombok.val;
import lombok.var;

import java.util.List;

/**
 * {@link CommunityPeopleRelatedBusiness}
 * Author: ACL
 * Date:2020/03/08
 * Description:
 * Created by ACL on 2020/03/08.
 */
public class CommunityPeopleRelatedBusiness {

    public static List<CommunityPeopleRelatedInfo> findByCommunityIdAndAccountId(String communityId,
                                                                                 String accountId) {
        var sql="select  c.peopleId 'relatedPeople.peopleId' ,p.peopleName 'relatedPeople.peopleName', " +
                "relatedPeopleRole from " +
                "tbCommunityPeopleRelated c left join tbPeople p on  p.peopleId=c.peopleId " +
                "where  communityId=:communityId and  c.accountId=:accountId  order by sortNumber ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("communityId", communityId)
                    .executeAndFetch( CommunityPeopleRelatedInfo.class);
        }
    }
}
