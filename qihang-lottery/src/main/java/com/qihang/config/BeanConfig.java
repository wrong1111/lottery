package com.qihang.config;


import com.qihang.common.util.upload.LocalUtil;
import com.qihang.common.util.upload.S3Util;
import com.qihang.service.upload.IUploadService;
import com.qihang.service.upload.LocalUploadServiceImpl;
import com.qihang.service.upload.UploadServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.selenium.DownloadChrome;
import us.codecraft.webmagic.downloader.selenium.FirefoxDownloader;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class BeanConfig {


    @Autowired
    LocalUtil localUtil;

    @Autowired
    S3Util s3Util;


    @Value("${webdriver.type}")
    String webdriverType;

    @Value("${webdriver.chrome.driver.path}")
    private String chromeDriverPath;

    @Value("${webdriver.firefox.driver.path}")
    String firefoxDriverPath;

    @Value("${webdriver.chrome.driver.exec}")
    String chromeExecPath;


    // 核心线程池大小
    private int corePoolSize = 50;

    // 最大可创建的线程数
    private int maxPoolSize = 200;

    // 队列最大长度
    private int queueCapacity = 1000;

    // 线程池维护线程所允许的空闲时间
    private int keepAliveSeconds = 300;


    @Bean
    public IUploadService uploadService() {
        switch (localUtil.getType()) {
            case "s3":
                return new UploadServiceImpl().setProperties(s3Util);
            default:
                return new LocalUploadServiceImpl().setProperties(localUtil);
        }
    }


    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程池对拒绝任务(无线程可用)的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

}
