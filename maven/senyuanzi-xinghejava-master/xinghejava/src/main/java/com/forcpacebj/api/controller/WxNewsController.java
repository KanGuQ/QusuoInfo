package com.forcpacebj.api.controller;

import com.forcpacebj.api.entity.UploadResultInfo;
import com.forcpacebj.api.utils.FileUtil;
import com.forcpacebj.api.utils.HttpUtil;
import com.forcpacebj.api.utils.JSONUtil;
import com.forcpacebj.api.utils.StrUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import spark.Route;

import java.io.File;
import java.nio.file.Paths;

import static spark.Spark.halt;

@Slf4j
public class WxNewsController extends BaseController {

    public static Route gather = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val params = JSONUtil.toMap(req.body());

        try {
            String html = HttpUtil.get((String) params.get("wxNewsUrl"));
            if (StrUtil.isNotBlank(html)) {

                val document = Jsoup.parse(html);
                if (document != null) {
                    val content = document.select(".rich_media_content#js_content");
                    if (content != null) {

                        val imgs = content.select("img[data-src]");
                        for (val element : imgs) {
                            String src = element.attr("data-src");//获取到src的值
                            element.removeAttr("data-src");
                            element.removeAttr("src");
                            element.attr("src", src);

                            try {
                                String cacheFileName = "wx-img-" + src.hashCode() + ".jpg";
                                val saveDir = FileUploadController.getSaveDirectory(user.getAccountId()).toString();
                                val cacheFilePath = Paths.get(saveDir, cacheFileName).toString();
                                if (!FileUtil.fileIsExists(cacheFilePath)) {
                                    log.info("开始下载文件：" + src + " --> " + cacheFilePath);
                                    HttpUtil.downloadFile(src, saveDir, cacheFileName);
                                }

                                val localServerSrc = "http://" + req.host() + File.separator + user.getAccountId() + File.separator + cacheFileName;
                                element.attr("src", localServerSrc);
                            } catch (Exception ex) {
                                log.warn("下载微信图文图片异常：" + src + "    " + ex.getMessage());
                            }
                        }

                        return content.html();
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("获取图文异常", ex);
        }

        throw halt(400, "获取图文失败，请输入有效的网址重试");
    };

    public static Route gatherOSS = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val params = JSONUtil.toMap(req.body());

        try {
            String html = HttpUtil.get((String) params.get("wxNewsUrl"));
            if (StrUtil.isNotBlank(html)) {

                val document = Jsoup.parse(html);
                if (document != null) {
                    val content = document.select(".rich_media_content#js_content");
                    if (content != null) {
                        //img
                        val imgs = content.select("img[data-src]");
                        for (val element : imgs) {
                            String src = element.attr("data-src");//获取到src的值
                            element.removeAttr("data-src");
                            element.removeAttr("src");
                            element.attr("src", src);

                            try {
                                String cacheFileName = "wx-img-" + src.hashCode() + extractImgType(src);
                                log.info("开始下载文件：" + src + " --> " + user.getAccountId() + "/" + cacheFileName);
                                UploadResultInfo result = HttpUtil.downloadFileToOSS(src, user.getAccountId(), cacheFileName);

                                val localServerSrc = result.getUrl();
                                element.attr("src", localServerSrc);
                            } catch (Exception ex) {
                                log.warn("下载微信图文图片异常：" + src + "    " + ex.getMessage());
                            }
                        }
                        //background-image
                        Elements bg_element = document.getElementsByAttributeValueContaining("style", "url");
                        for (Element element : bg_element) {
                            String style = element.attr("style");//获取到url的值
                            element.removeAttr("style");
                            String bg_url = extractImgLink(style);

                            try {
                                String cacheFileName = "wx-img-" + bg_url.hashCode() + extractImgType(bg_url);
                                log.info("开始下载文件：" + bg_url + " --> " + user.getAccountId() + "/" + cacheFileName);
                                UploadResultInfo result = HttpUtil.downloadFileToOSS(bg_url, user.getAccountId(), cacheFileName);

                                val localServerSrc = result.getUrl();
                                element.attr("style", style.replace(bg_url, localServerSrc));
                            } catch (Exception ex) {
                                log.warn("下载微信图文图片异常：" + bg_url + "    " + ex.getMessage());
                            }
                        }

                        return content.html();
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("获取图文异常", ex);
        }

        throw halt(400, "获取图文失败，请输入有效的网址重试");
    };

    private static String extractImgLink(String string) {
        String start = "url(\"", end = "\");";
        return string.substring(string.indexOf(start) + start.length(), string.indexOf(end, string.indexOf(start)));
    }

    private static String extractImgType(String string) {
        String start = "wx_fmt=", end = "?";
        int startIndex = string.indexOf(start) + start.length();
        int endIndex = string.indexOf(end, string.indexOf(start));

        return "." + (endIndex == -1 ? string.substring(startIndex)
                : string.substring(startIndex, endIndex));
    }
}
