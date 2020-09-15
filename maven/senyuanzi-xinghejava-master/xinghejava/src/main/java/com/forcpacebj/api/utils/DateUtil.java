/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */

package com.forcpacebj.api.utils;

import lombok.val;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 日期处理类
 */
public class DateUtil {

    /**
     * 判断是否为日期格式字符串
     *
     * @param str
     * @return
     */
    public static boolean isDate(String str) {

        try {
            val dt = DateTime.parse(str);
            return dt != null;
        } catch (Exception ex) {
            return false;
        }
    }

    /***
     * 取当前年份（格式为yyyy）
     */
    public static String getCurrentYear() {

        return new DateTime().toString("yyyy");
    }

    /***
     * 取当前年月（格式为yyyy-MM）
     */
    public static String getCurrentYearMonth() {

        return new DateTime().toString("yyyy-MM");
    }

    /***
     * 取当前年月日（格式为yyyy-MM-dd）
     */
    public static String getCurrentDate() {

        return new DateTime().toString("yyyy-MM-dd");
    }

    /***
     * 取当前年月日（格式为yyyy-MM-dd HH:mm:ss）
     */
    public static String getCurrentDateTime() {

        return new DateTime().toString("yyyy-MM-dd HH:mm:ss");
    }

    /***
     * 取当前时间戳
     */
    public static long getCurrentTimeMillis() {

        return new DateTime().getMillis();
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static Date now() {

        return new Date();
    }

    /**
     * 字符串转日期格式
     * 字符串格式：2015-12-31T16:00:00.000Z
     *
     * @param dateString
     * @return
     */
    public static Date convert(String dateString) {

        try {
            return DateTime.parse(dateString).toDate();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 时间戳转时间 yyyy-MM-dd HH:mm:ss
     *
     * @param mill
     * @return
     */
    public static String convert(long mill, String format) {

        try {
            return new DateTime(mill).toString(format);

        } catch (Exception ex) {
            return null;
        }
    }


    public static Date convert(long mill) {
        try {
            return new DateTime(mill).toDate();
        } catch (Exception ex) {
            return null;
        }
    }


    /**
     * String 时间转Date
     *
     * @param dtStr
     * @param format
     * @return
     */
    public static Date convert(String dtStr, String format) {

        if (StrUtil.isNotBlank(dtStr)) {
            try {
                return DateTimeFormat.forPattern(format).parseDateTime(dtStr).toDate();
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }


    /**
     * Date转Millis
     *
     * @return
     */
    public static long getMillis(Date dt) {

        return new DateTime(dt).getMillis();
    }


    /**
     * Date转Millis
     *
     * @return
     */
    public static long getMillis(String dt, String format) {

        return new DateTime(convert(dt, format)).getMillis();
    }

    /**
     * String 时间转Date
     *
     * @param date
     * @param format
     * @return
     */
    public static String convert(Date date, String format) {

        if (date != null) {
            try {
                return new DateTime(date).toString(format);
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * 时间戳转年月
     *
     * @param millis
     * @return 201607
     */
    public static String convertMillis2YM(long millis) {

        return new DateTime(millis).toString("yyyyMM");
    }

    /**
     * 时间运算
     *
     * @param date
     * @param minutes
     * @return
     */
    public static Date addMinutes(Date date, int minutes) {

        return new DateTime(date).plusMinutes(minutes).toDate();
    }

    /**
     * 时间运算
     *
     * @param date
     * @param seconds
     * @return
     */
    public static Date addSeconds(Date date, int seconds) {

        return new DateTime(date).plusSeconds(seconds).toDate();
    }

    /**
     * 是否相同年份
     *
     * @param date1
     * @param date2
     * @return
     */
    public static Boolean isSameYear(Date date1, Date date2) {

        return new DateTime(date1).getYear() == new DateTime(date2).getYear();
    }

    /**
     * 是否相同月份
     *
     * @param date1
     * @param date2
     * @return
     */
    public static Boolean isSameMonthOfYear(Date date1, Date date2) {

        return new DateTime(date1).getMonthOfYear() == new DateTime(date2).getMonthOfYear();
    }

    /**
     * 是否相同日期
     *
     * @param date1
     * @param date2
     * @return
     */
    public static Boolean isSameDayOfMonth(Date date1, Date date2) {

        val dt1 = new DateTime(date1);
        val dt2 = new DateTime(date2);

        return dt1.getMonthOfYear() == dt2.getMonthOfYear() && dt1.getDayOfMonth() == dt2.getDayOfMonth();
    }

    /**
     * 是否相同时间
     *
     * @param date1
     * @param date2
     * @return
     */
    public static Boolean isSameDateTime(Date date1, Date date2) {

        val dt1 = new DateTime(date1);
        val dt2 = new DateTime(date2);

        return dt1.toString("yyyy-MM-dd HH:mm:ss").equals(dt2.toString("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 相距年数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int yearsApart(Date date1, Date date2) {

        return new Period(date1.getTime(), date2.getTime(), PeriodType.years()).getYears();
    }

    /**
     * 相距月数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int monthsApart(Date date1, Date date2) {

        return new Period(date1.getTime(), date2.getTime(), PeriodType.months()).getMonths();
    }

    /**
     * 相距天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int daysApart(Date date1, Date date2) {

        return new Period(date1.getTime(), date2.getTime(), PeriodType.days()).getDays();
    }

    /**
     * 相距 小时数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int hoursApart(Date date1, Date date2) {

        return new Period(date1.getTime(), date2.getTime(), PeriodType.hours()).getHours();
    }

    /**
     * 相距秒数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int secondsApart(Date date1, Date date2) {

        return new Period(date1.getTime(), date2.getTime(), PeriodType.seconds()).getSeconds();
    }


    /**
     * 年计算
     *
     * @param date
     * @param year
     * @return
     */
    public static Date addYear(Date date, int year) {

        return new DateTime(date).plusYears(year).toDate();
    }


    /**
     * 月份计算
     *
     * @param date
     * @param month
     * @return
     */
    public static Date addMonth(Date date, int month) {

        return new DateTime(date).plusMonths(month).toDate();
    }


    /**
     * 日计算
     *
     * @param date
     * @param day
     * @return
     */
    public static Date addDay(Date date, int day) {

        return new DateTime(date).plusDays(day).toDate();
    }


    /**
     * 获取年
     *
     * @param date
     * @return
     */
    public static int getYear(Date date) {

        return new DateTime(date).getYear();
    }

    /**
     * 获取星期几
     *
     * @param date
     * @return
     */
    public static int getDayOfWeek(Date date) {

        return new DateTime(date).getDayOfWeek();
    }

    /**
     * 获取月
     *
     * @param date
     * @return
     */
    public static int getMonthOfYear(Date date) {

        return new DateTime(date).getMonthOfYear();
    }

    /**
     * 获取日
     *
     * @param date
     * @return
     */
    public static int getDayOfMonth(Date date) {

        return new DateTime(date).getDayOfMonth();
    }

    /**
     * 判断日期是否在指定开始,结束日期之间
     *
     * @param date
     * @param startDate
     * @param endDate
     * @return
     */
    public static Boolean isBetween(Date date, Date startDate, Date endDate) {

        val compareDate = new DateTime(date);
        return compareDate.isAfter(new DateTime(startDate)) && compareDate.isBefore(new DateTime(endDate));
    }

    /**
     * 获取生肖
     *
     * @param year
     * @return
     */
    public static String getAnimalYearName(int year) {

        int jiaziYear = 1804;
        if (year < jiaziYear) {
            jiaziYear = jiaziYear - (60 + 60 * ((jiaziYear - year) / 60));
        }
        int yearSpan = year - jiaziYear;

        String[] animalYear = new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
        return animalYear[yearSpan % 12];
    }


    /**
     * 获取时间范围内的所有年份
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getYearList(Date startTime, Date endTime) {

        val list = new ArrayList<String>();
        int startYear = new DateTime(startTime).getYear();
        int endYear = new DateTime(endTime).getYear();

        while (startYear <= endYear) {
            list.add(Integer.toString(startYear));
            startYear++;
        }

        return list;
    }

    /**
     * 获取时间范围内的所有年份
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getMonthList(Date startTime, Date endTime) {

        val list = new ArrayList<String>();
        DateTime dtStart = new DateTime(startTime);
        while (dtStart.isBefore(new DateTime(endTime))) {
            list.add(dtStart.toString("yyyyMM"));
            dtStart = dtStart.plusMonths(1);
        }

        return list;
    }

    /**
     * 获取时间范围内的所有年份
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getDayList(Date startTime, Date endTime) {

        val list = new ArrayList<String>();
        DateTime dtStart = new DateTime(startTime);
        while (dtStart.isBefore(new DateTime(endTime))) {
            list.add(dtStart.toString("yyyyMMdd"));
            dtStart = dtStart.plusDays(1);
        }

        return list;
    }

}
