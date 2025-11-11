package com.reri72.dirwatcher.config;

public class WatcherConfig {

    private String MonitorPath;

    private int MonitorDurations;

    private String LogfilePath;

    private int LogFileMaxMSize;

    public String getMonitorPath() {
        return MonitorPath;
    }

    public int getMonitorDurations() {
        return MonitorDurations;
    }

    public void setMonitorDurations(int value) {
        MonitorDurations = value;
    }

    public String getLogfilePath() {
        return LogfilePath;
    }

    public int getLogFileMaxMSize() {
        return LogFileMaxMSize;
    }

    public void setLogFileMaxMSize(int value) {
        LogFileMaxMSize = value;
    }
}