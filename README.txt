FOCUS — Android app project
==========================

This project uses the FOCUS brand/logo and includes a home dashboard, blocker controls,
local blocking counts, a daily goal setting, and bottom navigation where implemented.

Build without Android Studio:
1. Upload the CONTENTS of this folder to the root of your existing GitHub repository.
2. Ensure the hidden .github/workflows/build-apk.yml file is uploaded.
3. Commit changes and open the Actions tab.
4. Open the successful workflow run and download the FOCUS-debug-apk artifact.
5. Extract the artifact ZIP to get app-debug.apk and install it on your Android phone.

Note: The package/application ID remains com.noreels.app to support updating an existing
installation signed with the same key. The display name is FOCUS. The APK has not been
built or tested as part of preparing this ZIP.


APP SELECTION FEATURE
Open Blockers in FOCUS and scroll to BLOCK ENTIRE APPS. Turn on any listed app to block it completely. YouTube Shorts and Instagram Reels are controlled separately using their dedicated switches. Android accessibility permission must be enabled.

CALENDAR & REMINDERS UPDATE
Insights now includes a monthly calendar view, daily blocking-goal progress, a reminder time picker, a reminder-tone picker, and a reminder-off control. The reminder attempts to play the selected notification tone and vibrate at the chosen time. Android battery settings, Do Not Disturb, silent mode, and device policies can affect delivery. Reminders are scheduled as inexact alarms for compatibility and battery efficiency.
