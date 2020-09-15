package com.forcpacebj.api.config;

import lombok.val;

import static spark.Spark.*;

public class CorsConfig {

    public static void enableCORS() {

        val origin = "*";
        val methods = "GET, POST, DELETE, PUT, OPTIONS";
        val headers = "Authorization,Content-Type,Cache-Control,Pragma";

        staticFiles.header("Access-Control-Allow-Origin", origin);
        staticFiles.header("Access-Control-Request-Method", methods);
        staticFiles.header("Access-Control-Allow-Headers", headers);

        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
        });
    }
}
