package com.forcpacebj.api.controller;

import com.forcpacebj.api.Program;
import com.forcpacebj.api.entity.UploadResultInfo;
import com.forcpacebj.api.utils.AliyunOSSClientUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.coobird.thumbnailator.Thumbnails;
import spark.Route;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static spark.Spark.halt;

@Slf4j
public class FileUploadController extends BaseController {

    public static Route upload = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

        Part uploadFile = request.raw().getPart("file");
        val submittedFileName = uploadFile.getSubmittedFileName();
        val suffix = submittedFileName.substring(submittedFileName.lastIndexOf("."));
        val saveDir = getSaveDirectory(user.getAccountId());

        Path tempFile = Files.createTempFile(saveDir, "", suffix);

        try (val input = uploadFile.getInputStream()) {
            if (isImage(suffix) && uploadFile.getSize() > 500 * 1000) {  //如果是图片，而且文件大小超过500K ，则压缩0.4f
                Thumbnails.of(input).scale(1f).outputQuality(0.4f).toFile(tempFile.toAbsolutePath().toString());
            } else {
                Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }

//        val url = "http://" + request.host() + File.separator + user.getAccountId() + File.separator + tempFile.getFileName();
        val url = Program.host + "/" + user.getAccountId() + "/" + tempFile.getFileName();
        log.info("Uploaded file " + submittedFileName + " saved as " + tempFile.toAbsolutePath() + " ,visit by " + url);

        val result = new UploadResultInfo(true, submittedFileName, url, null);

        return toJson(result);
    };

    public static Route uploadOSS = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        Part uploadFile = request.raw().getPart("file");
        try {
            return toJson(AliyunOSSClientUtil.uploadOSS(uploadFile, user.getAccountId()));
        } catch (Exception ex){
            throw halt(400,ex.getMessage());
        }
    };

    public static Path getSaveDirectory(String accountId) {

        val folder = Program.fileUploadDirectory + File.separator + accountId;
        File uploadDir = new File(folder);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        return uploadDir.toPath();
    }

    private static boolean isImage(String suffix) {
        val extend = suffix.toLowerCase();
        return extend.equals(".jpg") || extend.equals(".png") || extend.equals(".gif") || extend.equals(".bmp") || extend.equals(".jpeg");
    }
}
