package us.codecraft.webmagic.downloader.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author code4crafter@gmail.com <br>
 * Date: 13-7-26 <br>
 * Time: 下午1:41 <br>
 */
class WebDriverPool {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static int DEFAULT_CAPACITY = 5;

    private final int capacity;

    private final static int STAT_RUNNING = 1;

    private final static int STAT_CLODED = 2;

    private AtomicInteger stat = new AtomicInteger(STAT_RUNNING);

    /*
     * new fields for configuring phantomJS
     */
    private WebDriver mDriver = null;
    private boolean mAutoQuitDriver = true;

    private static final String DEFAULT_CONFIG_FILE = "selenium.properties";
    private static final String DRIVER_FIREFOX = "firefox";
    private static final String DRIVER_CHROME = "chrome";
    private static final String DRIVER_PHANTOMJS = "phantomjs";

    protected static Properties sConfig;
    protected static DesiredCapabilities sCaps;

    /**
     * Configure the GhostDriver, and initialize a WebDriver instance. This part
     * of code comes from GhostDriver.
     * https://github.com/detro/ghostdriver/tree/master/test/java/src/test/java/ghostdriver
     *
     * @throws IOException
     * @author bob.li.0718@gmail.com
     */
    public void configure() throws IOException {
        // Read config file
        sConfig = new Properties();
        String configFile = DEFAULT_CONFIG_FILE;
        if (System.getProperty("selenuim_config") != null) {
            configFile = System.getProperty("selenuim_config");
        }
        sConfig.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile));

        // Prepare capabilities
        sCaps = new DesiredCapabilities();
        sCaps.setJavascriptEnabled(true);
        sCaps.setCapability("takesScreenshot", false);

        String driver = sConfig.getProperty("driver", DRIVER_PHANTOMJS);

        // Fetch PhantomJS-specific configuration parameters
        if (driver.equals(DRIVER_PHANTOMJS)) {
            // "phantomjs_exec_path"
            if (sConfig.getProperty("phantomjs_exec_path") != null) {
                sCaps.setCapability(
                        PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                        sConfig.getProperty("phantomjs_exec_path"));
            } else {
                throw new IOException(
                        String.format(
                                "Property '%s' not set!",
                                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY));
            }
            // "phantomjs_driver_path"
            if (sConfig.getProperty("phantomjs_driver_path") != null) {
                System.out.println("Test will use an external GhostDriver");
                sCaps.setCapability(
                        PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_PATH_PROPERTY,
                        sConfig.getProperty("phantomjs_driver_path"));
            } else {
                System.out
                        .println("Test will use PhantomJS internal GhostDriver");
            }
        }

        // Disable "web-security", enable all possible "ssl-protocols" and
        // "ignore-ssl-errors" for PhantomJSDriver
        // sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new
        // String[] {
        // "--web-security=false",
        // "--ssl-protocol=any",
        // "--ignore-ssl-errors=true"
        // });

        ArrayList<String> cliArgsCap = new ArrayList<String>();
        cliArgsCap.add("--web-security=false");
        cliArgsCap.add("--ssl-protocol=any");
        cliArgsCap.add("--ignore-ssl-errors=true");
        sCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                cliArgsCap);

        // Control LogLevel for GhostDriver, via CLI arguments
        sCaps.setCapability(
                PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_CLI_ARGS,
                new String[]{"--logLevel="
                        + (sConfig.getProperty("phantomjs_driver_loglevel") != null ? sConfig
                        .getProperty("phantomjs_driver_loglevel")
                        : "INFO")});

        // String driver = sConfig.getProperty("driver", DRIVER_PHANTOMJS);

        // Start appropriate Driver
        if (isUrl(driver)) {
            sCaps.setBrowserName("phantomjs");
            mDriver = new RemoteWebDriver(new URL(driver), sCaps);
        } else if (driver.equals(DRIVER_FIREFOX)) {
            FirefoxOptions options = new FirefoxOptions();
            //wyong edit 不调起firefox浏览器
            options.setHeadless(true);
            //禁用页面js
            options.setCapability("javascript.enabled", false);
            mDriver = new FirefoxDriver(options);
        } else if (driver.equals(DRIVER_CHROME)) {
            ChromeOptions options = new ChromeOptions();
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
            mDriver = new ChromeDriver(options);
        } else if (driver.equals(DRIVER_PHANTOMJS)) {
            mDriver = new PhantomJSDriver(sCaps);
        }
    }

    /**
     * check whether input is a valid URL
     *
     * @param urlString urlString
     * @return true means yes, otherwise no.
     * @author bob.li.0718@gmail.com
     */
    private boolean isUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException mue) {
            return false;
        }
    }

    /**
     * store webDrivers created
     */
    private List<WebDriver> webDriverList = Collections
            .synchronizedList(new ArrayList<WebDriver>());

    /**
     * store webDrivers available
     */
    private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>();

    public WebDriverPool(int capacity) {
        this.capacity = capacity;
    }

    public WebDriverPool() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * @return
     * @throws InterruptedException
     */
    public WebDriver get() throws InterruptedException {
        checkRunning();
        WebDriver poll = innerQueue.poll();
        if (poll != null) {
            return poll;
        }
        if (webDriverList.size() < capacity) {
            synchronized (webDriverList) {
                if (webDriverList.size() < capacity) {

                    // add new WebDriver instance into pool
                    try {
                        configure();
                        innerQueue.add(mDriver);
                        webDriverList.add(mDriver);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // ChromeDriver e = new ChromeDriver();
                    // WebDriver e = getWebDriver();
                    // innerQueue.add(e);
                    // webDriverList.add(e);
                }
            }

        }
        return innerQueue.take();
    }

    public void returnToPool(WebDriver webDriver) {
        checkRunning();
        innerQueue.add(webDriver);
    }

    protected void checkRunning() {
        if (!stat.compareAndSet(STAT_RUNNING, STAT_RUNNING)) {
            throw new IllegalStateException("Already closed!");
        }
    }

    public void closeAll() {
        boolean b = stat.compareAndSet(STAT_RUNNING, STAT_CLODED);
        if (!b) {
            throw new IllegalStateException("Already closed!");
        }
        for (WebDriver webDriver : webDriverList) {
            logger.info("Quit webDriver" + webDriver);
            webDriver.quit();
            webDriver = null;
        }
        //根据不同的操作系统结束残留的chrome进程
        String os = System.getProperty("os.name");
        if (os != null && os.toLowerCase().startsWith("windows")) {
            try {
                logger.info("{}清理残留进程", os);
                Runtime.getRuntime().exec("taskkill /F /im " + "chromedriver.exe");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (os != null && os.toLowerCase().startsWith("linux")) {
            try {
                logger.info("{}清理残留进程", os);
                Runtime.getRuntime().exec("ps -ef | grep Chrome | grep -v grep  | awk '{print \"kill -9 \"$2}'  | sh");
                Runtime.getRuntime().exec("ps -ef | grep chromedriver | grep -v grep  | awk '{print \"kill -9 \"$2}'  | sh");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}


