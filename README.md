# WakeSync ⏰⚡ — KMP Developer & AI Agent Reference Manual

**WakeSync** is an enterprise-grade, high-stakes social wake-up alarm & habit tracking application built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform** (targeting Android & iOS). 

This document serves as the **authoritative developer guide and AI Agent blueprint** for understanding the repository layout, Compose Multiplatform architecture, state machines, platform-specific expect/actual bridges, and complete 9-section feature implementations.

---

## 🏗️ 1. Project Directory & Package Sitemap

```
composeApp/src/
├── commonMain/kotlin/com/social/wakesync/
│   ├── app/
│   │   └── App.kt                        # App root composable, top-level screen router & global overlay engine
│   ├── feature/home/
│   │   ├── HomeRepository.kt             # Interface definition, AlarmData, Habit, SoundMetadata data models
│   │   ├── HomeViewModel.kt              # Main UI State management, autoSetAlarm(), addAlarm() handlers
│   │   ├── MainHomeScreen.kt             # Main Dashboard, Dynamic Alarm Card, Daily Habit Tracker UI
│   │   ├── SetAlarmScreen.kt             # Wheel Time Picker, 10 Bed Tasks, Duo/Group Username Search Bottom Sheet
│   │   ├── AlarmsScreen.kt               # Active alarm list, toggle, edit, and deletion UI
│   │   ├── AlarmPuzzleSolo.kt            # 60s Timer Engine, 2-Attempt Auto-Switch, 10 Bed-Friendly Task Composables
│   │   ├── StreakSaveScreen.kt           # Canvas Confetti Victory Celebration Screen (+1 Streak)
│   │   ├── StreakBrokenScreen.kt         # Broken Heart Failure Screen (-3 Streaks Penalty)
│   │   └── AlarmState.kt                 # Shared global mutable state (isRinging, showStreakSave, showStreakBroken)
│   ├── ui/
│   │   ├── navigation/
│   │   │   └── WakeSyncBottomBar.kt     # Fluid spring-animated custom bottom navigation bar
│   │   ├── theme/
│   │   │   └── ColorPalette.kt          # AppColorPalette tokens (#0A0E1A, #00E0FF, #00FF94, #FF3D71, #FFD23D)
│   │   └── utils/
│   │       └── BackHandler.kt            # KMP platform-agnostic hardware Back Press handler
└── androidMain/kotlin/com/social/wakesync/feature/home/
    ├── HomeRepository.kt                 # Android Firebase Firestore & local storage implementation
    ├── AlarmService.kt                   # Android Foreground Service, 100% Volume Lock & WakeLock
    └── AlarmReceiver.kt                  # BroadcastReceiver listening to Android AlarmManager intents
```

---

## 🎨 2. Design Tokens & Color System (`ColorPalette.kt`)

| Token Name | Hex Code | Purpose & Semantic Usage |
|---|---|---|
| `VoidBg` | `#0A0E1A` / `#060810` | Main deep background for screens |
| `Surface` | `#131829` | Standard component cards & bottom sheets |
| `Elevated` | `#1C2237` | High-priority interactive cards |
| `CyanCta` | `#00E0FF` | Primary action buttons, Home Tab accent, active sliders |
| `WinGreen` | `#00FF94` | Victory screen, puzzle success, Messages Tab accent |
| `LossRed` | `#FF3D71` | Alarm timeout, penalty cards, Profile Tab accent |
| `GoldPremium` | `#FFD23D` | Leaderboard rank cards, Rankings Tab accent |
| `StreakFire` | `#FF8A3D` ➔ `#FFD23D` | Active streak count badge, Feed Tab accent |

---

## 🛠️ 3. KMP Architecture & Platform Abstraction (`expect` / `actual`)

WakeSync utilizes **Kotlin Multiplatform (KMP)** patterns to share 100% of UI logic across Android and iOS while delegating hardware and backend services cleanly:

1. **Repository Pattern (`expect fun getHomeRepository(): HomeRepository`)**:
   - `commonMain`: Defines `HomeRepository` interface & `AlarmData` schema.
   - `androidMain`: `AndroidHomeRepository` connects to `FirebaseFirestore` and local storage.
2. **Platform Back Navigation (`expect fun BackHandler`)**:
   - Handles physical back button on Android devices and gesture navigation on iOS.
3. **Alarm Manager & Foreground Execution**:
   - `androidMain`: `AlarmService` uses `PowerManager.WakeLock` with `ACQUIRE_CAUSES_WAKEUP` and forces `AudioManager.STREAM_ALARM` to 100% volume.

---

## 🤖 4. AI Agent Guidelines for the 9 App Sections

If an AI Agent or developer is tasked with modifying or extending any screen, follow these strict section guidelines:

```
WakeSync Section Map for AI Agents
├── Section 1: Onboarding & Authentication → [App.kt / AuthScreen.kt]
├── Section 2: Main Home & Habit Tracker → [MainHomeScreen.kt / HomeViewModel.kt]
├── Section 3: Set Alarm & 10 Task Config → [SetAlarmScreen.kt / HomeRepository.kt]
├── Section 4: Duo & Group Sync Engine → [SetAlarmScreen.kt / AndroidHomeRepository.kt]
├── Section 5: Alarm Execution & 60s Engine → [AlarmPuzzleSolo.kt / AlarmService.kt]
├── Section 6: Celebration & Penalties → [StreakSaveScreen.kt / StreakBrokenScreen.kt]
├── Section 7: Rankings & Leaderboard → [WakeSyncBottomBar.kt / LeaderboardComposables]
├── Section 8: Social Shame Feed → [FeedComposables / Firestore Activity Stream]
└── Section 9: Messages & Profile → [ProfileComposables / SoundCatalog]
```

### Section 1: Onboarding & Authentication
- **Location**: `App.kt`, `AuthScreen.kt`
- **Rule**: Authentication states map directly to `MainUiState.Auth`. Upon successful Google Sign-In, write `user` document to `/users/{uid}` in Firestore.

### Section 2: Main Home Dashboard & Daily Habit Tracker
- **Location**: `MainHomeScreen.kt`, `HomeViewModel.kt`
- **Rule**: `HomeUiState` controls `hasAlarmToday`. When no alarm is active, display the 6:30 AM suggested card with embedded `⚡ Auto Set` and `⏰ Add New` buttons inside the card. Habits write directly to `HomeRepository.toggleHabit()`.

### Section 3: Set Alarm Screen & Configuration
- **Location**: `SetAlarmScreen.kt`
- **Rule**: Time picker must preserve `44.dp` slot height with centered vertical arrangement to prevent center text clipping. **Math challenge is the ONLY game with 3 levels (Level 1=1Q, Level 2=2Q, Level 3=3Q)**.

### Section 4: Duo & Group Real-Time Sync Engine
- **Location**: `SetAlarmScreen.kt` (Bottom Sheet UI), `androidMain/HomeRepository.kt`
- **Rule**: Duo enforces max 2 members (You + 1 Partner); Group enforces 3–8 members. **Solo alarms save locally for offline priority**; **Duo/Group alarms write to `/duo_alarms` and partner `/users/{partnerUid}/alarms`** in Firestore for real-time cross-phone synchronization.

### Section 5: Alarm Execution Engine & 10 Bed Tasks
- **Location**: `AlarmPuzzleSolo.kt`, `AlarmService.kt`
- **Rule**: `AlarmPuzzleSolo` must execute **60s Attempt 1 ➔ Auto Task Switch ➔ 60s Attempt 2**. All 10 tasks must be 100% bed-friendly (in-bed touch/shake interactions).

### Section 6: Celebration & Outcome Screens
- **Location**: `StreakSaveScreen.kt`, `StreakBrokenScreen.kt`
- **Rule**: Triggering `onDismiss` within 60s sets `AlarmState.showStreakSave = true` (+1 Streak). Timing out twice sets `AlarmState.showStreakBroken = true` (-3 Streaks penalty).

### Section 7: Global Leaderboard & Rankings ("Ranking" Tab)
- **Location**: `WakeSyncBottomBar.kt` (Gold Accent)
- **Rule**: Consumes `HomeStats` (`streak`, `wins`, `losses`, `rank`) to render gold tier badges.

### Section 8: Social Shame Feed ("Feed" Tab)
- **Location**: `WakeSyncBottomBar.kt` (Fire Accent)
- **Rule**: Listens to public `/activity_feed` in Firestore for oversleeping shame posts.

### Section 9: Messages & Profile Settings ("Messages" & "Profile" Tabs)
- **Location**: `WakeSyncBottomBar.kt` (Green & Red Accents)
- **Rule**: Manages direct messaging between Duo partners and sound catalog audio previews via `SoundPlayer`.