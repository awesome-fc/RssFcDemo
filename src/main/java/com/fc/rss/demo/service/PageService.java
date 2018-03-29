package com.fc.rss.demo.service;

import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.fc.rss.demo.model.PageRecord;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.thymeleaf.util.StringUtils.isEmpty;

public class PageService {

    private File downloadHome;

    private FunctionComputeLogger logger;

    public PageService(File downloadHome, FunctionComputeLogger logger) {
        this.downloadHome = downloadHome;
        this.logger = logger;
    }

    public PageRecord downloadPage(String url) {
        try {
            if ( ! downloadHome.exists() ) {
                downloadHome.mkdirs();
            }

            logger.info("start downloading: " + url + "...");

            Connection.Response response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36")
                    .execute();

            final Document document = response.parse();

            String title = nomalLize(document.title());

            File pageHome = new File(downloadHome, title);
            pageHome.mkdir();

            // download all img to disk
            downloadResources(document, pageHome, "img", "img", "src");

            // download all css to disk
            downloadResources(document, pageHome, "css", "link[rel=stylesheet]", "href");

            // download all js to disk
            downloadResources(document, pageHome, "js", "script", "src");

            // write page to disk
            final byte[] body = document.toString().getBytes();
            Files.write(new File(pageHome, "index.html").toPath(), body);

            return new PageRecord(url, title);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void downloadResources(Document document, File pageHome, String resourceName, String selector, String attr) {
        File resourceDir = new File(pageHome, resourceName);
        resourceDir.mkdir();

        // download all img disk
        Elements elements = document.select(selector);
        elements.stream().forEach(element -> {
            //make sure to get the absolute URL using abs: prefix
            String absSrc = element.attr("abs:" + attr);

            if ( ! isEmpty(absSrc) ) {
                int start = absSrc.lastIndexOf("/");
                if (start < 0) start = -1;

                int end = absSrc.lastIndexOf("?");
                if (end < 0 || end < start) end = absSrc.length();

                String name = absSrc.substring(start + 1, end);

                String relativePath = String.format("%s/%s", resourceName, name);

                if (StringUtils.isEmpty(name)) {
                    return;
                }

                try {
                    Files.write(new File(resourceDir, name).toPath(), Jsoup.connect(absSrc)
                            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36")
                            .ignoreContentType(true).execute().bodyAsBytes());
                } catch (IOException e) {
                    logger.error("error occur while downloading resource: " + absSrc + " and error is " + e.getMessage());
                }

                element.attr(attr, relativePath);
            }
        });

    }


    public String nomalLize(String title) {
        return title.replaceAll("/", "_");
    }

}
