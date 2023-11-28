package com.qihang.common.util.upload;


import cn.hutool.core.date.DateUtil;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;


@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "local")
public class LocalUtil {

    String filePath;
    String url;
    String type;

    public String saveFile(InputStream is, File file) {
        String root = this.filePath;
        String path = file.getAbsolutePath();
        String urlPath = path.substring(root.length());
        log.info(" path{} 上传文件 {}", path, url + urlPath);
        if (file.exists()) {
            return url + urlPath;
        }
        file.getParentFile().mkdirs();

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] by = new byte[1024];
            int len = -1;
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            while ((len = is.read(by)) != -1) {
                bufferedOutputStream.write(by, 0, len);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return url + urlPath;
    }

    public String saveFile(InputStream is, String path) {
        BufferedOutputStream bufferedOutputStream = null;
        try {
            byte[] by = new byte[1024];
            int len = -1;
            String root = this.filePath;
            String filePath = File.separator + path + File.separator + builderName(".png");
            if (path.indexOf(".") > -1) {
                filePath = File.separator + path;
            }
            File saveFile = new File(root + filePath);
            saveFile.getParentFile().mkdirs();
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(saveFile));
            while ((len = is.read(by)) != -1) {
                bufferedOutputStream.write(by, 0, len);
            }
            //文件访问路径
            log.info(" path: {} 上传文件 在 {}", path, url + filePath);
            String address = url + filePath;
            return address;
        } catch (Exception e) {
            log.error(" saveFile {} 出现错误 {}", path, e);
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return "";
    }

    public String saveFile(InputStream is, String path, String filename) {
        try {
            byte[] by = new byte[1024];
            int len = -1;
            String root = this.filePath;
            int idx = filename.indexOf(".");
            String filePath = "/" + path + "/" + builderName(filename.substring(idx));
            File saveFile = new File(root + filePath);
            saveFile.getParentFile().mkdirs();
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            OutputStream outputStream = new FileOutputStream(saveFile);
            while ((len = is.read(by)) != -1) {
                outputStream.write(by, 0, len);
            }
            //文件访问路径
            String address = url + filePath;
            return address;
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 前端上传文件
     *
     * @param files
     * @return
     */
    public String upload(MultipartFile files) {
        try {
            return saveFile(files.getInputStream(), "font", files.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 后端上传图片
     *
     * @param is
     * @return
     */
    public String upload(InputStream is) {
        return saveFile(is, "admin");
    }

    public static String builderName(String suffix) {
        return DateUtil.format(LocalDateTime.now(), "yyyy/MM/dd/HH") + "/" + RandomStringUtils.randomAlphanumeric(10) + suffix;
    }


}
