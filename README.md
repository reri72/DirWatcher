# 🚀 DirWatcher & Backup System
A comprehensive solution for real-time directory monitoring (dirwatcher) and schedule-based automated backup (dirbackup).


## 📂 Project Structure & Path Dependencies
This system operates based on Relative Paths. For the monitoring and backup processes to function correctly, all components must be co-located in the execution environment.

```text
dist/
├── dirwatcher.jar       # Main service (Execution point)
├── dirbackup.jar        # Backup module (Called by dirwatcher.jar)
├── config.json          # Configuration (Must be in the same folder as dirwatcher.jar)
├── README.md            # Documentation
```

* Configuration Access: dirwatcher.jar is designed to look for **config.json** in its current working directory.
* Prevent Infinite Loops: NEVER set *logfilePath* or *targetPath* inside the monitorPath.
* Path Alignment: Ensure the *monitorPath* and *targetPath* are accessible and have proper read/write permissions from the execution directory.


## ⚙️ Configuration (config.json)
Example of a configuration where logs and backups are stored outside the monitored directory.
```text
{
    "monitorPath": "/home/testdir",
    "monitorDurations": 5,
    "logfilePath": "/home/logs",
    "logFileMaxMSize": 5,
    "compress": [{
        "isCompressEnabled": false,
        "compressFormat": "zip",
        "jarLocation": "./dirbackup-runnable-1.0-SNAPSHOT.jar.jar",
        "compressTime": 3,
        "targetPath": "/home/compressed"
    }]
}
```


## 🚀 How to Run
Follow these steps:

1. Build the project using the provided script.
   ```sh
   ]# ./build_dist.sh
   ```
   
2. Navigate to the distribution folder :
   ```sh
   ]# cd dist
   ```

3. Run the application
   ```sh
   ]# chmod +x dirbackup.jar
   ]# java -jar dirwatcher.jar
   ```


## 🛠 Tech Stack
* Runtime: Java 11
* Libraries: Gson, Apache Commons Compress, Logback/SLF4J
* License: MIT License

