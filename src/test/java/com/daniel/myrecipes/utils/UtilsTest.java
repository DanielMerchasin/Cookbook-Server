package com.daniel.myrecipes.utils;

import org.json.JSONObject;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UtilsTest {

    @Test
    public void testRNG() {

        for (int i = 0; i < 10; i++) {
            System.out.println("img_10412_" + System.currentTimeMillis() + "_" + Utils.generateRandomString(16) + ".png");
        }

    }

    @Test
    public void testVersionComparison() {

//        System.out.println(Utils.compareVersions());



    }

    @Test
    public void byteArrayTest() {

        byte[] data = "YO WASSUP".getBytes(StandardCharsets.UTF_8);

        try {
            for (int i = 0; i < 2; i++) {
                try (ByteArrayInputStream input = new ByteArrayInputStream(data);
                     ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = input.read(buffer)) != -1)
                        output.write(buffer, 0, len);
                    System.out.println(new String(output.toByteArray(), StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}