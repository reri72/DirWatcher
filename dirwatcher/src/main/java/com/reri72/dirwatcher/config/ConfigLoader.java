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
                throw new IllegalStateException("MonitorPath(" + config.getMonitorPath() + ") is not exist");
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
            if (!logDir.exists() || !logDir.isDirectory()) {
                throw new IllegalStateException("LogfilePath(" + config.getLogfilePath() + ") is not exist");
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