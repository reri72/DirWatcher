package com.reri72.dirwatcher;

import com.reri72.dirwatcher.config.*;
import com.reri72.dirwatcher.logger.*;
import com.reri72.dirwatcher.watcher.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main( String[] args ) { 

        String monPath;
        Path watchPath;

        int monDuration;
        String logPath;

        try {
            WatcherConfig config = ConfigLoader.loadConfig();

            System.out.println("-- config --");

            System.out.println(" MonitorPath : "+ config.getMonitorPath());
            System.out.println(" MonitorDurations : "+ config.getMonitorDurations());
            System.out.println(" LogfilePath : "+ config.getLogfilePath());

            System.out.println("--------------------------\n");

            monPath = config.getMonitorPath();
            watchPath = Paths.get(monPath).toAbsolutePath();

            monDuration = config.getMonitorDurations();
            logPath = config.getLogfilePath();

            ChangeLogger logger = new FileChangeLogger(logPath);
            DirectoryWatcher watcher = new DirectoryWatcher(watchPath, logger);

            Runtime.getRuntime().addShutdownHook(new Thread(watcher::stop));
            watcher.start();
        }
        catch (Exception e)
        {
            System.err.println("Error : Failed to read or parse config.json");
            e.printStackTrace();
            System.exit(0);
        }        
    }
}
