package com.reri72.dirwatcher.config;

import com.google.gson.annotations.SerializedName;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private int compressTime = 2;
    private String targetPath = "/tmp";

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
    public int getCompressTime() { return compressTime;}
    public String getTargetPath() { return targetPath; }
    public void setCompressActive(boolean value) { isCompressEnabled = value; }
    public void setCompressFormat(String value) { compressFormat = value; }
    public void setJarLocation(String value) { jarLocation = value; }
    public void setCompressTime(int value) { compressTime = value; }
    public void setTargetPath(String value) { targetPath = value; }

    public static class Compress {
        @SerializedName("isCompressEnabled")
        private boolean isCompressEnabled = false;
        private String compressFormat;
        private String jarLocation;
        private int compressTime;
        private String targetPath;

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

                Path jarFile = Path.of(this.jarLocation); // 요즘 이렇게 쓰인다고 함
                if (!Files.exists(jarFile) || !Files.isRegularFile(jarFile))
                {
                    throw new Exception("jarLocation file does not exist : " + this.jarLocation);
                }

                if (!Files.isExecutable(jarFile))
                {
                    throw new Exception("jarLocation file is not executable : " + this.jarLocation);
                }

                if (this.compressTime < 0 || this.compressTime > 23)
                {
                    throw new Exception("compressTime is out of range (0-23) : " + this.compressTime);
                }

                if (this.targetPath == null || this.targetPath.isEmpty())
                {
                    throw new Exception("targetPath is null or empty");
                }

                Path path = Paths.get(this.targetPath);
                if (!Files.exists(path) || !Files.isDirectory(path))
                {
                    Files.createDirectories(path);
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

        public int getCompressTime() {
            return compressTime;
        }

        public String getTargetPath() {
            return targetPath;
        }
    }
}