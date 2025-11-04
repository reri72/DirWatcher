package com.reri72.dirwatcher.config;

public class WatcherConfig {

    private String MonitorPath;

    private int MonitorDurations;

    private String LogfilePath;

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
}