package com.reri72.dirbackup;

import com.reri72.dirbackup.compress.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;
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

        try (Stream<Path> paths = Files.walk(Paths.get(sourcePath)))
        {
            List<String> pathList = paths
                            .map(Path::toString)
                            .collect(Collectors.toList());
            pathList.forEach(System.out::println);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void errMessage(String msg)
    {
        System.out.println(msg);
        System.exit(1);
    }
}