package com.reri72.dirbackup.compress;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public class TarCompress implements Compress {
    
    private static void addFileToTar(TarArchiveOutputStream taos, Path file, Path baseDir) throws IOException
    {
        String entryName = baseDir.relativize(file).toString();
        TarArchiveEntry entry = new TarArchiveEntry(file.toFile(), entryName);
        taos.putArchiveEntry(entry);

        Files.copy(file, taos);
        taos.closeArchiveEntry();
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

                for (String pathStr : fileNames)
                {
                    Path path = Paths.get(pathStr);
                    File file = path.toFile();

                    if (file.isDirectory())
                    {

                    }
                    else
                    {
                        addFileToTar(taos, path, path.getParent());
                    }
                }
            }
    }
}