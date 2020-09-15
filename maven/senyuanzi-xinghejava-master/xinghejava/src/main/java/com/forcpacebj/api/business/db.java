package com.forcpacebj.api.business;

import com.forcpacebj.api.Program;
import lombok.val;
import org.sql2o.Sql2o;

public class db {

    public static Sql2o sql2o;

    static {

        val url = "jdbc:mysql://132.232.1.234:3306/{db}?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC";

        //sql2o = new Sql2o(url.replace("{db}", Program.mysqlDB), "root", "159357zx/C");
        sql2o = new Sql2o(url.replace("{db}", Program.mysqlDB), "root", "zy20200331");

    }
}
