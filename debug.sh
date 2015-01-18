#!/bin/bash
./gradlew assembleDebug &&
adb install -r app/build/outputs/apk/app-debug-unaligned.apk &&
adb shell am start -n com.geaden.android.gsana.app/.MainActivity
