package com.reri72.dirwatcher.watcher;

import com.reri72.dirwatcher.logger.ChangeLogger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import static java.nio.file.StandardWatchEventKinds.*;

public class DirectoryWatcher {

    private final Path path;
    private final ChangeLogger logger;
    private final int monitorDuration;

    private volatile boolean running = true;

    public DirectoryWatcher(Path path, ChangeLogger logger, int monitorDuration)
    {
        this.path = path;
        this.logger = logger;
        this.monitorDuration = monitorDuration;
    }
    
    private void register(Path dir, WatchService watchService) throws IOException {
        dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        System.out.println("Registered directory : " + dir);
    }

    private void registerAll(final Path start, final WatchService watchService) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) throws IOException {
                register(dir, watchService);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void start()
    {
        try (WatchService watchService = FileSystems.getDefault().newWatchService())
        {
            registerAll(path, watchService);
            System.out.println("감시 경로 : " + path);

            while (running)
            {
                WatchKey key = watchService.poll(monitorDuration, TimeUnit.SECONDS);
                if (key == null)
                {
                    logger.logChange("[MONITOR]", "Timeout " + monitorDuration + " Second(s)");
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents())
                {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW)
                        continue;

                    Path fileName = (Path) event.context();
                    Path parentPath = (Path) key.watchable();
                    Path fullPath = parentPath.resolve(fileName);

                    if (kind == ENTRY_MODIFY && Files.isDirectory(fullPath, LinkOption.NOFOLLOW_LINKS))
                        continue;

                    if (kind == ENTRY_CREATE)
                    {
                        if (Files.isDirectory(fullPath, LinkOption.NOFOLLOW_LINKS))
                        {
                            registerAll(fullPath, watchService);
                        }
                    }

                    logger.logChange(kind.name(), fullPath.toString());
                }
                
                // 접근 불가하면 감시 중지
                if (!key.reset())
                    break;
            }
        }
        catch (IOException | InterruptedException e)
        {
            if (running)
                e.printStackTrace();
            else
                System.out.println("dirwatcher stopped.");
        }
    }

    public void stop()
    {
        running = false;
    }
}