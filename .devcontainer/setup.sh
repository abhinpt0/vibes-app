#!/usr/bin/env bash
set -e

ANDROID_SDK_ROOT=/opt/android/sdk
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
CMDLINE_TOOLS_ZIP=/tmp/cmdtools.zip

echo "==> Downloading Android command-line tools..."
wget -q "$CMDLINE_TOOLS_URL" -O "$CMDLINE_TOOLS_ZIP"

echo "==> Installing Android SDK..."
sudo mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
sudo unzip -q "$CMDLINE_TOOLS_ZIP" -d /tmp/cmdtools-extracted
sudo mv /tmp/cmdtools-extracted/cmdline-tools "$ANDROID_SDK_ROOT/cmdline-tools/latest"
rm "$CMDLINE_TOOLS_ZIP"

export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$PATH"

echo "==> Accepting licenses and installing SDK components..."
yes | sdkmanager --sdk_root="$ANDROID_SDK_ROOT" --licenses > /dev/null 2>&1 || true
sdkmanager --sdk_root="$ANDROID_SDK_ROOT" \
  "platform-tools" \
  "platforms;android-34" \
  "build-tools;34.0.0"

echo "==> Making gradlew executable..."
chmod +x dsr-app/android/gradlew

echo "==> Android SDK setup complete."
echo "    Build APK with:  bash .devcontainer/build.sh"
