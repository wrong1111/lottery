package com.qihang.config;


import com.qihang.common.util.upload.LocalUtil;
import com.qihang.common.util.upload.S3Util;
import com.qihang.service.upload.IUploadService;
import com.qihang.service.upload.LocalUploadServiceImpl;
import com.qihang.service.upload.UploadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {


    @Autowired
    LocalUtil localUtil;

    @Autowired
    S3Util s3Util;


    @Bean
    public IUploadService uploadService() {
        switch (localUtil.getType()) {
            case "s3":
                return new UploadServiceImpl().setProperties(s3Util);
            default:
                return new LocalUploadServiceImpl().setProperties(localUtil);
        }
    }
}
