package com.reri72.dirbackup.compress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.*;

public class ZipCompress implements Compress {

    @Override
    public void compress(String sourcePath, List<String> fileNames, String destPath) throws IOException
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
        String now = LocalDateTime.now().format(formatter);
        System.out.println(now);

        String zipFilePath = destPath+"/backup_" + now + ".zip";

        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
                ZipOutputStream zos = new ZipOutputStream(fos))
        {
            byte[] buffer = new byte[1024];

            for (String fileName : fileNames)
            {
                try (FileInputStream fis = new FileInputStream(fileName))
                {
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);

                    int byteRead;
                    while ((byteRead = fis.read(buffer)) != -1)
                    {
                        zos.write(buffer, 0, byteRead);
                    }

                    zos.closeEntry();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw e;
        }
    }

}