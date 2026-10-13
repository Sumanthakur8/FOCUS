NoReels — Focus dashboard update

Includes:
- Refreshed dashboard UI and blocker setup status.
- Separate YouTube Shorts and Instagram Reels toggles.
- Local daily blocked-screen count and lifetime blocked count.
- Focus streak based on days with at least one detected block.
- Adjustable daily blocking goal (increase/decrease by 5, from 1 to 100).

Build:
1. Open this folder in Android Studio and allow Gradle to sync.
2. Connect an Android device, then press Run, or use Build > Build APK(s).
3. Open NoReels and enable the accessibility service from the setup card.
4. Choose which platforms to block.

Privacy and accuracy notes:
- Statistics are stored locally in the app's SharedPreferences; no account or network is used.
- The daily goal measures detected blocking events, NOT total screen time or minutes saved.
- Counters only increase when the service detects a configured Shorts/Reels screen identifier and sends Back.
- YouTube/Instagram can change screen identifiers in updates, so detection may need maintenance.
