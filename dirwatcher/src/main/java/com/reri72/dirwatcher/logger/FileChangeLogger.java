package com.reri72.dirwatcher.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileChangeLogger implements ChangeLogger {
    private final Path logFilePath;

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