package com.fc.rss.demo;


import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.fc.runtime.StreamRequestHandler;
import com.fc.rss.demo.model.PageRecord;
import com.fc.rss.demo.service.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RssFC implements StreamRequestHandler {

    public final static String OSS_ENDPOINT = "https://oss-cn-hangzhou.aliyuncs.com";
    public final static String OSS_BUCKET_NAME = "rss-fc";

    public final static String OTS_ENDPOINT = "https://rssdb.cn-hangzhou.ots.aliyuncs.com";
    public final static String OTS_INSTANCE_NAME = "rssdb";

    private final static File RESOURCE_ROOT = new File("/tmp/pages");

    private final static String INDEX_HTML_TEMPLATE = "\n" +
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "  <meta charset=\"utf-8\">\n" +
            "  <title>RssDemo</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div th:each=\"filename, iterStat : ${filenames}\">\n" +
            "  <span>\n" +
            "    <a th:href=\"'/' + ${filename} + '/index.html'\" th:text=\"${filename}\"></a>\n" +
            "  </span>\n" +
            "</div>\n" +
            "</body>\n" +
            "</html>\n";

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        if ( ! RESOURCE_ROOT.exists() ) {
            RESOURCE_ROOT.mkdirs();
        }

        String accessKeyId = context.getExecutionCredentials().getAccessKeyId();
        String accessKeysecret = context.getExecutionCredentials().getAccessKeySecret();
        String securityToken = context.getExecutionCredentials().getSecurityToken();

        FunctionComputeLogger logger = context.getLogger();

        try (OssService ossService = new OssService(OSS_ENDPOINT, OSS_BUCKET_NAME, accessKeyId, accessKeysecret, securityToken, logger)) {
            RecordService recordService = new RecordService(OTS_ENDPOINT, accessKeyId, accessKeysecret, securityToken, OTS_INSTANCE_NAME);
            PageService pageService = new PageService(RESOURCE_ROOT, logger);
            RssService rssService = new RssService(OTS_ENDPOINT, accessKeyId, accessKeysecret, securityToken, OTS_INSTANCE_NAME);
            TemplateService templateService = new TemplateService();

            logger.info("query all links from ots...");

            rssService.queryAllRss().stream().forEach(rss -> {
                try {
                    List<String> pages = rssService.parseRss(rss);

                    logger.info("find newly links...");

                    List<String> notExistUrls = recordService.getNotExistRecords(pages);

                    logger.info("download page to disk...");

                    List<PageRecord> pageRecords = notExistUrls.stream()
                            .map(url -> pageService.downloadPage(url))
                            .collect(Collectors.toList());

                    logger.info("update records to ots");

                    pageRecords.stream()
                            .forEach(pageRecord -> recordService.putRecord(pageRecord));

                    logger.info("render index.html");

                    List<String> filenames = recordService.queryAllRecords().stream()
                            .map(PageRecord::getFilename)
                            .collect(Collectors.toList());

                    Map<String, Object> variables = new HashMap<>();
                    variables.put("filenames", filenames);

                    String renderedContent = templateService.render(INDEX_HTML_TEMPLATE, variables);
                    Files.write(new File(RESOURCE_ROOT + "/index.html").toPath(), renderedContent.getBytes());

                    // upload page to oss
                    logger.info("upload pages to oss");
                    ossService.uploadFiles(RESOURCE_ROOT.toPath());

                    logger.info("clean disk cache");
                    FileUtils.cleanDirectory(RESOURCE_ROOT);
                } catch (IOException e) {
                    logger.error("error occur: " + e.getMessage());
                }
            });


        }

        outputStream.write("finish...".getBytes());
    }
}
