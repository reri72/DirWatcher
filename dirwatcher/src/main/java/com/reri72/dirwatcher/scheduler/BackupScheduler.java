package com.reri72.dirwatcher.scheduler;

import com.reri72.dirwatcher.logger.ChangeLogger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupScheduler {

    private final ChangeLogger logger;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    private final String jarPath;
    private final int atTime;
    private final String[] args;

    public BackupScheduler(String jarPath, int atTime, String format, String monitorPath, String targetPath, ChangeLogger logger) {
        this.jarPath = jarPath;
        this.atTime = atTime;

        this.args = new String[] {
            format, monitorPath, targetPath
        };

        this.logger = logger;
    }

    public void start()
    {
        long initialDelay = calculateDelay(atTime);
        long oneDayInSeconds = 24 * 60 * 60;

        scheduler.scheduleAtFixedRate(() -> {
            logger.logChange("[SCHEDULE]", "Starting backup executor");
            try {
                ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarPath, args[0], args[1], args[2]);
                String fullCommand = String.join(" ", pb.command());

                logger.logChange("[SCHEDULE]", "Executing command: " + fullCommand);

                pb.inheritIO();

                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    logger.logChange("[SCHEDULE]", "Backup completed successfully.");
                } else {
                    logger.logChange("[ERROR]", "Backup process exited with code: " + exitCode);
                }

            }
            catch (Exception e)
            {
                logger.logChange("[ERROR]", "Backup execution failed: " + e.getMessage());
            }
        }, initialDelay, oneDayInSeconds, TimeUnit.SECONDS);
    }

    private long calculateDelay(int hour)
    {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(hour).withMinute(0).withSecond(0).withNano(0);

        // 현재시각이 해당 시각을 지났으면 내일 함
        if (now.isAfter(nextRun))
            nextRun = nextRun.plusDays(1);

        Duration duration = Duration.between(now, nextRun);

        return duration.getSeconds();
    }

    public void stop() {
        scheduler.shutdown();
    }
}