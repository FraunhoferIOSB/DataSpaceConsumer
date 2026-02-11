#!/bin/bash

# Define source and destination folders

SOURCE_FOLDER="extensions"
DESTINATION_FOLDER="framework/extensions"

# Check if source folder exists

if [ ! -d "$SOURCE_FOLDER" ]; then
    echo "✗ Source folder does not exist: $SOURCE_FOLDER"
    exit 1
fi

# Check if destination folder exists, create if needed

if [ ! -d "$DESTINATION_FOLDER" ]; then
    mkdir -p "$DESTINATION_FOLDER"
fi

# Copy all *-all.jar files recursively from all subfolders, exclude target/extension and overwrite if existing

find "$SOURCE_FOLDER" -name "*-all.jar" -type f -not -path "$SOURCE_FOLDER/target/*" -exec cp -f {} "$DESTINATION_FOLDER" \;

if [ $? -eq 0 ]; then
    echo "✓ JAR files copied successfully from all subfolders (existing files overwritten, target/extension excluded)"
else
    echo "✗ Error copying JAR files"
    exit 1
fi