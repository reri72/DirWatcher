package com.reri72.dirwatcher.config;

public class WatcherConfig {

    private String monitorPath;

    private int monitorDurations;

    private String logfilePath;

    private int logFileMaxMSize;

    public String getmonitorPath() {
        return monitorPath;
    }

    public int getmonitorDurations() {
        return monitorDurations;
    }

    public void setmonitorDurations(int value) {
        monitorDurations = value;
    }

    public String getlogfilePath() {
        return logfilePath;
    }

    public int getlogFileMaxMSize() {
        return logFileMaxMSize;
    }

    public void setlogFileMaxMSize(int value) {
        logFileMaxMSize = value;
    }
}