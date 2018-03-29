package com.fc.rss.demo.service;

import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.*;
import com.alicloud.openservices.tablestore.model.internal.CreateTableRequestEx;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RssService {

    private final SyncClient syncClient;

    private static final String TABLE_NAME = "rss_list";

    private static final String PRIMARY_RSS = "rss";

    public RssService(String endpoint, String accessKeyId, String accessKeySecret, String securityToken, String instanceName) {
        syncClient = new SyncClient(endpoint, accessKeyId, accessKeySecret, instanceName, securityToken);
        ensureTableExist();
    }

    private void ensureTableExist() {
        ListTableResponse response = syncClient.listTable();
        boolean exist = response.getTableNames().stream()
                .anyMatch(table -> table.equals(TABLE_NAME));

        if ( ! exist ) {
            TableMeta tableMeta = new TableMeta(TABLE_NAME);

            tableMeta.addPrimaryKeyColumn(new PrimaryKeySchema(PRIMARY_RSS, PrimaryKeyType.STRING));

            // 永不过期
            int timeToLive = -1;

            // 保存的最大版本数
            int maxVersions = 1;

            TableOptions tableOptions = new TableOptions(timeToLive, maxVersions);
            CreateTableRequestEx request = new CreateTableRequestEx(tableMeta, tableOptions);

            syncClient.createTable(request);
        }
    }

    public List<String> queryAllRss() {
        RangeIteratorParameter rangeIteratorParameter = new RangeIteratorParameter(TABLE_NAME);

        // 设置起始主键
        PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder()
                .addPrimaryKeyColumn(PRIMARY_RSS, PrimaryKeyValue.INF_MIN);
        rangeIteratorParameter.setInclusiveStartPrimaryKey(primaryKeyBuilder.build());

        // 设置结束主键
        primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder()
                .addPrimaryKeyColumn(PRIMARY_RSS, PrimaryKeyValue.INF_MAX);
        rangeIteratorParameter.setExclusiveEndPrimaryKey(primaryKeyBuilder.build());

        rangeIteratorParameter.setMaxVersions(1);

        Iterator<Row> iterator = syncClient.createRangeIterator(rangeIteratorParameter);

        Iterable<Row> iterable = () -> iterator;

        return StreamSupport.stream(iterable.spliterator(), false)
                .map(row -> row.getPrimaryKey().getPrimaryKeyColumn(PRIMARY_RSS).getValue().asString())
                .collect(Collectors.toList());
    }

    public List<String> parseRss(String rss) {
        try {
            SyndFeedInput input = new SyndFeedInput();

            SyndFeed feed = input.build(new XmlReader(new URL(rss)));

            return feed.getEntries().stream()
                    .map(e -> e.getLink())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
