#!/usr/bin/env bash
set -e

cd dsr-app/android
echo "==> Building debug APK..."
./gradlew assembleDebug

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "==> Build successful!"
echo "    APK: dsr-app/android/$APK_PATH"
echo ""
echo "    To install on your phone:"
echo "    1. Download the APK from the Codespaces file explorer"
echo "    2. Transfer to your phone and open it (enable 'Install unknown apps' if prompted)"
echo "    OR connect via USB and run: adb install $APK_PATH"
