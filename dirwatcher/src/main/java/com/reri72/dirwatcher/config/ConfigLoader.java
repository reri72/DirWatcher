package com.reri72.dirwatcher.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigLoader {
    private static final String CONFIG_FILE = "config.json";

    public static WatcherConfig loadConfig() throws IOException, JsonSyntaxException {
        Gson gson = new Gson();
        Path configPath = Paths.get(CONFIG_FILE);

        System.out.println("Configuration file : " + configPath.toAbsolutePath());

        try (FileReader reader = new FileReader(configPath.toFile()))
        {
            WatcherConfig config = gson.fromJson(reader, WatcherConfig.class);
            
            if (config == null) {
                throw new IllegalStateException("Configuration file is empty or invalid structure");
            }

            // 감시 대상 경로 존재 확인
            if (config.getmonitorPath() == null || config.getmonitorPath().isEmpty()) {
                throw new IllegalStateException("Not exist monitorPath value");
            }

            File monitorDir = new File(config.getmonitorPath());
            if (!monitorDir.exists() || !monitorDir.isDirectory()) {
            {
                boolean created = monitorDir.mkdirs();
                if (created)
                    System.out.println("monitorPath directory created: " + monitorDir.getAbsolutePath());
                else
                    throw new IllegalStateException("monitorPath(" + config.getmonitorPath() + ") is not created");
            }
            }

            // 감시 주기 값 확인
            int duration = config.getmonitorDurations();
            if (duration <= 0) {
                config.setmonitorDurations(60);
            }

            // 로그 파일 생성 경로 존재 확인
            if (config.getlogfilePath() == null || config.getlogfilePath().isEmpty()) {
                throw new IllegalStateException("Not exist logfilePath value");
            }

            File logDir = new File(config.getlogfilePath());
            if (!logDir.exists() || !logDir.isDirectory())
            {
                boolean created = logDir.mkdirs();
                if (created)
                    System.out.println("logfilePath directory created: " + logDir.getAbsolutePath());
                else
                    throw new IllegalStateException("logfilePath(" + config.getlogfilePath() + ") is not created");
            }

            // 로그 파일 크기 최대 값(Mb) 확인
            int logSize = config.getlogFileMaxMSize();
            if (logSize <= 0) {
                config.setlogFileMaxMSize(5); // 기본 5MB
            }

            return config;
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error: configuration file '" + CONFIG_FILE + "'");
            throw e;
        }
    }
}