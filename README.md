# Fajr Alarm

Fajr Alarm is an offline-first Android alarm prototype built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, Hilt, WorkManager, AlarmManager, CameraX, and on-device ML Kit. It uses original name and artwork.

## Open and run

1. Install Android Studio and Android SDK Platform 35, Build Tools, and JDK 17.
2. Open this folder in Android Studio and choose **Sync Project with Gradle Files**.
3. Connect an Android phone, enable Developer options and USB debugging, then select **Run app**.
4. Create an alarm, choose a mission, and grant camera/activity permissions only when that mission asks.
5. On Android 12+, allow **Alarms & reminders** exact alarm access when prompted. Allow notifications and full-screen notifications so the ringing screen can appear over the lock screen.
6. Test with a one-minute alarm while the phone is locked. Repeat after reboot. In Android battery settings, set Fajr Alarm to **Unrestricted** if the phone delays alarms.

Alarms depend on Android's exact alarm permission and manufacturer power policies; verify on the target phone before relying on it.

## Included

- Alarm list with create, edit, delete, enable/disable, repeat days, label, volume, vibration, and mission selection.
- AlarmManager scheduling, reboot rescheduling, foreground ringing service, full-screen notification, persistent ring, volume ramp, and delayed emergency stop.
- Shake, arithmetic, exact-text, step-counter, photo label, and camera pose mission paths.
- Local wake-event records, basic streak/success/average insights, and dark theme setting.
- Three-screen first-run onboarding, 12/24-hour display, mission previews, default difficulty, and system alarm/notification tone selection.
- Unit tests for next-alarm calculations and mission verification rules.

No Play Console or subscription setup is needed; Billing and other phase-two items are not included.
