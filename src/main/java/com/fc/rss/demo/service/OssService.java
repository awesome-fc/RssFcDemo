package com.fc.rss.demo.service;

import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CreateBucketRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class OssService implements AutoCloseable {

    // ossClient 并发安全
    private final OSSClient ossClient;

    private final FunctionComputeLogger logger;

    private final String bucketName;

    public OssService(String endpoint, String bucketName, String accessKeyId, String accessKeySecret, String securityToken, FunctionComputeLogger logger) {
        this.ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret, securityToken);
        this.logger = logger;
        this.bucketName = bucketName;

        ensureBucketExist();
    }

    private void ensureBucketExist() {
        if ( ! ossClient.doesBucketExist(bucketName) ) {
            // Create a new OSS bucket
            logger.info("Creating bucket " + bucketName + "\n");
            ossClient.createBucket(bucketName);
            CreateBucketRequest createBucketRequest= new CreateBucketRequest(bucketName);
            createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
            ossClient.createBucket(createBucketRequest);
        }
    }

    public void uploadFiles(Path path) throws IOException {
        logger.info("begin uploading static resources...");

        // upload files recursively
        try (Stream<Path> stream = Files.walk(path)) {
            stream.map(Path::toFile)
                    .filter(File::isFile)
                    .forEach(f -> {
                        Path relative = path.relativize(f.toPath());

                        ossClient.putObject(bucketName, relative.toString(), f);
                        logger.info(String.format("uploading file %s success", f.getAbsolutePath()));
                    });
        }

        logger.info("uploading static resources end...");
    }

    @Override
    public void close() {
        ossClient.shutdown();
    }
}
