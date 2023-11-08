package com.qihang;

import com.qihang.service.upload.IUploadService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author bright
 */
@SpringBootApplication
@MapperScan("com.qihang.mapper.**")
public class QiHangLotteryApplication {
    public static void main(String[] args) {
        SpringApplication.run(QiHangLotteryApplication.class, args);
    }


}
