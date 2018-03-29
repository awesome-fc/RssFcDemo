package com.fc.rss.demo.context;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.Credentials;
import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.fc.runtime.FunctionParam;
import com.fc.rss.demo.credential.FileCredentials;
import com.fc.rss.demo.log.ConsoleFunctionComputeLogger;

import java.io.File;

public class FcContext implements Context {
    private ConsoleFunctionComputeLogger logger = new ConsoleFunctionComputeLogger();
    private Credentials credentials = new FileCredentials();

    public String getRequestId() {
        return "requestId";
    }

    public Credentials getExecutionCredentials() {
        return credentials;
    }

    public FunctionParam getFunctionParam() {
        return null;
    }

    public FunctionComputeLogger getLogger() {
        return logger;
    }
}
