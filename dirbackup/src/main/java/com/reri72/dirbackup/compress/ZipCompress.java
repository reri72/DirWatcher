package com.reri72.dirbackup.compress;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipCompress implements Compress {

    private static final Logger log = LoggerFactory.getLogger(ZipCompress.class);

    private void addFileToZip(Path sourcePath, String entryName, ZipOutputStream zos) throws IOException
    {
        ZipEntry zipEntry = new ZipEntry(entryName);
            
        zos.putNextEntry(zipEntry);
        Files.copy(sourcePath, zos);

        zos.closeEntry();
    }

    @Override
    public void compress(List<String> fileNames, String destPath) throws IOException
    {
        String now = getTimestamp();
        Path zipFilePath = Path.of(destPath, "backup_" + now + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath)))
        {
            log.info("target size : {}, output : {}", fileNames.size(), zipFilePath);

            for (String fileName : fileNames)
            {
                Path path = Path.of(fileName);
                if (Files.isDirectory(path))
                {
                    String dirName = fileName.endsWith("/") ? fileName : fileName + "/";
                    zos.putNextEntry(new ZipEntry(dirName));
                    zos.closeEntry();
                }
                else if (Files.isRegularFile(path))
                {
                    addFileToZip(path, fileName, zos);
                }
            }
        }
        catch (IOException e)
        {
            log.error("zip compress error : {}", zipFilePath, e);
            throw e;
        }
    }
}