package com.reri72.dirwatcher.config;

import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.util.List;

public class WatcherConfig {

    private String monitorPath;
    private int monitorDurations = 60;
    private String logfilePath;
    private int logFileMaxMSize = 5;

    private List<Compress> compress;
    private boolean isCompressEnabled = false;
    private String compressFormat = "zip";
    private String jarLocation;

    public String getMonitorPath() { return monitorPath; }
    public int getMonitorDurations() { return monitorDurations; }
    public String getLogfilePath() { return logfilePath; }
    public int getLogFileMaxMSize() { return logFileMaxMSize; }
    public List<Compress> getCompress() { return compress; }

    public void setMonitorDurations(int value) { monitorDurations = value; }
    public void setLogFileMaxMSize(int value) { logFileMaxMSize = value; }
    public void setCompress(List<Compress> value) { this.compress = value; }

    public boolean isCompressActive() { return isCompressEnabled; }
    public String getCompressFormat() { return compressFormat; }
    public String getJarLocation() { return jarLocation; }
    public void setCompressActive(boolean value) { isCompressEnabled = value; }
    public void setCompressFormat(String value) { compressFormat = value; }
    public void setJarLocation(String value) { jarLocation = value; }

    public static class Compress {
        @SerializedName("isCompressEnabled")
        private boolean isCompressEnabled = false;
        private String compressFormat = "zip";
        private String jarLocation;

        public void validate() throws Exception
        {
            if (this.isCompressEnabled)
            {
                if (this.compressFormat == null || this.compressFormat.isEmpty())
                {
                    throw new Exception("compressFormat is null or empty");
                }

                if (this.jarLocation == null || this.jarLocation.isEmpty())
                {
                    throw new Exception("jarLocation is null or empty");
                }

                File jarFile = new File(this.jarLocation);
                if (!jarFile.exists() || !jarFile.isFile())
                {
                    throw new Exception("jarLocation file does not exist : " + this.jarLocation);
                }

                if (!jarFile.isFile())
                {
                    throw new Exception("jarLocation file is not exist : " + this.jarLocation);
                }

                if (!jarFile.canExecute())
                {
                    throw new Exception("jarLocation file is not executable : " + this.jarLocation);
                }
            }
        }

        public boolean isCompressEnabled() {
            return isCompressEnabled;
        }

        public String getCompressFormat() {
            return compressFormat;
        }
        public String getJarLocation() {
            return jarLocation;
        }
    }
}