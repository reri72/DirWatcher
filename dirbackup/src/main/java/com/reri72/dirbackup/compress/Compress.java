package com.reri72.dirbackup.compress;

import java.io.IOException;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 인터페이스
public interface Compress {
    public void compress(List<String> fileNames, String destPath) throws IOException;

    // 디폴트 메서드 (공통 사용)
    default String getTimestamp()
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
        return LocalDateTime.now().format(formatter);
    }
}