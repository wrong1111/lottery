package us.codecraft.webmagic.downloader.selenium;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;

import java.io.Closeable;
import java.io.IOException;

public class DownloadFirefox extends AbstractDownloader implements Closeable {

    @Override
    public void close() throws IOException {

    }

    @Override
    public Page download(Request request, Task task) {
        return null;
    }

    @Override
    public void setThread(int threadNum) {

    }
}
