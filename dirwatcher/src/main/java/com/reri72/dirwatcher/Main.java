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
        int logSize;

        try {
            WatcherConfig config = ConfigLoader.loadConfig();

            System.out.println("-- config --");

            System.out.println(" monitorPath : "+ config.getmonitorPath());
            System.out.println(" monitorDurations : "+ config.getmonitorDurations() + " Sec");
            System.out.println(" logfilePath : "+ config.getlogfilePath());
            System.out.println(" logFileMaxMSize : " + config.getlogFileMaxMSize() + " MB");

            System.out.println("--------------------------\n");

            monPath = config.getmonitorPath();
            watchPath = Paths.get(monPath).toAbsolutePath();

            monDuration = config.getmonitorDurations();
            logPath = config.getlogfilePath();
            logSize = config.getlogFileMaxMSize();

            ChangeLogger logger = new FileChangeLogger(logPath, logSize);
            DirectoryWatcher watcher = new DirectoryWatcher(watchPath, logger, monDuration);

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
