package com.fc.rss.demo;

import com.fc.rss.demo.context.FcContext;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class RssFCTest {

    @Test
    @Ignore
    public void testHandler() throws IOException {
        RssFC rssFC = new RssFC();

        FcContext fcContext = new FcContext();
        rssFC.handleRequest(null, System.out, fcContext);

    }
}
