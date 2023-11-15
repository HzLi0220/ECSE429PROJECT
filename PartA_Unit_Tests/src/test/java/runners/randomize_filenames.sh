#!/bin/bash

# Directory containing the files (replace with your actual directory)
DIR="./src/test/resources/features"

# Loop over all files in the directory
for file in "$DIR"/*
do
  # Generate a random letter A-Z
  prefix=$(tr -dc 'A-Z' </dev/urandom | head -c 1)_

  # Extract filename
  filename=$(basename "$file")

  # Rename the file with the random letter prefix
  mv "$file" "$DIR/$prefix$filename"
done

echo "Files have been renamed with random prefixes."
