package us.codecraft.webmagic.samples;

import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.util.List;

public class PLS500Processor implements PageProcessor {

    /**
     * 排列三出奖
     */
    public static final String URL2 = "https://kaijiang.500.com/pls.shtml";

    /**
     * 排列5开奖
     */
    public static final String URL16 = "https://kaijiang.500.com/plw.shtml";


    /**
     * 七星彩开奖
     */
    public static final String URL17 = "https://kaijiang.500.com/qxc.shtml";
    /**
     * 大乐透开奖
     */
    public static final String URL20 = "https://kaijiang.500.com/dlt.shtml";
    private Site site = Site.me()//Site.me().setCycleRetryTimes(5).setRetryTimes(5).setSleepTime(500).setTimeOut(3 * 60 * 1000)
            .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
            .setCharset("UTF-8");

    private static final int voteNum = 1000;


    @Override
    public void process(Page page) {
        //排列 开奖结果爬取
        Html html = page.getHtml();
        String issueNo = html.css(".td_title01 .cfont2 strong", "text").get();
        List<String> rewardList = html.css(".ball_box01 .ball_orange", "text").all();
        String type = "";
        String moneyAward = "";

        if (ObjectUtil.equal(page.getUrl().toString(), URL2)) {

        } else if (ObjectUtil.equal(page.getUrl().toString(), URL16)) {
        } else if (ObjectUtil.equal(page.getUrl().toString(), URL17)) {
            moneyAward = html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[4]/td[3]/text()").toString().replaceAll(",", "");
        } else if (ObjectUtil.equal(page.getUrl().toString(), URL20)) {
            rewardList = page.getHtml().css(".ball_box01 li", "text").all();
            moneyAward = html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[3]/td[4]/text()").toString().replaceAll(",", "") + "," + html.xpath("/html/body/div[6]/div[3]/div[2]/div[1]/div[2]/table[2]/tbody/tr[5]/td[4]/text()").toString().replaceAll(",", "");
        }
        String award = StringUtils.join(rewardList, ",");
        System.out.println("issuNo>>" + issueNo + ",moneyAward=>" + moneyAward + " ,award=>" + award);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new PLS500Processor()).
                addUrl("https://kaijiang.500.com/plw.shtml").
                addPipeline(new FilePipeline("D:\\home\\")).
                thread(5).
                run();
    }
}
