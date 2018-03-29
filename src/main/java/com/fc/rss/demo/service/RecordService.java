package com.fc.rss.demo.service;

import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.*;
import com.alicloud.openservices.tablestore.model.internal.CreateTableRequestEx;
import com.fc.rss.demo.model.PageRecord;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RecordService {

    private final SyncClient syncClient;

    private static final String TABLE_NAME = "rss_meta";

    private static final String PRIMARY_URL = "url";

    private static final String COLUMN_FILENAME = "filename";

    public RecordService(String endpoint, String accessKeyId, String accessKeySecret, String securityToken, String instanceName) {
        syncClient = new SyncClient(endpoint, accessKeyId, accessKeySecret, instanceName, securityToken);
        ensureTableExist();
    }

    private void ensureTableExist() {
        ListTableResponse response = syncClient.listTable();
        boolean exist = response.getTableNames().stream()
                .anyMatch(table -> table.equals(TABLE_NAME));

        if ( ! exist ) {
            TableMeta tableMeta = new TableMeta(TABLE_NAME);

            tableMeta.addPrimaryKeyColumn(new PrimaryKeySchema(PRIMARY_URL, PrimaryKeyType.STRING));

            // 永不过期
            int timeToLive = -1;

            // 保存的最大版本数
            int maxVersions = 1;

            TableOptions tableOptions = new TableOptions(timeToLive, maxVersions);
            CreateTableRequestEx request = new CreateTableRequestEx(tableMeta, tableOptions);

            syncClient.createTable(request);
        }
    }

    public List<String> getNotExistRecords(List<String> urls) {
        if (urls == null || urls.size() == 0) return Collections.emptyList();

        MultiRowQueryCriteria multiRowQueryCriteria = new MultiRowQueryCriteria(TABLE_NAME);

        multiRowQueryCriteria.setMaxVersions(1);

        urls.stream().forEach(url -> {
            PrimaryKey primaryKey = PrimaryKeyBuilder.createPrimaryKeyBuilder()
                    .addPrimaryKeyColumn(PRIMARY_URL, PrimaryKeyValue.fromString(url)).build();
            multiRowQueryCriteria.addRow(primaryKey);
        });

        BatchGetRowRequest batchGetRowRequest = new BatchGetRowRequest();

        batchGetRowRequest.addMultiRowQueryCriteria(multiRowQueryCriteria);

        BatchGetRowResponse response = syncClient.batchGetRow(batchGetRowRequest);

        return response.getSucceedRows().stream().filter(row -> row.getRow() == null)
                .map(row -> urls.get(row.getIndex()))
                .collect(Collectors.toList());
    }

    public void putRecord(PageRecord pageRecord) {
        PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();

        primaryKeyBuilder.addPrimaryKeyColumn(PRIMARY_URL, PrimaryKeyValue.fromString(pageRecord.getUrl()));
        PrimaryKey primaryKey = primaryKeyBuilder.build();

        RowPutChange rowPutChange = new RowPutChange(TABLE_NAME, primaryKey);

        rowPutChange.addColumn(new Column(COLUMN_FILENAME, ColumnValue.fromString(pageRecord.getFilename())));

        syncClient.putRow(new PutRowRequest( rowPutChange));
    }

    public List<PageRecord> queryAllRecords() {
        RangeIteratorParameter rangeIteratorParameter = new RangeIteratorParameter(TABLE_NAME);

        // 设置起始主键
        PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder()
                .addPrimaryKeyColumn(PRIMARY_URL, PrimaryKeyValue.INF_MIN);
        rangeIteratorParameter.setInclusiveStartPrimaryKey(primaryKeyBuilder.build());

        // 设置结束主键
        primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder()
                .addPrimaryKeyColumn(PRIMARY_URL, PrimaryKeyValue.INF_MAX);
        rangeIteratorParameter.setExclusiveEndPrimaryKey(primaryKeyBuilder.build());

        rangeIteratorParameter.setMaxVersions(1);

        Iterator<Row> iterator = syncClient.createRangeIterator(rangeIteratorParameter);

        Iterable<Row> iterable = () -> iterator;

        return StreamSupport.stream(iterable.spliterator(), false)
                .map(row -> {
                    String url = row.getPrimaryKey().getPrimaryKeyColumn(PRIMARY_URL).getValue().asString();
                    String filename = row.getColumn(COLUMN_FILENAME).get(0).getValue().asString();

                    return new PageRecord(url, filename);
                }).collect(Collectors.toList());
    }
}
