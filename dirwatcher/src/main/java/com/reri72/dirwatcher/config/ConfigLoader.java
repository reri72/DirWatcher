package com.reri72.dirwatcher.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigLoader {
    private static final String CONFIG_FILE = "config.json";

    public static WatcherConfig loadConfig() throws IOException, JsonSyntaxException {
        Gson gson = new Gson();
        Path configPath = Paths.get(CONFIG_FILE).toAbsolutePath();

        System.out.println("Configuration file : " + configPath.toAbsolutePath());

        try (FileReader reader = new FileReader(configPath.toFile()))
        {
            WatcherConfig config = gson.fromJson(reader, WatcherConfig.class);
            
            if (config == null) {
                throw new IllegalStateException("Configuration file is empty or invalid structure");
            }

            // 감시 대상 경로 존재 확인
            validateAndCreateDir(config.getMonitorPath(), "monitorPath");

            // 감시 주기 값 확인
            int duration = config.getMonitorDurations();
            if (duration <= 0) {
                config.setMonitorDurations(60);
            }

            validateAndCreateDir(config.getLogfilePath(), "logfilePath");

            // 로그 파일 크기 최대 값(Mb) 확인
            int logSize = config.getLogFileMaxMSize();
            if (logSize <= 0) {
                config.setLogFileMaxMSize(5); // 기본 5MB
            }

            if (config.getCompress() != null && !config.getCompress().isEmpty()) 
            {

                WatcherConfig.Compress compressConfig = config.getCompress().get(0);
                try
                {
                    compressConfig.validate();

                    config.setCompressActive(compressConfig.isCompressEnabled());
                    config.setCompressFormat(compressConfig.getCompressFormat());
                    config.setJarLocation(compressConfig.getJarLocation());
                }
                catch (Exception e)
                {
                    config.setCompressActive(false);
                    throw new IllegalStateException("Invalid compress configuration : " + e.getMessage(), e);
                }
            }

            return config;
        }
        catch (java.io.FileNotFoundException e)
        {
            System.err.println("Error: configuration file '" + CONFIG_FILE + "'");
            throw e;
        }
    }

    private static void validateAndCreateDir(String pathStr, String label) throws IOException {
        if (pathStr == null || pathStr.isEmpty())
        {
            throw new IllegalStateException("Not exist " + label + " value");
        }

        Path path = Paths.get(pathStr);
        if (!Files.exists(path))
        {
            Files.createDirectories(path);
            System.out.println(label + " directory created : " + path.toAbsolutePath());
        }
    }
}