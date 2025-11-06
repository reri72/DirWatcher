package com.reri72.dirwatcher.watcher;

import com.reri72.dirwatcher.logger.ChangeLogger;

import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;

public class DirectoryWatcher {

    private final Path path;
    private final ChangeLogger logger;
    private volatile boolean running = true;

    public DirectoryWatcher(Path path, ChangeLogger logger)
    {
        this.path = path;
        this.logger = logger;
    }

    public void start()
    {
        try (WatchService watchService = FileSystems.getDefault().newWatchService())
        {
            path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            System.out.println("감시 경로 : " + path);

            while (running)
            {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents())
                {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path fileName = (Path) event.context();
                    Path fullPath = path.resolve(fileName);
                    logger.logChange(kind.name(), fullPath.toString());
                }
                key.reset();
            }

        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void stop()
    {
        running = false;
    }
}