package com.fc.rss.demo.service;

import com.fc.rss.demo.RssFC;
import com.fc.rss.demo.utils.ConfigUtils;
import com.rometools.rome.io.FeedException;
import org.junit.Test;

import java.io.IOException;

import static com.fc.rss.demo.RssFC.OTS_ENDPOINT;
import static com.fc.rss.demo.RssFC.OTS_INSTANCE_NAME;
import static org.junit.Assert.*;

public class RssServiceTest {

    @Test
    public void testParseRss() throws IOException, FeedException {
        String[] config = ConfigUtils.readConfigs();
        RssService rssService = new RssService(OTS_ENDPOINT, config[0], config[1], config.length > 2 ? config[2] : null, OTS_INSTANCE_NAME);
        assertTrue(rssService.parseRss("http://www.ruanyifeng.com/blog/atom.xml").size() > 0);

    }

    @Test
    public void testQueryAllRss() {
        String[] config = ConfigUtils.readConfigs();
        RssService rssService = new RssService(OTS_ENDPOINT, config[0], config[1], config.length > 2 ? config[2] : null, OTS_INSTANCE_NAME);

        assertTrue(rssService.queryAllRss().size() > 0);
    }
}