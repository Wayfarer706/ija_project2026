#!/usr/bin/env bash

set -e

ARCHIVE_NAME="game.zip"

# Remove old archive if it exists
rm -f "$ARCHIVE_NAME"

# Create archive with required files/folders
zip -r "$ARCHIVE_NAME" pom.xml data src \
  -x "*.class" \
  -x "*.jar" \
  -x "*.war" \
  -x "*.ear" \
  -x "*/target/*" \
  -x "target/*" \
  -x "*/bin/*" \
  -x "bin/*" \
  -x "*/out/*" \
  -x "out/*" \
  -x ".vscode/*" \
  -x "*/.vscode/*" \
  -x "*.log" \
  -x "*.tmp" \
  -x "*~"