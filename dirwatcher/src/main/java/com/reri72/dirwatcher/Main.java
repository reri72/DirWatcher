package com.reri72.dirwatcher;

import com.reri72.dirwatcher.config.WatcherConfig;
import com.reri72.dirwatcher.config.ConfigLoader;

public class Main {
    public static void main( String[] args ) {
        System.out.println( "Hello World!" );

        try {
            WatcherConfig config = ConfigLoader.loadConfig();

            System.out.println("-- config --");

            System.out.println(" MonitorPath : "+ config.getMonitorPath());
            System.out.println(" MonitorDurations : "+ config.getMonitorDurations());

            System.out.println("--------------------------\n");
        }
        catch (Exception e)
        {
            System.err.println("Error : Failed to read or parse config.json");
            e.printStackTrace();
        }
    }
}
