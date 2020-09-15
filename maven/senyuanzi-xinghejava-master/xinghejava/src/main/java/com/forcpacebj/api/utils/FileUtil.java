/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */
package com.forcpacebj.api.utils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 文件操作类
 */
@Slf4j
public class FileUtil {
    /**
     * 文件是否存在
     *
     * @param filePath
     * @return
     */
    public static boolean fileIsExists(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * 读取文本文件内容
     *
     * @param filePathAndName 带有完整绝对路径的文件名
     * @param encoding        文本文件打开的编码方式
     * @return 返回文本文件的内容
     */
    public static String readTxt(String filePathAndName, String encoding) throws IOException {
        encoding = encoding.trim();
        val str = new StringBuffer("");
        String st = "";
        try {
            val fs = new FileInputStream(filePathAndName);
            InputStreamReader isr;
            if (encoding.equals("")) {
                isr = new InputStreamReader(fs);
            } else {
                isr = new InputStreamReader(fs, encoding);
            }
            val br = new BufferedReader(isr);
            try {
                String data = "";
                while ((data = br.readLine()) != null) {
                    str.append(data + " ");
                }
            } catch (Exception e) {
                str.append(e.toString());
            }
            st = str.toString();
        } catch (IOException es) {
            st = "";
        }
        return st;
    }

    /**
     * 新建目录
     *
     * @param folderPath 目录
     * @return 返回目录创建后的路径
     */
    public static String createFolder(String folderPath) throws Exception {
        String txt = folderPath;
        try {
            val myFilePath = new File(txt);
            txt = folderPath;
            if (!myFilePath.exists()) {
                myFilePath.mkdir();
            }
        } catch (Exception e) {
            throw new Exception("创建目录失败", e);
        }
        return txt;
    }

    /**
     * 多级目录创建
     *
     * @param folderPath 准备要在本级目录下创建新目录的目录路径 例如 c:myf
     * @param paths      无限级目录参数，各级目录以单数线区分 例如 a|b|c
     * @return 返回创建文件后的路径 例如 c:myfac
     */
    public static String createFolders(String folderPath, String paths) throws Exception {
        String txts = folderPath;
        try {
            String txt;
            txts = folderPath;
            val st = new StringTokenizer(paths, "|");
            while (st.hasMoreTokens()) {
                txt = st.nextToken().trim();
                if (txts.lastIndexOf("/") != -1) {
                    txts = createFolder(txts + txt);
                } else {
                    txts = createFolder(txts + txt + "/");
                }
            }
        } catch (Exception e) {
            throw new Exception("创建目录失败", e);
        }
        return txts;
    }

    /**
     * 新建文件
     *
     * @param filePathAndName 文本文件完整绝对路径及文件名
     * @param fileContent     文本文件内容
     * @return
     */
    public static void createFile(String filePathAndName, String fileContent) throws Exception {
        try {
            String filePath = filePathAndName;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            if (!myFilePath.exists()) {
                myFilePath.createNewFile();
            }
            val resultFile = new FileWriter(myFilePath);
            val myFile = new PrintWriter(resultFile);
            String strContent = fileContent;
            myFile.println(strContent);
            myFile.close();
            resultFile.close();
        } catch (Exception e) {
            throw new Exception("创建文件失败", e);
        }
    }

    /**
     * 有编码方式的文件创建
     *
     * @param filePathAndName 文本文件完整绝对路径及文件名
     * @param fileContent     文本文件内容
     * @param encoding        编码方式 例如 GBK 或者 UTF-8
     * @return
     */
    public static void createFile(String filePathAndName, String fileContent, String encoding) {
        try {
            String filePath = filePathAndName;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            if (!myFilePath.exists()) {
                myFilePath.createNewFile();
            }
            val myFile = new PrintWriter(myFilePath, encoding);
            String strContent = fileContent;
            myFile.println(strContent);
            myFile.close();
        } catch (Exception ex) {
            log.error("创建文件失败", ex);
        }
    }

    /**
     * 删除文件
     *
     * @param filePathAndName 文本文件完整绝对路径及文件名
     * @return Boolean 成功删除返回true遭遇异常返回false
     */
    public static boolean delFile(String filePathAndName) {
        boolean bea = false;
        try {
            String filePath = filePathAndName;
            val myDelFile = new File(filePath);
            if (myDelFile.exists()) {
                myDelFile.delete();
                bea = true;
            } else {
                bea = false;
            }
        } catch (Exception ex) {
            log.error("删除文件失败", ex);
        }
        return bea;
    }

    /**
     * 删除文件夹
     *
     * @param folderPath 文件夹完整绝对路径
     * @return
     */
    public static void delFolder(String folderPath) throws Exception {
        try {
            delAllFile(folderPath); // 删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            val myFilePath = new File(filePath);
            myFilePath.delete(); // 删除空文件夹
        } catch (Exception e) {
            throw new Exception("删除文件夹失败", e);
        }
    }

    /**
     * 删除指定文件夹下所有文件
     *
     * @param path 文件夹完整绝对路径
     * @return
     */
    public static boolean delAllFile(String path) throws Exception {
        boolean bea = false;
        val file = new File(path);
        if (!file.exists()) {
            return bea;
        }
        if (!file.isDirectory()) {
            return bea;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);// 再删除空文件夹
                bea = true;
            }
        }
        return bea;
    }

    /**
     * 复制单个文件
     *
     * @param oldPathFile 准备复制的文件源
     * @param newPathFile 拷贝到新绝对路径带文件名
     * @return
     */
    public static void copyFile(String oldPathFile, String newPathFile) throws Exception {
        try {
            int bytesum = 0;
            int byteread = 0;
            val oldfile = new File(oldPathFile);
            if (oldfile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(oldPathFile); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPathFile);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            throw new Exception("复制单个文件操作出错", e);
        }
    }

    /**
     * 复制整个文件夹的内容
     *
     * @param oldPath 准备拷贝的目录
     * @param newPath 指定绝对路径的新目录
     * @return
     */
    public static void copyFolder(String oldPath, String newPath) throws Exception {
        try {
            new File(newPath).mkdirs(); // 如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }
                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {// 如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            throw new Exception("复制整个文件夹内容操作出错", e);
        }
    }

    /**
     * 移动文件
     *
     * @param oldPath
     * @param newPath
     * @return
     */
    public static void moveFile(String oldPath, String newPath) throws Exception {
        copyFile(oldPath, newPath);
        delFile(oldPath);
    }

    /**
     * 移动目录
     *
     * @param oldPath
     * @param newPath
     * @return
     */
    public static void moveFolder(String oldPath, String newPath) throws Exception {
        copyFolder(oldPath, newPath);
        delFolder(oldPath);
    }

    /**
     * 获取目录下所有文件(指定扩展名)
     *
     * @param dirPath
     * @param extendName
     * @return
     * @throws Exception
     */
    public static List<String> getFiles(String dirPath, String extendName) {
        val files = new ArrayList<String>();
        val f = new File(dirPath);
        if (!f.exists()) {
            return null;
        } else {
            File fa[] = f.listFiles();
            for (int i = 0; i < fa.length; i++) {
                File fs = fa[i];
                if (!fs.isDirectory() && fs.getName().endsWith(extendName)) {
                    files.add(fs.getAbsolutePath());
                }
            }
        }
        return files;
    }

    /**
     * 根据完整文件路径获取文件名称
     *
     * @param fullPath
     * @return
     */
    public static String getFileNameFromPath(String fullPath) {
        return new File(fullPath).getName();
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName
     * @return
     */
    public static String getFileExtension(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase().trim();
        } else {
            return "";
        }
    }

    /**
     * 获取系统临时文件夹
     *
     * @return
     */
    public static String getSystemTempFolder() {
        return System.getProperty("java.io.tmpdir");
    }
}
