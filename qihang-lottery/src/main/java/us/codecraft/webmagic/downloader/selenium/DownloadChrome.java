package us.codecraft.webmagic.downloader.selenium;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
import java.util.Properties;

@Slf4j
public class DownloadChrome extends AbstractDownloader implements Closeable {

    // 创建WebDriver对象
    static WebDriver driver = null;
    int sleepTime = 3;

    String driverPath;
    String execPath;


    static void initWebDriver(String exec) {
        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            options.setBinary(exec);
            //远程 执行
            options.addArguments("--remote-allow-origins=*");
            //  浏览器不提供可视化页面（无头模式
            options.addArguments("headless");
            // 谷歌禁用GPU加速
            options.addArguments("disable-gpu");
            //隐身模式（无痕模式）
            options.addArguments("incognito");
            // 禁用3D软件光栅化器
            options.addArguments("disable-software-rasterizer");
            //解决DevToolsActivePort文件不存在的报错
            options.addArguments("no-sandbox");
            //不加载图片, 提升速度
            options.addArguments("blink-settings=imagesEnabled=false");
            //
            options.addArguments("disable-dev-shm-usage");
            // 禁用GPU缓存
            options.addArguments("disable-gpu-program-cache");
            // 禁用扩展
            options.addArguments("disable-extensions");
            // 禁用JS
            options.addArguments("disable-javascript");
            // 禁用java
            options.addArguments("disable-java");
            //禁止加载所有插件，可以增加速度。可以通过about:plugins页面查看效果
            options.addArguments("disable-plugins");
            // 禁用图像
            options.addArguments("disable-images");
            driver = new ChromeDriver(options);
        }
    }

    @SneakyThrows
    public DownloadChrome(String driver, String exec) {
        this.driverPath = driver;
        this.execPath = exec;
        System.setProperty("webdriver.chrome.driver", driver);
        System.setProperty("webdriver.chrome.bin", exec);
        initWebDriver(exec);
    }

    @Override
    public void close() throws IOException {
        //this.driver.quit();
        //this.driver.close();

        //根据不同的操作系统结束残留的chrome进程
//        String os = System.getProperty("os.name");
//        if (os != null && os.toLowerCase().startsWith("windows")) {
//            try {
//                log.info("{}清理残留进程", os);
//                Runtime.getRuntime().exec("taskkill /F /im " + "chromedriver.exe");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        } else if (os != null && os.toLowerCase().startsWith("linux")) {
//            try {
//                log.info("{}清理残留进程", os);
//                Runtime.getRuntime().exec("ps -ef | grep Chrome | grep -v grep  | awk '{print \"kill -9 \"$2}'  | sh");
//                Runtime.getRuntime().exec("ps -ef | grep chromedriver | grep -v grep  | awk '{print \"kill -9 \"$2}'  | sh");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    @Override
    public Page download(Request request, Task task) {
        Page page = Page.fail();
        try {
            if (driver == null) {
                initWebDriver(this.execPath);
                log.error("ERROR<<<<<<<<<<<<<<<<< 初始化driver {} >>>>>>>>>>", request.getUrl());
            }
            log.info("<<<<<<<<<<<<<<<<< downloading page {} >>>>>>>>>>", request.getUrl());
            driver.get(request.getUrl());
            try {
                Thread.sleep(sleepTime * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            WebDriver.Options manage = driver.manage();
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
            WebElement webElement = driver.findElement(By.xpath("/html"));
            log.info("<<<<<<<<<<<<<<<<<  page {} Ok >>>>>>>>>>>>>>>>>>>>" + request.getUrl());
            String content = webElement.getAttribute("outerHTML");
            page.setDownloadSuccess(true);
            page.setRawText(content);
            page.setHtml(new Html(content, request.getUrl()));
            page.setUrl(new PlainText(request.getUrl()));
            page.setRequest(request);
            onSuccess(request, task);
        } catch (Exception e) {
            log.warn("download page {} error", request.getUrl(), e);
            onError(request, task, e);
        } finally {

        }
        return page;
    }

    @Override
    public void setThread(int threadNum) {
    }
}
