package com.reri72.dirwatcher.logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

// java.nio.file.StandardOpenOption
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class FileChangeLogger implements ChangeLogger {

    private final Path logFilePath;
    private final Map<String, Long> lastProcessedTime = new ConcurrentHashMap<>();
    private final long debounceMs = 1000;

    private final long maxFileSizeBytes;
    private final int maxBackupFiles = 9;   // .9 까지 로테이트

    public FileChangeLogger(String logFile, int logSize)
    {
        Path givenPath = Paths.get(logFile).toAbsolutePath();
        if (Files.isDirectory(givenPath))
            this.logFilePath = givenPath.resolve("dirwatch.log");
        else
            this.logFilePath = givenPath;
        
        maxFileSizeBytes = logSize * 1024L * 1024L;

        ensureLogFileExists();
    }

    @Override
    public synchronized void logChange(String eventType, String content)
    {
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastProcessedTime.get(content);

        if (lastTime != null && (currentTime - lastTime) < debounceMs)
            return;

        try
        {
            rotateLogfile();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String line = String.format("%s %-10s %s%n", timestamp, eventType, content);

            try (BufferedWriter writer = Files.newBufferedWriter(logFilePath, CREATE, APPEND))
            {
                writer.write(line);
                writer.flush();
            }

            System.out.print(line);

            lastProcessedTime.put(content, currentTime);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void rotateLogfile() throws IOException
    {
        if (!Files.exists(logFilePath))
            return;

        long size = Files.size(logFilePath);
        if (size < maxFileSizeBytes)
            return;
            
        for (int i = maxBackupFiles; i >= 1; i--)
        {
            Path src = Paths.get(logFilePath.toString() + "." + i);
            Path dest = Paths.get(logFilePath.toString() + "." + (i + 1));

            if (Files.exists(dest))
                Files.delete(dest);

            if (Files.exists(src))
                Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        // 현재 로그파일 .1 로 변경
        Path firstBackup = Paths.get(logFilePath.toString() + ".1");
        Files.move(logFilePath, firstBackup, StandardCopyOption.REPLACE_EXISTING);

        Files.createFile(logFilePath);
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