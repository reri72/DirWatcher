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
            if (config.getMonitorPath() == null || config.getMonitorPath().isEmpty()) {
                throw new IllegalStateException("Not exist monitorPath value");
            }

            Path monitorPath = Paths.get(config.getMonitorPath());
            if (!Files.exists(monitorPath) || !Files.isDirectory(monitorPath))
            {
                try
                {
                    Files.createDirectories(monitorPath);
                    System.out.println("monitorPath directory created : " + monitorPath.toAbsolutePath());
                }
                catch (IOException e)
                {
                    throw new IllegalStateException("monitorPath directory creation failed : " 
                                                        + monitorPath.toAbsolutePath(), e);
                }
            }

            // 감시 주기 값 확인
            int duration = config.getMonitorDurations();
            if (duration <= 0) {
                config.setMonitorDurations(60);
            }

            // 로그 파일 생성 경로 존재 확인
            if (config.getLogfilePath() == null || config.getLogfilePath().isEmpty()) {
                throw new IllegalStateException("Not exist logfilePath value");
            }

            Path logDir = Paths.get(config.getLogfilePath());
            if (!Files.exists(logDir))
            {
                try
                {
                    Files.createDirectories(logDir);
                    System.out.println("logfilePath directory created : " + logDir.toAbsolutePath());
                }
                catch (IOException e)
                {
                    throw new IllegalStateException("logfilePath(" + config.getLogfilePath() + ") is not created");
                }
            }

            // 로그 파일 크기 최대 값(Mb) 확인
            int logSize = config.getLogFileMaxMSize();
            if (logSize <= 0) {
                config.setLogFileMaxMSize(5); // 기본 5MB
            }

            return config;
        }
        catch (java.io.FileNotFoundException e)
        {
            System.err.println("Error: configuration file '" + CONFIG_FILE + "'");
            throw e;
        }
    }
}