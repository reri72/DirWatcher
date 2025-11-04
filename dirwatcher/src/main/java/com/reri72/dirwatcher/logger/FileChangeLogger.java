
import com.reri72.dirwatcher.logger.ChangeLogger;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileChangeLogger implements ChangeLogger {
    private final String logFile;

    public FileChangeLogger(String logFile)
    {
        this.logFile = logFile;
    }

    @Override
    public synchronized void logChange(String eventType, String filePath)
    {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String line = String.format("%s %-10s %s%n", timestamp, eventType, filePath);

        try (FileWriter fw = new FileWriter(logFile, true))
        {
            fw.write(line);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.print(line);
    }
}