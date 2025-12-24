#!/bin/bash

DIST_DIR="./dist"
WATCHER_DIR="./dirwatcher"
BACKUP_DIR="./dirbackup"
TARGET_FILE="dirwatcher_package.tar.gz"

fail() {
    echo -e "\033[31m[ERROR] $1\033[0m"
    rm -rf "$DIST_DIR"
    exit 1
}

[ -d "$WATCHER_DIR" ] || fail "dirwatcher is not exist."
[ -d "$BACKUP_DIR" ] || fail "dirbackup is not exist."

echo "[clean project]"
rm -rf "$DIST_DIR"
rm -f "$TARGET_FILE"
mkdir -p "$DIST_DIR"
mkdir -p "$DIST_DIR"
echo "-----------------------------------------"

echo "[watcher build]"
cd "$WATCHER_DIR"
mvn clean package -DskipTests || fail "dirwatcher Maven build failed."

WATCHER_JAR=$(ls target/dirwatcher-runnable-*.jar 2>/dev/null)
[ -f "$WATCHER_JAR" ] || fail "dirwatcher JAR is not created."
cp "$WATCHER_JAR" "../$DIST_DIR/dirwatcher.jar"
cd ..
echo "-----------------------------------------"

echo "[backup build]"
cd "$BACKUP_DIR"
mvn clean package -DskipTests || fail "dirbackup Maven build failed."

BACKUP_JAR=$(ls target/dirbackup-runnable-*.jar 2>/dev/null)
[ -f "$BACKUP_JAR" ] || fail "dirbackup JAR is not created."
cp "$BACKUP_JAR" "../$DIST_DIR/dirbackup.jar"
cd ..
echo "-----------------------------------------"

echo "[copy configuration file]"
[ -f "$WATCHER_DIR/config.json" ] || fail "config.conf is not exist."
[ -f "README.md" ] || fail "README.md is not exist."

cp $WATCHER_DIR/config.json $DIST_DIR/
cp README.md $DIST_DIR/ 2>/dev/null
echo "-----------------------------------------"

echo "[tar compression]"
tar -czvf "$TARGET_FILE" -C "$DIST_DIR" . || fail "Failed compress"
echo "-----------------------------------------"

if [ -s "$TARGET_FILE" ]; then
    echo "========================================"
    echo " SUCCESS: $TARGET_FILE (Size: $(du -h $TARGET_FILE | cut -f1))"
    echo "========================================"
else
    fail "Compress Failed. (File is empty)"
fi
