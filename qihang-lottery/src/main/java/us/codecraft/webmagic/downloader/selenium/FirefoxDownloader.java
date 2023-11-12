package us.codecraft.webmagic.downloader.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public class FirefoxDownloader extends AbstractDownloader implements Closeable {

    private volatile WebDriverPool webDriverPool;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private int sleepTime = 0;

    private int poolSize = 1;

    private static final String DRIVER_PHANTOMJS = "phantomjs";

    /**
     * 新建
     *
     * @param firefoxDriverPath chromeDriverPath
     */
    public FirefoxDownloader(String firefoxDriverPath) {
        System.getProperties().setProperty("webdriver.gecko.driver",
                firefoxDriverPath);
        System.getProperties().setProperty("", "");
    }

    /**
     * Constructor without any filed. Construct PhantomJS browser
     *
     * @author bob.li.0718@gmail.com
     */
    public FirefoxDownloader() {
        // System.setProperty("phantomjs.binary.path",
        // "/Users/Bingo/Downloads/phantomjs-1.9.7-macosx/bin/phantomjs");
    }

    /**
     * set sleep time to wait until load success
     *
     * @param sleepTime sleepTime
     * @return this
     */
    public FirefoxDownloader setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    @Override
    public Page download(Request request, Task task) {
        checkInit();
        WebDriver webDriver = null;
        Page page = Page.fail();
        try {
            webDriver = webDriverPool.get();

            logger.info("downloading page " + request.getUrl());
            webDriver.get(request.getUrl());
            try {
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WebDriver.Options manage = webDriver.manage();
            Site site = task.getSite();
            if (site.getCookies() != null) {
                for (Map.Entry<String, String> cookieEntry : site.getCookies()
                        .entrySet()) {
                    Cookie cookie = new Cookie(cookieEntry.getKey(),
                            cookieEntry.getValue());
                    manage.addCookie(cookie);
                }
            }

            /*
             * TODO You can add mouse event or other processes
             *
             * @author: bob.li.0718@gmail.com
             */
            try {
                //休眠3秒就是为了动态的数据渲染完成后在进行获取
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            WebElement webElement = webDriver.findElement(By.xpath("/html"));
            String content = webElement.getAttribute("outerHTML");
            page.setDownloadSuccess(true);
            page.setRawText(content);
            page.setHtml(new Html(content, request.getUrl()));
            page.setUrl(new PlainText(request.getUrl()));
            page.setRequest(request);
            onSuccess(request, task);
        } catch (Exception e) {
            logger.warn("download page {} error", request.getUrl(), e);
            onError(request, task, e);
        } finally {
            if (webDriver != null) {
                webDriverPool.returnToPool(webDriver);
            }
        }
        return page;
    }

    private void checkInit() {
        if (webDriverPool == null) {
            synchronized (this) {
                if (webDriverPool == null) {
                    webDriverPool = new WebDriverPool(poolSize);
                }
            }
        }
    }

    @Override
    public void setThread(int thread) {
        this.poolSize = thread;
    }

    @Override
    public void close() throws IOException {
        if (webDriverPool != null) {
            webDriverPool.closeAll();
        }
    }
}
