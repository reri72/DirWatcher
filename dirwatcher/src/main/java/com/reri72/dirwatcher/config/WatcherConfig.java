package com.reri72.dirwatcher.config;

public class WatcherConfig {

    private String monitorPath;

    private int monitorDurations;

    private String logfilePath;

    private int logFileMaxMSize;

    public String getMonitorPath() {
        return monitorPath;
    }

    public int getMonitorDurations() {
        return monitorDurations;
    }

    public void setMonitorDurations(int value) {
        monitorDurations = value;
    }

    public String getLogfilePath() {
        return logfilePath;
    }

    public int getLogFileMaxMSize() {
        return logFileMaxMSize;
    }

    public void setLogFileMaxMSize(int value) {
        logFileMaxMSize = value;
    }
}