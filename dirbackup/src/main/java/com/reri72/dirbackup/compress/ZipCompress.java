package com.reri72.dirbackup.compress;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipCompress implements Compress {

    private static final Logger log = LoggerFactory.getLogger(ZipCompress.class);

    private void addFileToZip(File file, String entryName, ZipOutputStream zos) throws IOException
    {
        try (FileInputStream fis = new FileInputStream(file))
        {
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int byteRead;
            while ((byteRead = fis.read(buffer)) != -1)
            {
                zos.write(buffer, 0, byteRead);
            }

            zos.closeEntry();
        }
    }

    @Override
    public void compress(List<String> fileNames, String destPath) throws IOException
    {
        String now = getTimestamp();
        String zipFilePath = destPath+"/backup_" + now + ".zip";

        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
                ZipOutputStream zos = new ZipOutputStream(fos))
        {
            log.info("target size : {}, output : {}", fileNames.size(), zipFilePath);

            for (String fileName : fileNames)
            {
                File file = new File(fileName);
                if (file.isDirectory())
                {
                    // 디렉토리는 이름이 / 로 끝나도록 처리해야 오류가 안남..
                    String dirName = fileName.endsWith("/") ? fileName : fileName + "/";
                    ZipEntry zipEntry = new ZipEntry(dirName);
                    zos.putNextEntry(zipEntry);
                    zos.closeEntry();
                }
                else if (file.isFile())
                {
                    addFileToZip(file, fileName, zos);
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