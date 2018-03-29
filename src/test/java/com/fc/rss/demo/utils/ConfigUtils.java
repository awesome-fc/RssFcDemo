package com.fc.rss.demo.utils;

import com.fc.rss.demo.credential.FileCredentials;
import junit.framework.AssertionFailedError;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class ConfigUtils {

    public static String[] readConfigs() {
        InputStream inputStream = FileCredentials.class.getResourceAsStream("/access.conf");

        if (inputStream == null) {
            throw new AssertionFailedError("config file must be exist");
        }

        Optional<String> configOptinal = new BufferedReader(new InputStreamReader(inputStream))
                .lines().findFirst();

        String configContent = configOptinal.orElseThrow(() -> new AssertionFailedError("config file must be exist and must be not empty"));

        return configContent.split(",");
    }
}
