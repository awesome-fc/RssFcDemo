package com.fc.rss.demo.service;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TemplateServiceTest {

    @Test
    public void testRender() {
        String template = "<p th:text=\"${message}\">Hello World!</p>";

        TemplateService templateService = new TemplateService();

        final Map<String, Object> variables = new HashMap<>();
        variables.put("message", "xxx");

        assertEquals("<p>xxx</p>", templateService.render(template, variables));
    }
}