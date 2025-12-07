package com.reri72.dirbackup.compress;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.*;

public class ZipCompress implements Compress {

    private void addFileToZip(File file, String entryName, ZipOutputStream zos) throws IOException
    {
        System.out.println("압축 중 (파일): " + entryName);
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
    public void compress(String sourcePath, List<String> fileNames, String destPath) throws IOException
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
        String now = LocalDateTime.now().format(formatter);
        System.out.println(now);

        String zipFilePath = destPath+"/backup_" + now + ".zip";

        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
                ZipOutputStream zos = new ZipOutputStream(fos))
        {
            for (String fileName : fileNames)
            {
                File file = new File(fileName);

                if (file.isDirectory())
                {

                }
                else // 일반 파일
                {
                    addFileToZip(file, fileName, zos);
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