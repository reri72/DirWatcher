package com.reri72.dirbackup.compress;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.zip.*;

public class ZipCompress implements Compress {

    @Override
    public void compress(String sourcePath, List<String> fileNames) throws IOException
    {
        System.out.println(sourcePath);
    }
}