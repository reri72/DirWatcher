package com.reri72.dirwatcher.watcher;

import com.reri72.dirwatcher.logger.ChangeLogger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.nio.file.StandardWatchEventKinds.*;

public class DirectoryWatcher {

    private final Path path;
    private final ChangeLogger logger;
    private final int monitorDuration;

    private volatile boolean running = true;

    private final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
    private final Map<Path, Long> fileSizes = new ConcurrentHashMap<>();
    private final Map<Path, String> fileOwners = new ConcurrentHashMap<>();

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock writeLock = rwLock.writeLock();

    private WatchService watchService;

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

                String owner = getFileOwner(dir);
                fileOwners.put(dir, owner);

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes Attrs) throws IOException {
                fileSizes.put(path, Attrs.size());

                String owner = getFileOwner(path);
                fileOwners.put(path, owner);

                return FileVisitResult.CONTINUE;
            }
        });
    }

    private String getFileOwner(Path path) {
        try {
            FileOwnerAttributeView ownerView = Files.getFileAttributeView(path, FileOwnerAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            if (ownerView != null)
            {
                UserPrincipal owner = ownerView.getOwner();
                return owner != null ? owner.getName() : "Unkown";
            }
        } catch (IOException e) {
            return "OwnerCheckError";
        }
        return "NotSupported";
    }

    public void start()
    {
        try (WatchService ws = FileSystems.getDefault().newWatchService())
        {
            this.watchService = ws;
            
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
                    
                    keys.clear();
                    fileSizes.clear();
                    fileOwners.clear();

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

                    String ownerInfo = "NA";
                    long currentSize = -1L;
                    long previousSize = -1L;
                    String sizeChangeInfo = "";

                    try
                    {
                        if (kind == ENTRY_CREATE)
                        {
                            // 초기 크기 및 소유자 기록
                            currentSize = Files.size(fullPath);
                            ownerInfo = getFileOwner(fullPath);
                            
                            if (Files.isDirectory(fullPath, LinkOption.NOFOLLOW_LINKS))
                            {
                                registerAll(fullPath, watchService);
                            }
                            else
                            {
                                fileSizes.put(fullPath, currentSize);
                                fileOwners.put(fullPath, ownerInfo);
                            }
                        }

                        if (kind == ENTRY_DELETE)
                        {
                            ownerInfo = fileOwners.getOrDefault(fullPath, " (Deleted)");
                            fileSizes.remove(fullPath);
                            fileOwners.remove(fullPath);
                        }

                        if (kind == ENTRY_MODIFY)
                        {
                            if (Files.isDirectory(fullPath, LinkOption.NOFOLLOW_LINKS))
                            {
                                // 디렉토리 수정 이벤트 무시
                            }
                            else
                            {
                                // 최종 소유자 확인
                                ownerInfo = getFileOwner(fullPath);
                                fileOwners.put(fullPath, ownerInfo);

                                previousSize = fileSizes.getOrDefault(fullPath, -1L);
                                currentSize = Files.size(fullPath);
                                fileSizes.put(fullPath, currentSize);

                                if (previousSize != -1L)
                                {
                                    long diffSize = currentSize - previousSize;
                                    sizeChangeInfo = String.format(", Change Size: %+d bytes (from %d to %d)", diffSize, previousSize, currentSize);
                                }
                                else
                                {
                                    sizeChangeInfo = String.format(", Current Size: %d bytes", currentSize);
                                }
                            }
                        }
                    }
                    catch (NoSuchFileException e)
                    {
                        // 파일,디렉토리가 이미 삭제되었거나 이동된 경우
                        System.err.println("[WARN] " + fullPath + " : " + e.getMessage());
                        logger.logChange("[WARN]", fullPath + " does not exist");
                        
                        fileSizes.remove(fullPath);
                        fileOwners.remove(fullPath);

                        continue;
                    }
                    catch (IOException e)
                    {
                        System.err.println("[ERROR] " + fullPath + " : " + e.getMessage());
                        logger.logChange("[ERROR]", fullPath + " : " + e.getMessage());
                        continue;
                    }

                    String logMessage = String.format("%s, Owner : %s%s", fullPath.toString(), ownerInfo, sizeChangeInfo);
                    logger.logChange(kind.name(), logMessage);
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
        catch (ClosedWatchServiceException e)
        {
            if (running)
                logger.logChange("[ERROR]", "WatchService closed unexpectedly");
        }
        finally
        {
            this.watchService = null;
        }
    }

    public void stop()
    {
        running = false;
        if (watchService != null)
        {
            try
            {
                watchService.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}