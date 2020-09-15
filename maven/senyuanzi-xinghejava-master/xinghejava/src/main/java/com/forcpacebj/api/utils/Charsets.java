/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */
package com.forcpacebj.api.utils;

import java.io.File;
import java.nio.charset.Charset;

/**
 * 字符集工具类
 */
public class Charsets {    // 字符集GBK
    public static final Charset GBK = Charset.forName("GBK");    // 字符集ISO-8859-1
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");    // 字符集utf-8
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final char C_BACKSLASH = '\\';

    /**
     * 系统字符集编码，如果是Windows，则默认为GBK编码
     *
     * @return 系统字符集编码
     * @since 3.1.2
     */
    public static String systemCharsetName() {
        return systemCharset().name();
    }

    /**
     * 系统字符集编码，如果是Windows，则默认为GBK编码
     *
     * @return 系统字符集编码
     * @since 3.1.2
     */
    public static Charset systemCharset() {
        return isWindows() ? GBK : defaultCharset();
    }

    /**
     * 系统默认字符集编码
     *
     * @return 系统字符集编码
     */
    public static String defaultCharsetName() {
        return defaultCharset().name();
    }

    /**
     * 系统默认字符集编码
     *
     * @return 系统字符集编码
     */
    public static Charset defaultCharset() {
        return Charset.defaultCharset();
    }

    /**
     * 是否为Windows环境
     *
     * @return 是否为Windows环境
     * @since 3.0.9
     */
    public static boolean isWindows() {
        return C_BACKSLASH == File.separatorChar;
    }
}
