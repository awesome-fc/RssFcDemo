package com.fc.rss.demo.credential;

import com.aliyun.fc.runtime.Credentials;
import com.fc.rss.demo.utils.ConfigUtils;
import junit.framework.AssertionFailedError;

import java.io.*;
import java.util.Optional;

public class FileCredentials implements Credentials {

    private String accessKeyId;

    private String accessKeySecret;

    private String securityToken;

    public FileCredentials() {
        String[] configs = ConfigUtils.readConfigs();

        accessKeyId = configs[0];
        accessKeySecret = configs[1];

        if (configs.length >= 3) {
            securityToken = configs[2];
        }
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public String getSecurityToken() {
        return securityToken;
    }
}
