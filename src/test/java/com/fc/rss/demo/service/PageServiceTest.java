package com.fc.rss.demo.service;

import com.fc.rss.demo.log.ConsoleFunctionComputeLogger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class PageServiceTest {

    private Path downloadHome;

    @Before
    public void setup() throws IOException {
        downloadHome = Files.createTempDirectory("fc_test");
    }

    @Test
    public void testDownloadPage() {
        PageService pageService = new PageService(downloadHome.toFile(), new ConsoleFunctionComputeLogger());

        pageService.downloadPage("http://www.ruanyifeng.com/blog/2018/03/node-debugger.html");

        assertTrue(downloadHome.toFile().listFiles().length > 0);
    }

    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(downloadHome.toFile());
    }

}