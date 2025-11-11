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
            if (config.getMonitorPath() == null || config.getMonitorPath().isEmpty()) {
                throw new IllegalStateException("Not exist MonitorPath value");
            }

            File monitorDir = new File(config.getMonitorPath());
            if (!monitorDir.exists() || !monitorDir.isDirectory()) {
            {
                boolean created = monitorDir.mkdirs();
                if (created)
                    System.out.println("MonitorPath directory created: " + monitorDir.getAbsolutePath());
                else
                    throw new IllegalStateException("MonitorPath(" + config.getMonitorPath() + ") is not created");
            }
            }

            // 감시 주기 값 확인
            int duration = config.getMonitorDurations();
            if (duration <= 0) {
                config.setMonitorDurations(60);
            }

            // 로그 파일 생성 경로 존재 확인
            if (config.getLogfilePath() == null || config.getLogfilePath().isEmpty()) {
                throw new IllegalStateException("Not exist LogfilePath value");
            }

            File logDir = new File(config.getLogfilePath());
            if (!logDir.exists() || !logDir.isDirectory())
            {
                boolean created = logDir.mkdirs();
                if (created)
                    System.out.println("LogfilePath directory created: " + logDir.getAbsolutePath());
                else
                    throw new IllegalStateException("LogfilePath(" + config.getLogfilePath() + ") is not created");
            }

            // 로그 파일 크기 최대 값(Mb) 확인
            int logSize = config.getLogFileMaxMSize();
            if (logSize <= 0) {
                config.setLogFileMaxMSize(5); // 기본 5MB
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