# Ramanan Fit — Master Prompt

Use this prompt to rebuild, extend or hand over the Ramanan Fit app. It describes every
feature of the current version. Build a working app, not a mockup.

---

## 1. Goal

Build a complete **offline Android cardio HIIT app** called **"Ramanan Fit"** for personal
home workouts (fitness, stamina, weight management), in **English and Tamil**. Deliver:

1. Installable APK (debug + release, release signed with the debug key for sideloading).
2. Complete Gradle source project.
3. Build and install instructions (Android Studio + GitHub Actions).
4. Asset list with origins and licences.
5. Verification report and remaining limitations.

Everything — timers, animations, videos, voice, settings — must work **in airplane mode**.
No login, ads, subscriptions, analytics, network calls or Android permissions.
Never claim guaranteed results ("flat belly", "lose X kg"). Never claim the APK, assets or
tests are done unless they were actually built and verified.

## 2. Tech stack

- Kotlin 2.0.21, Jetpack Compose (BOM 2024.12.01), Material 3, AGP 8.7.3, Gradle 8.11.1.
- compileSdk/targetSdk 35, minSdk 26, JVM 17. Package `com.ramanan.fit`.
- Timer logic in **pure Kotlin** (no Android imports), separate from UI and audio, with
  JUnit 4 tests. Single Activity + one AndroidViewModel (SavedStateHandle), survives rotation.
- GitHub Actions `.github/workflows/build-apk.yml` (ubuntu-24.04): checkout@v5,
  setup-java@v5 (JDK 17), **no setup-android step** (runner already has the SDK; the old
  setup-android@v3 fails on the removed 'tools' package), setup-gradle@v4,
  `chmod +x gradlew`, `testDebugUnitTest`, `assembleDebug assembleRelease`, upload artifact
  **RamananFit-apk**.

## 3. Workout design — cardio HIIT

- Interval = **30 s exercise + 20 s rest** (50 s).
- **Warm-up runs once** (exercises 1–2), then **cardio rounds of 8** (exercises 3–10) repeat
  until time is up; the final round may be shorter so the session ends exactly on time.
  The final 20 s rest is included; completion is announced after it.

| Session | Intervals | Structure |
|---|---|---|
| 15 min | 18 | warm-up + 2 rounds |
| 30 min (default) | 36 | warm-up + 4 rounds + 2 |
| 45 min | 54 | warm-up + 6 rounds + 4 |
| 1 hour | 72 | warm-up + 8 rounds + 6 |

### The 10 exercises (level · jump · no-jump option)
1. March with Arm Swings — Warm-up · no jump.
2. Step Jacks — Warm-up · no jump.
3. High Knees — Beginner · light · fast march.
4. Butt Kicks — Beginner · light · walk-pace heel curls.
5. Shadow Boxing — Beginner · light bounce · slower punches.
6. Jumping Jacks — Advanced · jump · step jacks.
7. Skaters — Advanced · jump · side step + tap back foot.
8. Mountain Climbers — Advanced · floor · hands on chair/bed.
9. Squat Jumps — Harder · jump · squat + rise to toes.
10. Burpees — Harder · jump · step back / in / reach.

Each exercise has, **in English and in Tamil**: name, 3 steps (start → movement → return),
2 form tips, a no-jump/easier option where applicable, and a get-ready line shown during the
preceding rest. English text includes simple Tanglish tips; Tamil text is in Tamil script.

## 4. Timer engine rules (must be unit-tested)

- Active elapsed = accumulated time + (`SystemClock.elapsedRealtime()` − resume moment),
  recomputed every tick → no drift; paused time never counts.
- Big countdown = `ceil(remaining)`: exercise 30→1, rest 20→1; phase switches the instant
  remaining hits 0.
- Position: phase, exercise index, warm-up flag, round (0 in warm-up), number within
  warm-up/round, group size (2, 8 or short final round), next exercise, time left, progress,
  completed exercises, completed full rounds.
- Start only from READY (duplicate presses ignored); one ticker coroutine (~50 ms).
- Process-death restore: snapshot on pause, restored PAUSED at the same point.

## 5. Voice coach (offline, prerecorded female, English + Tamil)

Clips packed in `assets/voice.pack` with index `assets/voice.json` (22 per language,
`tools/pack_voice.py`), loaded via SoundPool at byte offsets; keeps the repo < 100 files:
- Start "Let's start!", countdown "Five…One" (each < 0.9 s), "Take a rest.", "Next exercise.",
  "Great job! Workout completed."
- **Motivation (15 s into every exercise, when the display shows 15):** warm-up line for the
  two warm-ups; "Halfway there!" on the interval containing the session midpoint;
  "Last round! Give it everything!" on the first exercise of the final round;
  "Final exercise! Finish strong!" on the last; otherwise a rotation of six lines
  (Keep going / You've got this / Great form / Push a little harder / Every rep makes you
  stronger / Don't stop now).
- **Rest praise (3 s into every rest):** rotation of Breathe deep / Great work / Be proud of
  yourself (last rest always "Be proud").
- The same motivation text appears on screen as a banner from the moment it is spoken.
- English: Kokoro-82M (70 % af_heart + 30 % af_nicole). Tamil: AI4Bharat Indic-TTS Tamil
  female (FastPitch + HiFi-GAN). Generated by `tools/make_voice.py`.
- One voice at a time; each tick plays at most the newest cue crossed and only if < 0.6 s
  late; paused cues never replayed. SoundPool, transient-may-duck focus per clip; transient
  focus loss (call) → pause. Home: volume slider, mute, Test voice; missing clips reported.

## 6. Visuals

- **Animation data:** original joint-angle keyframes (`tools/make_poses.py`) → `poses.json`
  (18 joints, 48–96 frames, jump lift, sideways travel). Animation clock = phase time, so
  it freezes on Pause.
- **Coach (default):** original 2D cartoon fitness girl drawn from shapes — light-brown high
  ponytail, teal sports bra, teal leggings with a yellow side stripe, white sneakers, dark
  wristband, athletic build (`Avatar.kt`, mirrored by `tools/avatar.py`). Not an AI video.
- **Coach photo:** owner-supplied AI image `res/drawable-nodpi/coach_photo.jpg` on the Home
  "Your Coach" card (with a daily motivation quote, EN/TA) and on the Completed screen.
  Keep the repo private; the image's licence is unknown.
- **Stick figure:** optional style (Home → Guide style).
- **How-to videos:** 20 portrait MP4s (10 × EN/TA), 720×1280 H.264 + AAC, 28–45 s, in
  `res/raw/howto_<id>_<lang>.mp4`, made by `tools/make_howto_videos.py`: title → each step in
  slow motion showing the matching part of the move → full speed → form tips → no-jump
  option → "Ready? Let's go!". Captions in the chosen language (Tamil via Noto Sans Tamil
  with raqm shaping); narration in the same voice as the coach.

## 7. Screens (all text in English or Tamil)

1. **Home** — title; **language picker (English / தமிழ்)**; "Your Coach" photo card with a daily quote; 2×2 duration cards; info card
   (30 s / 20 s / rounds / intervals, structure, honest results note); voice card; switches
   for guide style and "Show how-to videos before each workout"; 10 exercise rows with level
   tag, live coach thumbnail, expandable steps/tips/no-jump + "Watch how-to video"; recent
   history; safety disclaimer; big START.
2. **How-to** — after START (if enabled): videos 1/10…10/10 with Back / Next, last one
   START WORKOUT, "Skip videos & start workout", Replay, Close; single video from a row.
3. **Workout (green)** — warm-up or round/exercise status, time left, progress bar, large
   coach animation, motivation banner, huge countdown, level, name, form tip, no-jump
   option, next exercise, PAUSE/RESUME + STOP (confirm).
4. **Rest (blue)** — next-exercise preview animation, rest praise banner, huge countdown,
   next name and get-ready line.
5. **Completed** — round coach photo, "Your coach is proud of you!", great job, session, exercises, full rounds, active time, cool-down tip.

## 8. Storage, lifecycle, interruptions

SharedPreferences only: session length, language, volume, mute, guide style,
how-to-before-workout, last 50 workouts. Screen kept on only while running. Rotation does
not pause. Leaving the app auto-pauses with a banner; never skips or resets. Back = Pause.

## 9. Project layout

```
app/src/main/java/com/ramanan/fit/
  engine/ WorkoutPlan.kt (schedule, positions, cues, motivation), WorkoutSession.kt
  audio/  VoicePlayer.kt (both languages)
  anim/   PoseLibrary.kt, StickFigure.kt, Avatar.kt (coach)
  data/   Exercises.kt (Lang, Level, EN/TA text, video ids), Prefs.kt
  ui/     Strings.kt (EN/TA), WorkoutViewModel.kt, App.kt, HomeScreen.kt, HowToScreen.kt,
          WorkoutScreen.kt, Theme.kt
assets/voice.pack + voice.json, assets/poses/poses.json, res/raw/howto_*_en|ta.mp4
tools/ make_poses.py, avatar.py, make_voice.py, pack_voice.py, make_howto_videos.py, check_assets.py
```

## 10. Verification checklist

Unit tests: exact durations with jittery ticks; warm-up once then rounds of 8; round
numbering and short final round; 30→1/20→1 display; voice numbers match display; every cue
once and in order; motivation at display 15 and rest praise at display 17, never at the
same moment as another cue; Halfway / Last round / Final push each exactly once; pause/
resume exactness; no replay after pause; duplicate Start ignored; stale cues dropped;
snapshot restore. Asset check: every exercise has an animation and EN + TA videos; every
cue has EN + TA clips within length limits. On a phone: timed session, pause/resume,
rotation, Home → paused banner, airplane mode, switch language, listen to both voices.

## 11. Honesty rules

Say so if the APK could not be built, if audio/video was not heard by a person, or if an
image is a mockup. No guaranteed fitness or weight-loss claims.
