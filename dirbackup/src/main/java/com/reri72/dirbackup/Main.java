package com.reri72.dirbackup;

import com.reri72.dirbackup.compress.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main 
{
    public static void main( String[] args )
    {
        if (args.length != 3)
            errMessage("Require 3 arguments");

        String type = args[0];
        String sourcePath = args[1];
        String destPath = args[2];

        existPath(sourcePath, true);
        existPath(destPath, false);

        List<String> pathList = sourcePathValid(sourcePath);
        pathList.forEach(System.out::println);  

        if (type.equalsIgnoreCase("zip"))
        {
            System.out.println("ZIP!!!");
            ZipCompress zc = new ZipCompress();
            try
            {
                zc.compress(sourcePath, pathList, destPath);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static List<String> sourcePathValid(String src)
    {
        List<String> pathList = new ArrayList<>();
        Path rootPath = Paths.get(src);

        try (Stream<Path> paths = Files.walk(Paths.get(src)))
        {
            pathList = paths
                            .filter(path -> !path.equals(rootPath))
                            .map(Path::toString)
                            .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            errMessage("["+ src + "]" + " is invalid path");
        }

        return pathList;
    }

    public static void existPath(String target, boolean isSrc)
    {
        Path path = Paths.get(target);
        if (Files.notExists(path))
        {
            if (isSrc)
            {
                errMessage("["+ target + "]" + " is not exist");
            }
            else
            {
                System.out.println("["+ target + "]" + " is not exist");
                try
                {
                    Files.createDirectories(path);
                    System.out.println("["+ target + "]" + " is created");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    errMessage("Failed to create directory ["+ target + "]");
                }
            }
        }
    }

    public static void errMessage(String msg)
    {
        System.out.println(msg);
        System.exit(1);
    }
}