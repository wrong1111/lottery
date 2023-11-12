package com.qihang.reptile;

import com.qihang.constant.CrawlingAddressConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.scheduler.BloomFilterDuplicateRemover;
import us.codecraft.webmagic.scheduler.QueueScheduler;

import javax.annotation.Resource;

@Component
public class SpiderRunner {

    @Resource
    LotteryPipeline lotteryPipeline;

    @Value("${webdriver.chrome.driver.path}")
    private String chromeDriverPath;

    @Resource
    FirefoxDownload firefoxDownload;

    /*
     比赛 赛事
     */
    public void runHour() {
        Spider.create(new LotteryProcessor()).addUrl(
                        CrawlingAddressConstant.URL1 //足彩比赛
                        , CrawlingAddressConstant.URL4 // 篮彩比赛
                        , CrawlingAddressConstant.URL8 //北京单场
                        , CrawlingAddressConstant.URL9 //北京单场 进球
                        , CrawlingAddressConstant.URL10 //北京单场 上下单双
                        , CrawlingAddressConstant.URL11 //北京单场 比分
                        , CrawlingAddressConstant.URL12 //北京单场 半全场进球
                        , CrawlingAddressConstant.URL18 //胜负彩比赛
                )
                //自定义下载规则，主要是来处理爬取动态的网站,如果只是爬取静态的这个可以用默认的就行
                // http://chromedriver.storage.googleapis.com/index.html 版本一定会要与浏览器对应
                .setDownloader(firefoxDownload).setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000000))).thread(5).addPipeline(lotteryPipeline).runAsync();
    }

    /*
        遗漏 七星彩，排列3，排列5，大乐透
        */
    public void runOmit() {
        Spider.create(new LotteryProcessor()).addUrl(
                        CrawlingAddressConstant.URL21 //数字彩 遗漏排列3
                        , CrawlingAddressConstant.URL22 //数字彩 遗漏排列5
                        , CrawlingAddressConstant.URL23 //数字彩 遗漏七星彩
                        , CrawlingAddressConstant.URL24 //数字彩 遗漏大乐透
                )
                //自定义下载规则，主要是来处理爬取动态的网站,如果只是爬取静态的这个可以用默认的就行
                // http://chromedriver.storage.googleapis.com/index.html 版本一定会要与浏览器对应
                .setDownloader(new SeleniumDownloader(chromeDriverPath)).setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000000))).thread(5).addPipeline(lotteryPipeline).run();

    }

    /*
     双色球，快乐8，福彩3D，七乐彩,排列3,排列5，七星彩， 开奖
     */
    public void runDay() {
        Spider.create(new LotteryProcessor()).addUrl(
                        CrawlingAddressConstant.URL_FC3D // 福彩3D 开奖
                        , CrawlingAddressConstant.URL_SSQ // 双色球 开奖
                        , CrawlingAddressConstant.URL_KL8 // 快乐8 开奖
                        , CrawlingAddressConstant.URL_QLC // 七乐彩 开奖
                        , CrawlingAddressConstant.URL20 //大乐透开奖
                        , CrawlingAddressConstant.URL17 //七星彩开奖
                        , CrawlingAddressConstant.URL16 //排列5开奖
                        , CrawlingAddressConstant.URL2 //排列三出奖
                )
                //自定义下载规则，主要是来处理爬取动态的网站,如果只是爬取静态的这个可以用默认的就行
                // http://chromedriver.storage.googleapis.com/index.html 版本一定会要与浏览器对应
                .setDownloader(new SeleniumDownloader(chromeDriverPath)).setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000000))).thread(5).addPipeline(lotteryPipeline).run();
    }

    public void run() {
        Spider.create(new LotteryProcessor()).addUrl(
//                        CrawlingAddressConstant.URL1 //足彩比赛
//                        , CrawlingAddressConstant.URL2 //排列三出奖
                        CrawlingAddressConstant.URL3 //足彩对局分析
//                        , CrawlingAddressConstant.URL4 // 篮彩比赛
                        , CrawlingAddressConstant.URL5 //篮球对局分析
                        , CrawlingAddressConstant.URL6 //足球开奖
                        , CrawlingAddressConstant.URL7 //篮球开奖
//                        , CrawlingAddressConstant.URL8 //北京单场
//                        , CrawlingAddressConstant.URL9 //北京单场 进球
//                        , CrawlingAddressConstant.URL10 //北京单场 上下单双
//                        , CrawlingAddressConstant.URL11 //北京单场 比分
//                        , CrawlingAddressConstant.URL12 //北京单场 半全场进球
//                        , CrawlingAddressConstant.URL13 //北单开奖
                        , CrawlingAddressConstant.URL14 //篮球单关查询
                        , CrawlingAddressConstant.URL15 //北单分析
//                        , CrawlingAddressConstant.URL16 //排列5开奖
//                        , CrawlingAddressConstant.URL17 //七星彩开奖
                        , CrawlingAddressConstant.URL18 //胜负彩比赛
                        , CrawlingAddressConstant.URL19 //胜负彩开奖
//                        , CrawlingAddressConstant.URL20 //大乐透开奖
//                        , CrawlingAddressConstant.URL21 //数字彩 遗漏排列3
//                        , CrawlingAddressConstant.URL22 //数字彩 遗漏排列5
//                        , CrawlingAddressConstant.URL23 //数字彩 遗漏七星彩
//                        , CrawlingAddressConstant.URL24 //数字彩 遗漏大乐透
//                        , CrawlingAddressConstant.URL_FC3D // 福彩3D 开奖
//                        ,CrawlingAddressConstant.URL_SSQ // 双色球 开奖
//                        , CrawlingAddressConstant.URL_KL8 // 快乐8 开奖
//                        , CrawlingAddressConstant.URL_QLC // 七乐彩 开奖
                )
                //自定义下载规则，主要是来处理爬取动态的网站,如果只是爬取静态的这个可以用默认的就行
                // http://chromedriver.storage.googleapis.com/index.html 版本一定会要与浏览器对应
                .setDownloader(new SeleniumDownloader(chromeDriverPath)).setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000000))).thread(5).addPipeline(lotteryPipeline).run();
    }
}
