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
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.selenium.DownloadChrome;
import us.codecraft.webmagic.downloader.selenium.FirefoxDownloader;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;

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


    //TODO
    /*
     1,需要 安装对应的浏览器
     2,需要放置浏览器版本匹配的驱动程序。
     目前好像firefox没有这个要求，chrome要求比较严格，版本不匹配执行有问题。
     */
    @Bean
    public AbstractDownloader downloader() {
        switch (webdriverType) {
            case "chrome":
                return new DownloadChrome(chromeDriverPath, chromeExecPath);
            case "firefox":
                return new FirefoxDownloader(firefoxDriverPath);
            default:
                throw new RuntimeException("未设置 爬虫使用的浏览器驱动");
        }
    }


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
