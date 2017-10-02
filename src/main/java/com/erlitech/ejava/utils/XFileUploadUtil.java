package com.erlitech.ejava.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.crypto.hash.Md5Hash;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 文件上传类
 *
 * @author 孙振强
 * @since 2017-09-30
 */
public class XFileUploadUtil {

    private static final int BUFFER_SIZE = 100 * 1024;
    private static final SimpleDateFormat sdf_year = new SimpleDateFormat("yyyy");
    private static final SimpleDateFormat sdf_day = new SimpleDateFormat("MMdd");
    private static Logger logger = XLoggerUtil.getLogger(XFileUploadUtil.class.getName());

    public static JSONObject upload(JSONObject joInData, HttpServletRequest request) throws FileUploadException {
        JSONObject joOutData = new JSONObject();
        Part filePart = getFilePart(request);
        InputStream inputStream;

        if (null == filePart) {
            joOutData.put("error", 1709301408);
            joOutData.put("errorMessage", "无文件上传信息");
            logger.warning(joOutData.toString());
            return joOutData;
        }

        try {
            inputStream = filePart.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            joOutData.put("error", 1709301409);
            joOutData.put("errorMessage", "文件数据错误");
            logger.warning(joOutData.toString());
            return joOutData;
        }

        XFile xFile = getXFile(request);
        // 获取根路径
        xFile.rootPath = joInData.getString("rootPath");

        // 创建保存文件夹
        xFile = makeFileDir(xFile);

        // 保存上传文件
        xFile = saveFile(xFile, inputStream);

        // 判断是否上传完成
        if (xFile.chunk == xFile.chunks - 1) {
            joOutData.put("message", "文件 " + xFile.name + " 上传完成");
            joOutData.put("path", xFile.path);

            if ("1".equals(joInData.getString("saveToDb"))) {
                joOutData.put("file", saveToDb(joInData, xFile));
            }
        } else {
            joOutData.put("message", "文件 " + xFile.name + " 第" + (xFile.chunk + 1) + "块上传完成，共：" + xFile.chunks);
        }

        joOutData.put("error", 0);
        logger.info(joOutData.toString());
        return joOutData;
    }

    // 从request中获取filePart
    private static Part getFilePart(HttpServletRequest request) {
        Part filePart = null;

        try {
            Collection<Part> parts = request.getParts();

            if (null != parts) {
                for (Part part : parts) {
                    String partName = part.getName();

                    if ("file".equals(partName)) {
                        filePart = part;
                        break;
                    }
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServletException e) {
            e.printStackTrace();
        }

        return filePart;
    }

    // 从request中获取filePart
    public static JSONObject saveToDb(JSONObject joInData, XFile xFile) {
        JSONObject joFile = new JSONObject();

        joFile.put("xtb", joInData.getString("xtb"));
        joFile.put("xid", joInData.getString("xid"));
        joFile.put("name", xFile.name);
        joFile.put("type", xFile.type);
        joFile.put("path", xFile.path);
        joFile.put("size", xFile.size);
        joFile.put("dir", xFile.dir);
        joFile.put("uuidName", xFile.uuidName);
        joFile.put("useTime", joInData.getInteger("useTime"));

        if (null == joFile.get("xtb")) {
            joFile.put("xtb", "");
        }

        if (null == joFile.get("xid")) {
            joFile.put("xid", "");
        }

        if (null == joFile.get("useTime")) {
            joFile.put("useTime", 0);
        }

        String table = joInData.getString("fileTableName");

        if (null == table || "".equals(table)) {
            table = "sys_file";
        }

        XqlUtil xql = new XqlUtil();

        xql.setTable(table);
        xql.setValue(joFile);

        String sql = xql.getInsertSql();
        String sql2 = xql.getInsertSql();

        String id = XdbUtil.insert(xql);

        joFile.put("id", id);

        return joFile;
    }

    // 根据request信息获取xFile
    private static XFile getXFile(HttpServletRequest request) {
        XFile xFile = new XFile();

        xFile.name = request.getParameter("name");
        xFile.type = FilenameUtils.getExtension(xFile.name);
        xFile.chunk = 0;
        xFile.chunks = 0;

        // 根据User-Agent生成上传临时文件名
        String userAgent = request.getHeaders("User-Agent").nextElement();
        xFile.uploadingName = new Md5Hash(userAgent) + xFile.name;

        if (null != request.getParameter("chunk") && !request.getParameter("chunk").equals("")) {
            xFile.chunk = Integer.valueOf(request.getParameter("chunk"));
        }

        if (null != request.getParameter("chunks") && !request.getParameter("chunks").equals("")) {
            xFile.chunks = Integer.valueOf(request.getParameter("chunks"));
        }

        return xFile;
    }

    // 保存上传的文件
    private static XFile saveFile(XFile xFile, InputStream inputStream) {
        // 定义上传文件的临时路径
        xFile.uploadingPath = xFile.dirPath + File.separator + xFile.uploadingName;

        //目标文件
        File file = new File(xFile.uploadingPath);

        //文件已存在删除旧文件（上传了同名的文件）
        if (xFile.chunk == 0 && file.exists()) {
            file.delete();
            file = new File(xFile.uploadingPath);
        }

        //合成文件
        appendFile(inputStream, file);

        if (xFile.chunk == xFile.chunks - 1) {
            // 形成最终文件
            xFile.uuidName = UUID.randomUUID().toString() + "." + xFile.type;
            xFile.uuidPath = xFile.dirPath + File.separator + xFile.uuidName;
            xFile.path = xFile.dir + File.separator + xFile.uuidName;

            File finalFile = new File(xFile.uuidPath);
            file.renameTo(finalFile);

            xFile.size = finalFile.length();
        }

        return xFile;
    }

    // 创建目标文件夹
    public static XFile makeFileDir(XFile xFile) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormatYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat("MMdd");
        String year = simpleDateFormatYear.format(date);
        String day = simpleDateFormatDay.format(date);

        xFile.dir = File.separator + year + File.separator + day;
        xFile.dirPath = xFile.rootPath + xFile.dir;

        File dir = new File(xFile.dirPath);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return xFile;
    }

    // 给file追加内容。Plupload 配置了chunk的时候，新上传的文件append到文件末尾
    public static File appendFile(InputStream inputStream, File file) {
        OutputStream outputStream = null;
        int buffer_size = 1024 * 100;

        try {
            if (file.exists()) {
                outputStream = new BufferedOutputStream(new FileOutputStream(file, true), buffer_size);
            } else {
                outputStream = new BufferedOutputStream(new FileOutputStream(file), buffer_size);
            }

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, buffer_size);

            int len = 0;
            byte[] buffer = new byte[buffer_size];

            while ((len = bufferedInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            bufferedInputStream.close();
        } catch (Exception e) {
            logger.severe("上传文件出现错误，" + e.getMessage());
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }

                if (null != outputStream) {
                    outputStream.close();
                }
            } catch (IOException e) {
                logger.severe("上传文件出现错误，" + e.getMessage());
            }
        }

        return file;
    }

    static class XFile {
        String name;
        String path;
        String type;
        long size;
        String uuidName;
        String uuidPath;
        String rootPath;
        String dir;
        String dirPath;
        String uploadingName;
        String uploadingPath;
        Integer chunk;
        Integer chunks;
    }
}

