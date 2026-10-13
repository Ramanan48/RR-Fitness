# Verification results

Date: 13 Oct 2026. Honest status of every check in the brief.

## Status update
- Voice clips are now packed into one file (`assets/voice.pack`); all 44 clips verified
  byte-identical to the originals and decodable at their offsets. Project = 74 files.
- The APK **has been built successfully on GitHub Actions** (earlier version, 13 Oct 2026).
  This update (10 exercises, coach, English/Tamil, motivation) must be rebuilt the same way.
- English + Tamil voice clips and 20 how-to videos generated; not yet heard by a person.

## Build environment limitation
The workspace this was written in blocks `dl.google.com`, `maven.google.com`, Maven Central
and `services.gradle.org` (HTTP 403, organisation network policy). The Android SDK, Android
Gradle Plugin and Jetpack Compose libraries could not be downloaded, so:

- **No APK has been built yet.** Build with GitHub Actions or Android Studio (see README).
- **The app has not been run on a device or emulator.** Nothing UI-, audio-playback- or
  lifecycle-related has been observed running.

## What was actually tested

### Timer engine — compiled and run (Kotlin 2.0.21, JVM 21): 11 / 11 passed (re-run after the 10-exercise + motivation update: 12 / 12 passed)
The same test file (`app/src/test/.../WorkoutEngineTest.kt`) runs under JUnit in Android Studio/CI.

| Test | Covers |
|---|---|
| sessionLengthsAreExact | 15/30/45/60 min = 18/36/54/72 intervals; cardio 16/34/52/70; full rounds of 8: 2/4/6/8, final round 8/2/4/6; 30 min default |
| motivationAndPraiseNeverOverlapOtherCues | Motivation at display 15 (exercise), praise at display 17 (rest), no two cues at the same moment, Last round on the first exercise of the final round |
| eachDurationFinishesAtExactActiveTimeWithJitteryTicks | Each duration, ticked at random 5–140 ms intervals, finishes at exactly the selected active time and never runs past it |
| countdownDisplayAndTransitions | Exercise shows 30…1, rest 20…1; switch happens exactly at 0 (30.000 s → rest shows 20; 50.000 s → next exercise) |
| warmupOnceThenCardioRoundsRepeat | For all 4 durations: warm-up (2) appears exactly once at the start; then exercises 3–10 repeat in order; round/exercise numbering, short final round, "next" exercise, final rest, completed counts |
| cueSequenceIsCompleteOrderedAndSpokenOnce | Full sessions: exact cue order; Let's start once; Five…One each spoken 2× per cycle (exercise + rest); Take a rest per cycle; Next exercise per cycle after the first; Completed once, after the final rest; each cue ≤ 50 ms after its second |
| countdownCuesLineUpWithDisplayedNumber | "Five" plays when the display shows 5 … "One" when it shows 1 |
| pauseResumePreservesExactRemainingTime | 1 h paused: position unchanged; after resume, phase and session remaining identical to the millisecond |
| pausedCueIsNotReplayedAfterResume | A number cut off by Pause isn't repeated; the next number plays on time |
| repeatedStartDoesNotRestartOrDuplicate | Extra Start presses do nothing; elapsed time unaffected |
| staleCuesAreDroppedNotQueued | If the ticker stalls, only the newest fresh cue plays; late cues are dropped, never queued |
| restoreFromSnapshotIsPausedAtSamePoint | Process-death restore comes back paused at the identical position |

One test initially failed because my own expectation was wrong (I expected 29 after 1 ms;
correct ceiling behaviour keeps 30 for the full first second). The test was corrected; the
engine was not changed.

### Assets — checked (`tools/check_assets.py`)
- All 8 exercises in code have a bundled animation (48–96 frames, 18 joints).
- All 9 voice cues in code have a bundled `.ogg`. Countdown clips are 0.49–0.60 s, so each
  ends before the next second; the longest transition clip (1.19 s) never collides with
  another cue given the schedule (nearest neighbour is 1 s *before* it and ends at +0.49 s).
- Animations were visually inspected as rendered frames (two pose bugs found and fixed:
  wrong face direction in side views; floor-reach hand crossing the body).

### Cartoon avatar
Rendered for every frame of all 8 exercises by `tools/avatar.py` and inspected
(side-view shirt and ankle drawing fixed). `Avatar.kt` is a line-by-line port of that
renderer; it has been syntax-checked but, like the rest of the UI, not yet run on a device.

### How-to videos
All 8 rendered and probed (H.264 + AAC, 28–35 s each). Frames were inspected for burpees,
jumping jacks and skaters; an early version showed the wrong part of the movement under
each step caption — fixed so every step shows its matching phase. Narration audio was not
listened to by a person (same limitation as the countdown clips). The in-app player
(`HowToScreen.kt`, Android VideoView) is syntax-checked only.

### Android source — syntax-checked only
All app Kotlin files pass the Kotlin 2.0.21 parser (one real bug found and fixed: a `/*`
inside a doc comment). Full type-checking needs the Android/Compose libraries, so API-level
compile errors are still possible on first build — if any appear, they should be small.

## Checklist from the brief

| Check | Status |
|---|---|
| All four durations end at the correct active elapsed time | ✅ Engine tests |
| Exercise/rest transitions and round counts correct | ✅ Engine tests |
| Countdown numbers display correctly | ✅ Engine tests (logic); ⏳ on-device display not seen |
| Countdown numbers speak correctly | ✅ Cue timing tested; ⏳ playback not heard on device; clips not human-reviewed |
| Pause/Resume preserves exact remaining time | ✅ Engine tests |
| Stop cancels timers, animations and audio | ⏳ Implemented (`stop()` cancels the ticker job, calls `voice.stop()`, discards the session); not run |
| Repeated Start creates no duplicate session | ✅ Engine test + ViewModel guards (`session != null`, `ticker?.isActive`) |
| Visual guides and female audio work in airplane mode | ⏳ By design: no INTERNET permission, all assets in APK; not run |
| Screen rotation | ⏳ ViewModel survives rotation; `isChangingConfigurations` prevents a pause; landscape layout provided; not run |
| Backgrounding | ⏳ `onStop` auto-pauses with a banner; process-death restore tested in engine; not run |
| APK created | ❌ Not built here (network policy) |

## Please test on the phone after building
1. 15-min session start to finish with a stopwatch (should read 15:00 ± well under 1 s).
2. Pause mid-countdown ("Three"), wait, Resume — next number should be "Two" on time.
3. Press Start repeatedly / quickly — only one session.
4. Rotate during exercise and during rest — timer continues, no pause.
5. Press Home, return — paused with yellow banner; Resume continues.
6. Airplane mode on — everything works.
7. Listen to all clips (Test voice + one cycle) and judge the voice tone.

## Known limitations
- No background running: the workout pauses whenever the app leaves the screen (by design,
  per the brief's fallback). Screen-off also pauses.
- An incoming-call *notification* (without opening the call screen) may not pause the
  workout; answering the call does. Detecting calls directly would need the phone-state
  permission, which was deliberately avoided.
- Voice is TTS-generated, not a human recording; "slightly husky" is approximate.
- The avatar and stick figures are 2D; torso rotation depth is shown only by arm foreshortening and the face dot.
- Mountain climbers use a slightly inclined plank in the 2D drawing (see ASSETS.md).
- Jumping exercises make floor noise; each has a no-jump option shown on screen.
