1，google浏览器需要本地禁用。否则会自动更新，导致定时任务失败。
2, 文件上传使用本地存储

3，liunx chromedriver=>106.0.5249.119
 
liunx /opt/google/google-chrome
在文件 最后添加
--no-sandbox --disable-dev-shm-usage --headless