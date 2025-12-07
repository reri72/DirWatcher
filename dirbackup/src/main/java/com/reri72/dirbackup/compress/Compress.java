package com.reri72.dirbackup.compress;

import java.io.IOException;
import java.util.List;

// 인터페이스
public interface Compress {
    void compress(List<String> fileNames, String destPath) throws IOException;
}