package com.erlitech.ejava.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 文件下载类
 *
 * @author 孙振强
 * @since 2017-08-15
 */
public class XFileDownloadUtil {

    private static final Logger LOGGER = XLoggerUtil.getLogger(XFileDownloadUtil.class.getName());

    /**
     * 获取文件类型（后缀）
     *
     * @param fileName 文件名
     * @return 文件后缀
     */
    public static String getFileType(String fileName) {
        String fileType;

        int index = fileName.lastIndexOf(".") + 1; //取得文件名中最后.的下标
        fileType = fileName.substring(index); //截取子字符串
        fileType = fileType.toLowerCase(); //转化为小写

        return fileType;
    }

    /**
     * 获取文件的MIME类型
     *
     * @param fileName 文件名
     * @return String 文件mime类型
     */
    public static String getFileMime(String fileName) {
        String fileType = getFileType(fileName);
        String mime;

        switch (fileType) {
            case "doc":
                mime = "application/msword";
                break;
            case "docx":
                mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                break;
            case "xlsx":
                mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                break;
            case "xls":
                mime = "application/vnd.ms-excel";
                break;
            case "pdf":
                mime = "application/pdf";
                break;
            case "ppt":
                mime = "appication/powerpoint";
                break;
            case "rtf":
                mime = "appication/rtf";
                break;
            case "z":
                mime = "appication/x-compress";
                break;
            case "gz":
                mime = "application/x-gzip";
                break;
            case "gtar":
                mime = "application/x-gtar";
                break;
            case "swf":
                mime = "application/x-shockwave-flash";
                break;
            case "tar":
                mime = "application/x-tar";
                break;
            case "zip":
                mime = "application/zip";
                break;
            case "rar":
                mime = "application/x-rar-compressed";
                break;
            case "mpeg":
            case "mp2":
                mime = "audio/mpeg";
                break;
            case "mid":
            case "midi":
            case "rmf":
                mime = "audio/x-aiff";
                break;
            case "rpm":
                mime = "audio/x-pn-realaudio-plugin";
                break;
            case "wav":
                mime = "audio/x-wav";
                break;
            case "gif":
                mime = "image/gif";
                break;
            case "jpeg":
            case "jpg":
            case "jpe":
                mime = "image/jpeg";
                break;
            case "png":
                mime = "image/png";
                break;
            case "txt":
                mime = "text/plain";
                break;
            case "xml":
                mime = "text/xml";
                break;
            case "json":
                mime = "text/json";
                break;
            case "exe":
                mime = "application/octet-stream";
                break;
            default:
                mime = "";
                break;
        }

        return mime;
    }

    /**
     * 文件下载
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     * @param response HttpServletResponse
     * @throws IOException 异常处理
     */
    public static void fileDownload(String filePath, String fileName, HttpServletResponse response) throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
            LOGGER.log(Level.SEVERE, "文件不存在：" + filePath);
        }

        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[1024];

        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setContentType(getFileMime(fileName));
        String encodedFileName = new String(fileName.getBytes("utf-8"), "ISO8859-1");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);

        OutputStream outputStream = response.getOutputStream();

        int len;

        while ((len = bufferedInputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }

        bufferedInputStream.close();
        outputStream.close();
    }

}
