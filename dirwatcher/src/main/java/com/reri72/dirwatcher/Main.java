package com.reri72.dirwatcher;

import com.reri72.dirwatcher.config.*;
import com.reri72.dirwatcher.logger.*;
import com.reri72.dirwatcher.watcher.*;
import com.reri72.dirwatcher.scheduler.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main( String[] args ) { 

        String monPath;
        Path watchPath;

        int monDuration;
        String logPath;
        int logSize;

        try {
            WatcherConfig config = ConfigLoader.loadConfig();

            System.out.println("-- config --");

            System.out.println(" monitorPath : "+ config.getMonitorPath());
            System.out.println(" monitorDurations : "+ config.getMonitorDurations() + " Sec");
            System.out.println(" logfilePath : "+ config.getLogfilePath());
            System.out.println(" logFileMaxMSize : " + config.getLogFileMaxMSize() + " MB");
            System.out.println(" isCompressEnabled : " + config.isCompressActive());
            if (config.isCompressActive())
            {
                System.out.println(" compressFormat : " + config.getCompressFormat());
                System.out.println(" jarLocation : " +  config.getJarLocation());
                System.out.println(" compressTime : " + config.getCompressTime());
                System.out.println(" targetPath : " + config.getTargetPath());
            }

            System.out.println("--------------------------\n");

            monPath = config.getMonitorPath();
            watchPath = Paths.get(monPath).toAbsolutePath();

            monDuration = config.getMonitorDurations();
            logPath = config.getLogfilePath();
            logSize = config.getLogFileMaxMSize();

            // 불변성 보장
            final ChangeLogger logger = new FileChangeLogger(logPath, logSize);
            final DirectoryWatcher watcher = new DirectoryWatcher(watchPath, logger, monDuration);

            BackupScheduler tempscheduler = null;
            if (config.isCompressActive())
            {
                tempscheduler = new BackupScheduler(
                    config.getJarLocation(),
                    config.getCompressTime(),
                    config.getCompressFormat(),
                    monPath,
                    config.getTargetPath(),
                    logger
                );
                tempscheduler.start();
            }

            final BackupScheduler scheduler = tempscheduler;
            Runtime.getRuntime().addShutdownHook(new Thread(()-> {
                if (scheduler != null)
                {
                    logger.logChange("[SCHEDULE]", "Stopping backup scheduler...");
                    scheduler.stop();
                }
                logger.logChange("[SCHEDULE]", "Stopping watcher scheduler...");
                watcher.stop();
            }));

            watcher.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }        
    }
}
