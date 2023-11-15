#!/bin/bash

# Directory containing the files (replace with your actual directory)
FEATURES_DIR="./src/test/resources/features"

# Loop over all files in the directory
for file in "$FEATURES_DIR"/*.feature
do
  # Extract filename
  filename=$(basename "$file")

  # Remove the first two characters (prefix) from the filename
  newfilename=${filename:2}

  # Rename the file to remove the prefix
  mv "$file" "${file%/*}/$newfilename"
done

echo "Prefixes have been removed from the feature files."
