#!/usr/bin/env bash

set -e

LOGIN="xyuguyn00"
ARCHIVE_NAME="${LOGIN}.zip"

echo "Cleaning up old archive..."
rm -f "$ARCHIVE_NAME"

echo "Creating temporary directory structure..."
TEMP_DIR=$(mktemp -d)
mkdir -p "$TEMP_DIR/$LOGIN"

echo "Staging files for packaging..."
# Safely copy only the files/folders that actually exist
for item in src data lib readme.txt pom.xml ai_audit.md git_history.txt rozdeleni.txt; do
    if [ -e "$item" ]; then
        cp -r "$item" "$TEMP_DIR/$LOGIN/"
    fi
done

echo "Zipping archive..."
# Move into the temp directory so the zip structure starts at the xyuguyn00 folder
cd "$TEMP_DIR"

zip -r "$ARCHIVE_NAME" "$LOGIN" \
  -x "*.class" \
  -x "*.jar" \
  -x "*.war" \
  -x "*.ear" \
  -x "*/target/*" \
  -x "*/bin/*" \
  -x "*/out/*" \
  -x "*/.vscode/*" \
  -x "*.log" \
  -x "*.tmp" \
  -x "*~"

echo "Moving archive to project root..."
cd - > /dev/null
mv "$TEMP_DIR/$ARCHIVE_NAME" .

echo "Cleaning up temporary files..."
rm -rf "$TEMP_DIR"

echo "Done! $ARCHIVE_NAME has been created successfully."