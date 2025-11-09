package com.reri72.dirwatcher.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class FileChangeLogger implements ChangeLogger {

    private final Path logFilePath;
    private final Map<String, Long> lastProcessedTime = new ConcurrentHashMap<>();
    private final long debounceMs = 1000;

    public FileChangeLogger(String logFile)
    {
        Path givenPath = Paths.get(logFile).toAbsolutePath();
        if (Files.isDirectory(givenPath))
            this.logFilePath = givenPath.resolve("dirwatch.log");
        else
            this.logFilePath = givenPath;

        ensureLogFileExists();
    }

    @Override
    public synchronized void logChange(String eventType, String filePath)
    {
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastProcessedTime.get(filePath);

        if (lastTime != null && (currentTime - lastTime) < debounceMs)
            return;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String line = String.format("%s %-10s %s%n", timestamp, eventType, filePath);

        try (FileWriter fw = new FileWriter(logFilePath.toFile(), true))
        {
            fw.write(line);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.print(line);

        lastProcessedTime.put(filePath, currentTime);
    }

    private void ensureLogFileExists()
    {
        try {
            Path parentDir = logFilePath.getParent();

            if (parentDir != null && !Files.exists(parentDir))
                Files.createDirectories(parentDir);
            if (!Files.exists(logFilePath))
                Files.createFile(logFilePath);
        }
        catch (IOException e)
        {
            System.err.println("Failed to create log file: " + e.getMessage());
        }
    }
}