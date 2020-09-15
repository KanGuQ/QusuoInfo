package com.forcpacebj.api.core;

import lombok.Data;
import lombok.val;

/**
 *
 * @param status 0成功 1参数错误
 */
@Data
public class ResponseWrapper<T> {
    private int status = 0;
    private T data = null;
    private String message = "";

    public static ResponseWrapper ok(Object data) {
        val res = new ResponseWrapper();
        res.data = data;
        return res;
    }
    public static ResponseWrapper error(String message) {
        val res = new ResponseWrapper();
        res.message = message;
        res.status = 1;
        return res;
    }
    public static ResponseWrapper page(int count,Object data) {
        val res = new ResponseWrapper<Page>();
        res.data = new Page(count,data);
        return res;
    }


    @Data
    private static class Page{
        Page(int count, Object data) {
            this.count = count;
            this.list = data;
        }

        private int count = 0;
        private Object list = null;
    }
}
