# Ramanan Fit

Offline Android cardio HIIT app in **English and Tamil**: 10 exercises (2 warm-up, 3 beginner,
3 advanced, 2 harder), 30 s work / 20 s rest, sessions of 15 / 30 / 45 / 60 minutes, animated
guides performed by a cartoon fitness coach (or a stick figure), a "Your Coach" photo card, a female voice coach with
countdowns and motivation lines, and how-to videos.
No login, ads, internet or permissions.

> **Status:** the APK was **not** built in the environment where this project was written
> (Google's Maven repository and Android SDK downloads were blocked there). The timer engine
> was compiled and tested; the Android UI code has been syntax-checked only. Build it with
> one of the two methods below — see `VERIFICATION.md` for exactly what has and hasn't been tested.

Full feature specification: see `MASTER_PROMPT.md`.

## Build the APK

### Option A — GitHub Actions (no Android Studio needed)
1. Create a new **private** GitHub repository and push this folder to it.
2. Open the repository's **Actions** tab → the **Build APK** workflow runs automatically
   (or click *Run workflow*).
3. When it finishes (≈5–8 min), download the **RamananFit-apk** artifact. It contains
   `app-debug.apk` and `app-release.apk` (both installable; release is smaller and faster).

### Option B — Android Studio
1. Install Android Studio (Ladybug 2024.2 or newer).
2. *File → Open* → select this `RamananFit` folder. Let Gradle sync (first sync downloads
   Gradle 8.11.1, AGP 8.7.3, Kotlin 2.0.21 and Compose — needs internet once).
3. Run tests: right-click `app/src/test` → *Run Tests* (or `./gradlew testDebugUnitTest`).
4. Build: *Build → Build App Bundle(s) / APK(s) → Build APK(s)*
   (or `./gradlew assembleRelease`). Output: `app/build/outputs/apk/…`

Command line equivalent (JDK 17 + Android SDK 35 installed, `ANDROID_HOME` set):
```
./gradlew testDebugUnitTest assembleRelease
```

## Install on the phone
1. Copy the `.apk` to the phone (USB, Drive, WhatsApp-to-self…).
2. Tap it → allow *Install unknown apps* for the app you opened it from → Install.
   Or with USB debugging: `adb install -r app-release.apk`.
3. Requires Android 8.0 (API 26) or newer. The release build is signed with the debug key —
   fine for personal use, not for Play Store.

## Using it
- **How-to videos:** pressing **START** first plays a short how-to video for each of the
  10 exercises (Next ▶ / ◀ Back, or *Skip videos & start workout*). The last one has
  **START WORKOUT**. Turn this off on the home screen ("Show how-to videos before each
  workout"). Any single video: tap an exercise on the home screen → *Watch how-to video*.
- Pick 15 / 30 / 45 min / 1 hour (30 is default) → **START**.
- **PAUSE / RESUME** freezes and continues the countdown, animation and voice at the exact same point.
- **STOP** (with confirmation) ends the session and returns to the home screen.
- Leaving the app (home button, app switch, screen off, call screen) **auto-pauses**; a yellow
  banner says why. Rotation does not pause.
- Volume slider, Mute switch and *Test voice* are on the home screen.

## Project layout
```
app/src/main/java/com/ramanan/fit/
  engine/        Pure Kotlin timer: schedule, monotonic clock, pause/resume, voice cues (no Android imports)
  audio/         VoicePlayer — SoundPool playback of bundled clips, audio focus, one clip at a time
  anim/          PoseLibrary (loads poses.json), StickFigure + Avatar (Compose Canvas renderers)
  data/          Exercise text (English + Tanglish), local preferences & history
  ui/            ViewModel, Home / Workout+Rest / Completion screens, theme
app/src/main/assets/
  voice.pack + voice.json  all 44 voice clips (EN + TA) in one file
app/src/main/res/raw/howto_*_en|ta.mp4  20 how-to videos
  poses/poses.json  10 looping animations (generated)
app/src/test/    Engine unit tests (JUnit 4)
tools/make_poses.py    Regenerates poses.json (+ preview GIFs) from joint-angle keyframes
tools/avatar.py        Reference coach renderer (makes preview GIFs on demand)
tools/make_voice.py    Generates the EN + TA voice clips (Kokoro + AI4Bharat Indic-TTS)
tools/pack_voice.py    Packs the clips into assets/voice.pack
tools/make_howto_videos.py  Renders the EN + TA how-to MP4s (see ASSETS.md for model files)
tools/check_assets.py  Checks every exercise/cue in code has a bundled asset
docs/            Sample screens, coach sheet (PNG), voice previews (EN + TA MP3)
```

### How the timing works
- Each interval = 30 s exercise + 20 s rest. 15 min = 18 intervals, 30 = 36, 45 = 54, 60 = 72.
- Warm-up (March, Step jacks) runs **once** at the start. Then cardio rounds of 8
  (High knees, Butt kicks, Shadow boxing, Jumping jacks, Skaters, Mountain climbers,
  Squat jumps, Burpees) repeat; a shorter final round makes the session end exactly on time:
  15 min = warm-up + 2 rounds · 30 min = warm-up + 4 rounds + 2 ·
  45 min = warm-up + 6 rounds + 4 · 60 min = warm-up + 8 rounds + 6.
- Motivation: 15 s into every exercise the coach says a motivation line (Halfway / Last round /
  Final exercise at the right moments), and 3 s into every rest a praise line — also shown on screen.
- Language: Home → English / தமிழ் switches all text, the voice and the how-to videos.
- The final rest is included; completion is announced after it.
- Elapsed time = accumulated active time + (`SystemClock.elapsedRealtime()` − resume moment),
  recomputed from the monotonic clock every tick — no drift; paused time never counts.
- The big number is `ceil(remaining)`: 30 shows for the whole first second, 1 for the last,
  and the phase switches the instant remaining reaches 0.
- Voice cues are scheduled at exact times ("Five" when the display turns 5 … "One" when it
  turns 1, in both exercise and rest). Each tick returns at most one cue — the newest one
  crossed, and only if it is less than 0.6 s late — so audio never queues or plays stale.

### Results — honest expectations
4–5 sessions a week for 4 weeks typically gives a clear stamina and leg-endurance
improvement. Fat loss depends mostly on food; results vary person to person. The app
makes no guaranteed-result claims.

## Changing the animations
Edit keyframes in `tools/make_poses.py`, then:
```
python3 tools/make_poses.py app/src/main/assets/poses/poses.json docs/guide-previews
```
(needs Pillow for the GIF previews).
