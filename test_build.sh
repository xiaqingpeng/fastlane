#!/bin/bash

# Create key.properties file
echo "Creating key.properties file..."
mkdir -p android

# Write key.properties content
echo "storePassword=android" > android/key.properties
echo "keyPassword=android" >> android/key.properties
echo "keyAlias=release" >> android/key.properties
echo "storeFile=../release.keystore" >> android/key.properties

echo "Preparing release.keystore..."
# Check if we have a keystore in secrets, otherwise use the fixed debug keystore
if [ -n "" ]; then
  echo "Using keystore from GitHub Secrets..."
  echo "" | base64 --decode > android/release.keystore
else
  echo "Using fixed debug keystore from project..."
  cp android/debug.keystore android/release.keystore
fi

echo "Verifying created files..."
ls -la android/
if [ -f android/key.properties ]; then
  echo "key.properties file created successfully: $(cat android/key.properties)"
else
  echo "ERROR: key.properties file not created!"
  exit 1
fi

if [ -f android/release.keystore ]; then
  echo "release.keystore file created successfully, size: $(du -sh android/release.keystore | awk '{print $1}')"
else
  echo "ERROR: release.keystore file not created!"
  exit 1
fi

# Test the Flutter build
echo "Testing Flutter build..."
flutter build apk --release
