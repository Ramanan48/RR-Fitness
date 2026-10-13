#!/usr/bin/env python3
"""
Generates every voice clip in English and Tamil → <out_dir>/<lang>/<cue>.ogg,
then run tools/pack_voice.py <out_dir> to build app/src/main/assets/voice.pack + voice.json.

  English: Kokoro-82M (Apache-2.0), 70 % af_heart + 30 % af_nicole female blend.
  Tamil:   AI4Bharat Indic-TTS Tamil FastPitch + HiFi-GAN (MIT), "female" speaker.

Usage: python3 tools/make_voice.py <kokoro_dir> <indic_dir_containing_ta/> <out_dir>
       python3 tools/pack_voice.py <out_dir>
Cue names must match the Cue enum in engine/WorkoutPlan.kt. The on-screen text for the
motivation lines lives in ui/Strings.kt — keep the wording in sync.
"""
import os, subprocess, sys, tempfile
import numpy as np, soundfile as sf

LINES = {
    "lets_start":    ("Let's start!", "தொடங்கலாம்!"),
    "five":          ("Five.", "ஐந்து."),
    "four":          ("Four.", "நான்கு."),
    "three":         ("Three.", "மூன்று."),
    "two":           ("Two.", "இரண்டு."),
    "one":           ("One.", "ஒன்று."),
    "take_rest":     ("Take a rest.", "ஓய்வு எடுங்கள்."),
    "next_exercise": ("Next exercise.", "அடுத்த பயிற்சி."),
    "completed":     ("Great job! Workout completed.", "அருமை! பயிற்சி முடிந்தது."),
    "keep_going":    ("Keep going, you're doing great!", "தொடருங்கள், நன்றாக செய்கிறீர்கள்!"),
    "you_got_this":  ("You've got this!", "உங்களால் முடியும்!"),
    "great_form":    ("Great form, stay strong!", "சரியான முறை, உறுதியாக இருங்கள்!"),
    "push_harder":   ("Push a little harder!", "இன்னும் கொஞ்சம் வேகமாக!"),
    "stronger":      ("Every rep makes you stronger!", "ஒவ்வொரு முயற்சியும் உங்களை வலிமையாக்கும்!"),
    "dont_stop":     ("Don't stop now!", "இப்போது நிறுத்தாதீர்கள்!"),
    "warmup":        ("Nice and easy. Warm up your body.", "மெதுவாக, உடலை தயார் செய்யுங்கள்."),
    "halfway":       ("Halfway there! Amazing work!", "பாதி முடிந்தது! அருமை!"),
    "last_round":    ("Last round! Give it everything!", "கடைசி சுற்று! முழு சக்தியுடன் செய்யுங்கள்!"),
    "final_push":    ("Final exercise! Finish strong!", "கடைசி பயிற்சி! வலிமையாக முடியுங்கள்!"),
    "breathe":       ("Breathe deep. You're doing amazing.", "ஆழமாக மூச்சு விடுங்கள். அருமையாக செய்கிறீர்கள்."),
    "great_work":    ("Great work! Get ready for the next one.", "நல்ல வேலை! அடுத்ததுக்கு தயாராகுங்கள்."),
    "proud":         ("Be proud of yourself. Keep it up!", "உங்களை நினைத்து பெருமைப்படுங்கள்!"),
}
COUNTDOWN = {"five", "four", "three", "two", "one"}


def english(kdir):
    from kokoro_onnx import Kokoro
    k = Kokoro(f"{kdir}/kokoro-v1.0.onnx", f"{kdir}/voices-v1.0.bin")
    style = 0.7 * k.get_voice_style("af_heart") + 0.3 * k.get_voice_style("af_nicole")
    def say(t):
        a, sr = k.create(t, voice=style, speed=1.0, lang="en-us"); return np.asarray(a, np.float32), sr
    return say


def tamil(idir):
    cwd = os.getcwd(); os.chdir(idir)
    from TTS.utils.synthesizer import Synthesizer
    syn = Synthesizer(tts_checkpoint="ta/fastpitch/best_model.pth", tts_config_path="ta/fastpitch/config.json",
                      tts_speakers_file="ta/fastpitch/speakers.pth", vocoder_checkpoint="ta/hifigan/best_model.pth",
                      vocoder_config="ta/hifigan/config.json", use_cuda=False)
    os.chdir(cwd)
    def say(t):
        return np.asarray(syn.tts(t, speaker_name="female"), np.float32), syn.output_sample_rate
    return say


def encode(a, sr, out, countdown):
    with tempfile.TemporaryDirectory() as td:
        w = os.path.join(td, "a.wav"); sf.write(w, a, sr)
        chain = ("silenceremove=start_periods=1:start_threshold=-42dB:start_silence=0.02,areverse,"
                 "silenceremove=start_periods=1:start_threshold=-42dB:start_silence=0.06,areverse")
        # countdown numbers must finish within the second: speed up gently if needed
        dur = float(subprocess.check_output(["ffprobe", "-v", "error", "-show_entries", "format=duration",
                                             "-of", "csv=p=0", w]))
        tmp = os.path.join(td, "t.wav")
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", w, "-af", chain, tmp], check=True)
        d2 = float(subprocess.check_output(["ffprobe", "-v", "error", "-show_entries", "format=duration",
                                            "-of", "csv=p=0", tmp]))
        tempo = f",atempo={min(1.6, d2 / 0.8):.3f}" if countdown and d2 > 0.8 else ""
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", tmp, "-af",
                        f"loudnorm=I=-16:TP=-1.5:LRA=7{tempo},aresample=24000", "-ac", "1",
                        "-c:a", "libvorbis", "-q:a", "5", out], check=True)
    return float(subprocess.check_output(["ffprobe", "-v", "error", "-show_entries", "format=duration",
                                          "-of", "csv=p=0", out]))


def main():
    kdir, idir, out = sys.argv[1:4]
    for lang, idx, make in (("en", 0, lambda: english(kdir)), ("ta", 1, lambda: tamil(idir))):
        say = make(); d = os.path.join(out, lang); os.makedirs(d, exist_ok=True)
        for name, texts in LINES.items():
            a, sr = say(texts[idx])
            dur = encode(a, sr, os.path.join(d, f"{name}.ogg"), name in COUNTDOWN)
            print(f"{lang} {name:14s} {dur:4.2f}s  {texts[idx]}")


if __name__ == "__main__":
    main()
