package com.forcpacebj.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.forcpacebj.api.business.UserBusiness;
import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.entity.DepartmentInfo;
import com.forcpacebj.api.entity.TokenInfo;
import com.forcpacebj.api.entity.UserInfo;
import com.forcpacebj.api.entity.UserRoleInfo;
import com.forcpacebj.api.utils.DateUtil;
import com.forcpacebj.api.utils.JSONUtil;
import com.forcpacebj.api.utils.JedisUtil;
import com.forcpacebj.api.utils.StrUtil;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j;
import lombok.val;
import lombok.var;
import redis.clients.jedis.Jedis;
import spark.Request;


import static spark.Spark.halt;

@Log4j
public class BaseController {

    protected static String toJson(Object obj) {
        return obj == null ? "" : JSONUtil.toJson(obj);
    }

    protected static UserInfo ensureUserIsLoggedIn(Request request) {

        val headerAuth = request.headers("Authorization");

        if (StrUtil.isBlank(headerAuth)) {
            throw halt(401, "登录无效或已过期，请重新登录");
        }
        val user = decodeToken(headerAuth);

        UserBusiness.lastAccessed(user);
        return user;
    }

    private static final String SECRET = "xinghe_data";

    static final String Default_Pwd = "123456";

    protected static UserInfo decodeToken(String toke) {
        try (Jedis jedis = JedisUtil.getJedis()) {
            val body = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(toke).getBody();
            if (jedis.exists(body.getSubject())) {
                if (!toke.equals(jedis.get(body.getSubject()))) throw new JwtException("账号已在别处登录，您被迫下线...");
            } else if (!(Boolean) body.get("multi")) throw new Exception();
            val user = new UserInfo();
            user.setUserId(body.getSubject());
            user.setUserName((String) body.get("userName"));
            user.setPhoneNumber((String) body.get("phoneNumber"));
            user.setIsAdmin((Boolean) body.get("isAdmin"));
            user.setEmail((String) body.get("email"));
            user.setAccountId((String) body.get("accountId"));
            user.setAccountName((String) body.get("accountName"));
            if (body.get("departmentId") != "")
                user.setDepartmentId(Integer.valueOf((String) body.get("departmentId")));
            var roleJson = JSONObject.parseObject((String) body.get("role"));
            user.setUserRole(new UserRoleInfo((Integer) roleJson.get("roleId"), (String) roleJson.get("roleName"), roleJson.get("power")));
            var departmentJson = JSONObject.parseObject((String) body.get("department"));
            user.setDepartment(new DepartmentInfo((Integer) departmentJson.get("id"), (String) departmentJson.get("name"), departmentJson.get("power")));
            jedis.expire(user.getUserId(), 10800);
            return user;
        } catch (Exception ex) {
            throw halt(401, "登录无效或已过期，请重新登录");
        }
    }

    static void generatorToken(UserInfo user) {
        try (Jedis jedis = JedisUtil.getJedis()) {
            val claim = Jwts.claims().setSubject(user.getUserId());
            claim.put("userName", user.getUserName());
            claim.put("email", user.getEmail());
            claim.put("phoneNumber", user.getPhoneNumber());
            claim.put("isAdmin", user.getIsAdmin());
            claim.put("accountId", user.getAccountId());
            claim.put("accountName", user.getAccountName());
            claim.put("roleId", user.getUserRole().getRoleId());
            claim.put("power", user.getUserRole().getPower());
            claim.put("roleName", user.getUserRole().getRoleName());
            claim.put("multi", user.getMultipleLogin());
            claim.put("department", toJson(user.getDepartment()));
            claim.put("role", toJson(user.getUserRole()));
            //部门ID放入Token中
            claim.put("departmentId", toJson(user.getDepartmentId()));

            val expiration = DateUtil.addDay(DateUtil.now(), 3);
            claim.setExpiration(expiration);
            val token = Jwts.builder().setClaims(claim).signWith(SignatureAlgorithm.HS512, SECRET).compact();
            user.setToken(new TokenInfo(token, DateUtil.now()));

            if (user.getMultipleLogin()) return;
            jedis.set(user.getUserId(), token);
            jedis.expire(user.getUserId(), 10800);
        } catch (Exception ex) {
            throw halt(401, "登录无效或已过期，请重新登录");
        }
    }

    static Boolean checkPower(UserInfo user) {
        return user.getIsAdmin() || user.getUserRole().getPower().contains(StaticParam.SUPER);
    }
}
