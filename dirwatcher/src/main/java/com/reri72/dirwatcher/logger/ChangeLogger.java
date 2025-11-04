package com.reri72.dirwatcher.logger;

// 인터페이스
public interface ChangeLogger {
    void logChange(String eventType, String filePath);
}