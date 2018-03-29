package com.fc.rss.demo.log;

import com.aliyun.fc.runtime.FunctionComputeLogger;

public class ConsoleFunctionComputeLogger implements FunctionComputeLogger {
    public void trace(String string) {
        System.out.println("trace: " + string);

    }

    public void debug(String string) {
        System.out.println("debug: " + string);
    }

    public void info(String string) {
        System.out.println("info: " + string);

    }

    public void warn(String string) {
        System.out.println("warn: " + string);

    }

    public void error(String string) {
        System.out.println("error: " + string);

    }
}
