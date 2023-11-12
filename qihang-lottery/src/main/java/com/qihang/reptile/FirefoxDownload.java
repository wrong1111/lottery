package com.qihang.reptile;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

@Component
public class FirefoxDownload extends AbstractDownloader implements Closeable {

    RemoteWebDriver driver;
    long sleepTime = 0;

    @Value("${webdriver.chrome.driver.path}")
    private String chromeDriverPath;

    public FirefoxDownload setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    @Resource
    FirefoxDownload firefoxDownload;

    @PostConstruct
    public void init() {
        ChromeOptions chromeOptions = new ChromeOptions();
        //不打开浏览器，命令行运行
        chromeOptions.addArguments("--headless");
        if (chromeDriverPath.indexOf("firefox") > -1) {
            this.driver = new FirefoxDriver(new FirefoxOptions(chromeOptions));
        } else {
            this.driver = new ChromeDriver(chromeOptions);
        }
    }

    @Override
    public Page download(Request request, Task task) {
        driver.get(request.getUrl());
        try {
            try {
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Site site = task.getSite();
            if (site.getCookies() != null) {
                for (Map.Entry<String, String> cookieEntry : site.getCookies().entrySet()) {
                    Cookie cookie = new Cookie(cookieEntry.getKey(), cookieEntry.getValue());
                    driver.manage().addCookie(cookie);
                }
            }
            //休眠3秒 为了页面渲染完成。
            Thread.sleep(3000);
            //滚动到底部
            // driver.executeScript("window.scrollTo(0,document.body.scrollHeight-1000)");
            // Page page = createPage(request.getUrl(), driver.getPageSource());

            WebElement webElement = driver.findElement(By.xpath("/html"));
            String content = webElement.getAttribute("outerHTML");
            Page page = Page.fail();
            page.setDownloadSuccess(true);
            page.setRawText(content);
            page.setHtml(new Html(content, request.getUrl()));
            page.setUrl(new PlainText(request.getUrl()));
            page.setRequest(request);
            onSuccess(request, task);
            return page;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setThread(int threadNum) {

    }


    @Override
    public void close() throws IOException {
        //是否要关闭浏览器
        driver.close();
    }
}
