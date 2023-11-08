package com.qihang.common.util.upload;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;


@Data
@Component
@ConfigurationProperties(prefix = "local")
public class LocalUtil {

    String filePath;
    String url;
    String type;

    /**
     * 前端上传文件
     *
     * @param files
     * @return
     */
    public String upload(MultipartFile files) {
        try {
            InputStream is = files.getInputStream();
            String fileName = IdWorker.get32UUID() + ".png";
            byte[] by = new byte[1024];
            int len = -1;
            String root = this.filePath;
            String path = "font";
            String filePaths = buildPath(root, path, fileName);
            String file = root + File.separator + path + File.separator + builderName(".png");
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(file)));
            while ((len = is.read(by)) != -1) {
                bufferedOutputStream.write(by, 0, len);
            }
            //文件访问路径
            String address = url + filePaths;
            return address;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 后端上传图片
     *
     * @param is
     * @return
     */
    public String upload(InputStream is) {
        try {
            String fileName = IdWorker.get32UUID() + ".png";
            byte[] by = new byte[1024];
            int len = -1;
            String root = this.filePath;
            String path = "admin";
            String filePaths = buildPath(root, path, fileName);
            String file = root + File.separator + path + File.separator + builderName(".png");
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(new File(file)));
            while ((len = is.read(by)) != -1) {
                bufferedOutputStream.write(by, 0, len);
            }
            //文件访问路径
            String address = url + filePaths;
            return address;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String builderName(String suffix) {
        return DateUtil.format(LocalDateTime.now(), "yyyy/MM/dd/HH") + RandomStringUtils.randomAlphanumeric(10) + suffix;
    }

    @SneakyThrows
    public static String buildPath(String root, String path, String name) {
        File file = new File(root + File.separator + path + File.separator + name);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            file.createNewFile();
        }
        return path + name;
    }


}
