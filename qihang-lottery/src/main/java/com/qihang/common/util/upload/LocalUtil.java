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

import java.io.*;
import java.time.LocalDateTime;


@Data
@Component
@ConfigurationProperties(prefix = "local")
public class LocalUtil {

    String filePath;
    String url;
    String type;

    public String saveFile(InputStream is, String path) {
        try {
            byte[] by = new byte[1024];
            int len = -1;
            String root = this.filePath;
            String filePath = File.separator + path + File.separator + builderName(".png");
            File saveFile = new File(root + filePath);
            saveFile.getParentFile().mkdirs();
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(saveFile));
            while ((len = is.read(by)) != -1) {
                bufferedOutputStream.write(by, 0, len);
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
            return saveFile(files.getInputStream(), "font");
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
