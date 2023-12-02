package com.qihang.reptile.spiders;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.selector.Html;

import java.util.Date;

@Slf4j
public class WebClientUtils {

    public static String spiderRun(String url) {
        log.info("======>爬取URL{}", url);
        final WebClient webClient = new WebClient(BrowserVersion.CHROME);   // 新建一个模拟谷歌Chrome浏览器的浏览器客户端对象

        webClient.getOptions().setThrowExceptionOnScriptError(false);   // 当JS执行出错的时候是否抛出异常, 这里选择不需要
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false); // 当HTTP的状态非200时是否抛出异常, 这里选择不需要
        webClient.getOptions().setActiveXNative(false); // 不启用ActiveX
        webClient.getOptions().setCssEnabled(false);    // 是否启用CSS, 因为不需要展现页面, 所以不需要启用
        webClient.getOptions().setJavaScriptEnabled(true);  // 很重要，启用JS
        webClient.getOptions().setDownloadImages(false);    // 不下载图片
        webClient.setAjaxController(new NicelyResynchronizingAjaxController()); // 很重要，设置支持AJAX
        webClient.getOptions().setScreenWidth(1000);
        webClient.getOptions().setScreenHeight(960);

        webClient.waitForBackgroundJavaScript(5 * 1000);   // 异步JS执行需要耗时,所以这里线程要阻塞30秒,等待异步JS执行结束

        HtmlPage page = null;
        try {
            page = webClient.getPage(url);  // 尝试加载给出的网页
        } catch (Exception e) {
            log.info("======>【严重】爬取失败：{}", url);
            e.printStackTrace();
        } finally {
            webClient.close();
        }

        return page.asXml();  // 直接将加载完成的页面转换成xml格式的字符串
    }

    public static void main(String[] args) {
        //FileUtil.writeString(spiderRun("https://trade.500.com/bjdc/index.php"), "d:\\bd.html", "utf-8");
//        String txt = FileUtil.readString("d:\\bd.html", "utf-8");
//        Html html = Html.create(txt);
//        String issueno = html.xpath("//select@[id='expect_select']/option[1]/text()").get();
//        System.out.println(issueno);
        System.out.println(DateUtil.year(new Date()));
    }
}
