package com.reri72.dirbackup.compress;

import java.io.*;
import java.nio.file.*;
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
    }
}