package com.reri72.dirbackup.compress;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public class TarCompress implements Compress {
    
    private Set<Path> getTopPaths(List<String> fileNames)
    {
        // Path 객체로 변환하고 절대경로로 존재 여부 다시 확인
        List<Path> allPaths = fileNames.stream()
                    .map(p -> Paths.get(p).toAbsolutePath().normalize())
                    .filter(Files::exists)
                    .collect(Collectors.toList());

        if (allPaths.isEmpty())
            return Collections.emptySet();
        
        // 오름차순 정렬
        List<Path> sortedPaths = allPaths.stream()
                                        .sorted(Comparator.comparing(Path::toString))
                                        .collect(Collectors.toList());
                                    
        // sortedPaths.stream().forEach(System.out::println);

        Set<Path> uniquePaths = new HashSet<>();
        for (Path curPath : sortedPaths)
        {
            boolean isSub = false;
            for (Path top : uniquePaths)
            {
                // 상위 하위 관계 확인해서 최상위 경로만 남기도록 함!
                if (curPath.startsWith(top))
                {
                    isSub = true;
                    break;
                }
            }

            if (isSub == false)
                uniquePaths.add(curPath);
        }

        // uniquePaths.stream().forEach(System.out::println);

        return uniquePaths;
    }
    
    private static void addFileToTar(TarArchiveOutputStream taos, Path file, Path baseDir) throws IOException
    {
        String entryName = baseDir.relativize(file).toString();
        if (Files.isDirectory(file))
        {
            TarArchiveEntry entry = new TarArchiveEntry(file.toFile(), entryName + "/");
            taos.putArchiveEntry(entry);
            taos.closeArchiveEntry();

            try (var stream = Files.list(file))
            {
                for (Path p : (Iterable<Path>) stream::iterator)
                    addFileToTar(taos, p, baseDir);
            }
        }
        else
        {
            TarArchiveEntry entry = new TarArchiveEntry(file.toFile(), entryName);
            taos.putArchiveEntry(entry);
            Files.copy(file, taos);
            taos.closeArchiveEntry();
        }
    }

    @Override
    public void compress(List<String> fileNames, String destPath) throws IOException
    {
        String now = getTimestamp();
        String tarFilePath = destPath+"/backup_" + now + ".tar.gz";

        try (FileOutputStream fos = new FileOutputStream(tarFilePath);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
            TarArchiveOutputStream taos = new TarArchiveOutputStream(gzos))
            {
                // 긴 이름의 파일도 지원하도록
                taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

                Set<Path> uniquePaths = getTopPaths(fileNames);
                Path currentDir = Paths.get("").toAbsolutePath().normalize();

                for (Path path : uniquePaths)
                {
                    addFileToTar(taos, path, currentDir);
                }
            }
    }
}