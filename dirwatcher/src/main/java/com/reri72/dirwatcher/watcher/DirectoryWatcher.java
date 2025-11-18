package com.reri72.dirwatcher.watcher;

import com.reri72.dirwatcher.logger.ChangeLogger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.nio.file.StandardWatchEventKinds.*;

public class DirectoryWatcher {

    private final Path path;
    private final ChangeLogger logger;
    private final int monitorDuration;

    private volatile boolean running = true;

    private final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock writeLock = rwLock.writeLock();

    public DirectoryWatcher(Path path, ChangeLogger logger, int monitorDuration)
    {
        this.path = path;
        this.logger = logger;
        this.monitorDuration = monitorDuration;
    }
    
    // 단일 등록
    private void register(Path dir, WatchService watchService) throws IOException {
        writeLock.lock();
        try
        {
            WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            keys.put(key, dir);
            System.out.println("Registered directory : " + dir);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    // 재귀적 등록
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
            System.out.println("Monitor : " + path);
            logger.logChange("[MONITOR]", "Started monitoring " + path.toString());

            while (running)
            {
                // 최상위 경로가 삭제되면 이벤트 감지가 아예 안되므로
                // 경로생성과 이벤트 등록을 다시 해준다.
                if (!Files.exists(path))
                {
                    logger.logChange("[WARN]", "Main path deleted");
                    
                    Files.createDirectories(path);
                    registerAll(path, watchService);
                    logger.logChange("[MONITOR]", "Main path recreated and re-registered");
                    continue;
                }

                WatchKey key = watchService.poll(monitorDuration, TimeUnit.SECONDS);
                if (key == null)
                {
                    logger.logChange("[MONITOR]", "Timeout " + monitorDuration + " Second(s)");
                    continue;
                }

                Path dir = keys.get(key);
                if (dir == null)
                {
                    logger.logChange("[MONITOR]", "skipping events : " + key);
                    key.reset();
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents())
                {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW)
                        continue;

                    Path fileName = (Path) event.context();
                    Path parentPath = dir; // 맵에서 가져온 path 사용
                    Path fullPath = parentPath.resolve(fileName); // 이벤트가 발생한 전체 경로

                    try
                    {
                        if (kind == ENTRY_CREATE)
                        {
                            if (Files.isDirectory(fullPath, LinkOption.NOFOLLOW_LINKS))
                            {
                                registerAll(fullPath, watchService);
                            }
                        }

                        if (kind == ENTRY_DELETE) {}
                        if (kind == ENTRY_MODIFY && Files.isDirectory(fullPath, LinkOption.NOFOLLOW_LINKS)) {}
                    }
                    catch (NoSuchFileException e)
                    {
                        // 파일,디렉토리가 이미 삭제되었거나 이동된 경우
                        System.err.println("[WARN] " + fullPath + " : " + e.getMessage());
                        logger.logChange("[WARN]", fullPath + " does not exist");
                        continue;
                    }
                    catch (IOException e)
                    {
                        System.err.println("[ERROR] " + fullPath + " : " + e.getMessage());
                        logger.logChange("[ERROR]", fullPath + " : " + e.getMessage());
                        continue;
                    }

                    logger.logChange(kind.name(), fullPath.toString());
                }
                
                if (!key.reset())
                {
                    keys.remove(key);
                }
            }
        }
        catch (IOException | InterruptedException e)
        {
            if (running)
            {
                e.printStackTrace();
                logger.logChange("[ERROR]", e.getMessage());
            }
            else
            {
                logger.logChange("[MONITOR]", "dirwatcher stopped.");
                System.out.println("dirwatcher stopped.");
            }
        }
    }

    public void stop()
    {
        running = false;
    }
}