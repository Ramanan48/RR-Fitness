package com.ramanan.fit.ui

import com.ramanan.fit.data.Lang
import com.ramanan.fit.engine.Cue
import com.ramanan.fit.engine.PauseReason
import com.ramanan.fit.engine.SessionLength

/** Every user-visible string, in English and Tamil. Keep voice-line wording in sync with tools/make_voice.py. */
class S private constructor(
    val tagline: String,
    val language: String,
    val sessionLength: String,
    val defaultTag: String,
    val workout: String,
    val rest: String,
    val rounds: String,
    val intervals: String,
    val voiceCoach: String,
    val femaleVoice: String,
    val muted: String,
    val volume: (Int) -> String,
    val mute: String,
    val testVoice: String,
    val missingClips: String,
    val exercisesTitle: String,
    val guideStyle: (Boolean) -> String,
    val howtoBefore: String,
    val recent: String,
    val disclaimer: String,
    val start: (String) -> String,
    val structure: (SessionLength) -> String,
    val info: (SessionLength) -> String,
    val history: (Int, Int, Int) -> String,
    val steps: String,
    val easier: String,
    val watchHowto: String,
    // workout
    val exercise: String,
    val restLabel: String,
    val warmupStatus: (Int, Int) -> String,
    val roundStatus: (Int, Int, Int, Int) -> String,
    val left: (String) -> String,
    val nextUpPreview: String,
    val lastRest: String,
    val next: (String) -> String,
    val lastExercise: String,
    val finalRest: String,
    val noJump: (String) -> String,
    val pause: String,
    val resume: String,
    val stop: String,
    val stopTitle: String,
    val stopText: String,
    val keepGoing: String,
    val paused: (PauseReason?) -> String,
    // done
    val greatJob: String,
    val completed: String,
    val session: String,
    val exercisesDone: String,
    val fullRounds: String,
    val activeTime: String,
    val coolDown: String,
    val backHome: String,
    // how-to
    val howto: String,
    val close: String,
    val replay: String,
    val back: String,
    val home: String,
    val nextBtn: String,
    val startWorkout: String,
    val skipVideos: String,
    val gotIt: String,
    val videoMissing: String,
    val motivation: (Cue) -> String,
    val dur: (SessionLength) -> String,
    val coachTitle: String,
    val coachQuotes: List<String>,
    val coachProud: String,
) {
    companion object {
        fun of(lang: Lang): S = if (lang == Lang.TA) TA else EN

        private val EN = S(
            tagline = "Cardio HIIT at home · works fully offline",
            language = "Language",
            sessionLength = "Session length",
            defaultTag = "  (default)",
            workout = "Workout", rest = "Rest", rounds = "Rounds", intervals = "Intervals",
            voiceCoach = "Voice coach",
            femaleVoice = "Female voice coach",
            muted = "Muted",
            volume = { "Volume $it%" },
            mute = "Mute",
            testVoice = "Test voice",
            missingClips = "Missing voice clips: ",
            exercisesTitle = "The 10 exercises (tap for form tips)",
            guideStyle = { if (it) "Guide style: Coach" else "Guide style: Stick figure" },
            howtoBefore = "Show how-to videos before each workout",
            recent = "Recent workouts",
            disclaimer = "General fitness guide only — results vary person to person. Stop if you feel pain, " +
                "dizziness or chest discomfort, and check with a doctor if you have a health condition.",
            start = { "START  ·  $it" },
            structure = { "Warm-up + ${it.fullRounds} rounds" + if (it.lastRoundSize < 8) " + ${it.lastRoundSize}" else "" },
            info = {
                "Cardio HIIT: a 2-exercise warm-up once, then rounds of 8 cardio exercises " +
                    "(beginner → advanced → harder) repeat until ${it.label} is up. Stretch 3–5 min after.\n\n" +
                    "For best progress: 4–5 days a week + healthy food. Stamina usually improves " +
                    "within 4 weeks; weight change depends mostly on diet and varies person to person."
            },
            history = { m, e, r -> "$m min · $e exercises · $r full rounds" },
            steps = "Steps",
            easier = "No-jump / easier: ",
            watchHowto = "▶  Watch how-to video",
            exercise = "EXERCISE", restLabel = "REST",
            warmupStatus = { n, g -> "Warm-up  ·  $n/$g" },
            roundStatus = { r, R, n, g -> "Round $r/$R  ·  Exercise $n/$g" },
            left = { "$it left" },
            nextUpPreview = "NEXT UP — preview",
            lastRest = "LAST REST",
            next = { "Next: $it" },
            lastExercise = "Last exercise!",
            finalRest = "Final rest — breathe slowly. Almost done!",
            noJump = { "No-jump: $it" },
            pause = "PAUSE", resume = "RESUME", stop = "STOP",
            stopTitle = "Stop workout?",
            stopText = "This ends the session and resets the timer.",
            keepGoing = "Keep going",
            paused = {
                when (it) {
                    PauseReason.BACKGROUND -> "Paused — the app left the screen. Tap Resume to continue."
                    PauseReason.AUDIO_INTERRUPTION -> "Paused — audio interruption (e.g. a call). Tap Resume to continue."
                    else -> "Paused"
                }
            },
            greatJob = "Great job!",
            completed = "Workout completed",
            session = "Session", exercisesDone = "Exercises completed", fullRounds = "Full cardio rounds",
            activeTime = "Active time",
            coolDown = "Cool down: 3–5 min easy walking and gentle stretches. Drink water!",
            backHome = "Back to home",
            howto = "HOW TO", close = "Close", replay = "↻  Replay", back = "◀ Back", home = "Home",
            nextBtn = "Next ▶", startWorkout = "START WORKOUT", skipVideos = "Skip videos & start workout",
            gotIt = "Got it", videoMissing = "How-to video missing",
            motivation = {
                when (it) {
                    Cue.KEEP_GOING -> "Keep going, you're doing great!"
                    Cue.YOU_GOT_THIS -> "You've got this!"
                    Cue.GREAT_FORM -> "Great form, stay strong!"
                    Cue.PUSH_HARDER -> "Push a little harder!"
                    Cue.STRONGER -> "Every rep makes you stronger!"
                    Cue.DONT_STOP -> "Don't stop now!"
                    Cue.WARMUP -> "Nice and easy. Warm up your body."
                    Cue.HALFWAY -> "Halfway there! Amazing work!"
                    Cue.LAST_ROUND -> "Last round! Give it everything!"
                    Cue.FINAL_PUSH -> "Final exercise! Finish strong!"
                    Cue.BREATHE -> "Breathe deep. You're doing amazing."
                    Cue.GREAT_WORK -> "Great work! Get ready for the next one."
                    Cue.PROUD -> "Be proud of yourself. Keep it up!"
                    else -> ""
                }
            },
            dur = { it.label },
            coachTitle = "Your Coach",
            coachQuotes = listOf(
                "Ready to sweat? Let's crush today's cardio together!",
                "Small steps every day = big results. Let's go!",
                "Your only competition is yesterday's you.",
                "30 seconds at a time — you can do anything!",
                "Show up today. Future you will say thanks!",
            ),
            coachProud = "Your coach is proud of you!",
        )

        private val TA = S(
            tagline = "வீட்டிலேயே கார்டியோ HIIT · இணையம் தேவையில்லை",
            language = "மொழி",
            sessionLength = "பயிற்சி நேரம்",
            defaultTag = "  (இயல்பு)",
            workout = "பயிற்சி", rest = "ஓய்வு", rounds = "சுற்றுகள்", intervals = "இடைவெளிகள்",
            voiceCoach = "குரல் பயிற்சியாளர்",
            femaleVoice = "பெண் குரல் பயிற்சியாளர்",
            muted = "ஒலி அணைக்கப்பட்டது",
            volume = { "ஒலி அளவு $it%" },
            mute = "ஒலி நிறுத்து",
            testVoice = "குரலை சோதி",
            missingClips = "இல்லாத குரல் கோப்புகள்: ",
            exercisesTitle = "10 பயிற்சிகள் (விவரங்களுக்கு தொடுங்கள்)",
            guideStyle = { if (it) "வழிகாட்டி: பயிற்சியாளர்" else "வழிகாட்டி: கோட்டு உருவம்" },
            howtoBefore = "ஒவ்வொரு பயிற்சிக்கு முன்பும் விளக்க வீடியோக்களை காட்டு",
            recent = "சமீபத்திய பயிற்சிகள்",
            disclaimer = "பொதுவான உடற்பயிற்சி வழிகாட்டி மட்டுமே — பலன் ஒவ்வொருவருக்கும் மாறுபடும். வலி, தலைசுற்றல் " +
                "அல்லது நெஞ்சு அசௌகரியம் இருந்தால் நிறுத்துங்கள்; உடல்நலப் பிரச்சினை இருந்தால் மருத்துவரை அணுகுங்கள்.",
            start = { "தொடங்கு  ·  $it" },
            structure = { "வார்ம்-அப் + ${it.fullRounds} சுற்றுகள்" + if (it.lastRoundSize < 8) " + ${it.lastRoundSize}" else "" },
            info = {
                "கார்டியோ HIIT: முதலில் ஒருமுறை 2 வார்ம்-அப் பயிற்சிகள், பின் 8 கார்டியோ பயிற்சிகள் கொண்ட சுற்றுகள் " +
                    "(தொடக்கம் → மேம்பட்டது → கடினம்) ${if (it == SessionLength.MIN_60) "1 மணி நேரம்" else "${it.minutes} நிமிடம்"} முடியும் வரை தொடரும். பிறகு 3–5 நிமிடம் ஸ்ட்ரெச் செய்யுங்கள்.\n\n" +
                    "சிறந்த முன்னேற்றத்துக்கு: வாரம் 4–5 நாள் + ஆரோக்கியமான உணவு. பொதுவாக 4 வாரத்தில் ஸ்டாமினா மேம்படும்; " +
                    "எடை மாற்றம் பெரும்பாலும் உணவைப் பொறுத்தது, ஒவ்வொருவருக்கும் மாறுபடும்."
            },
            history = { m, e, r -> "$m நிமி · $e பயிற்சிகள் · $r முழு சுற்றுகள்" },
            steps = "படிகள்",
            easier = "குதிக்காமல் / எளிதாக: ",
            watchHowto = "▶  விளக்க வீடியோ பார்க்க",
            exercise = "பயிற்சி", restLabel = "ஓய்வு",
            warmupStatus = { n, g -> "வார்ம்-அப்  ·  $n/$g" },
            roundStatus = { r, R, n, g -> "சுற்று $r/$R  ·  பயிற்சி $n/$g" },
            left = { "$it மீதம்" },
            nextUpPreview = "அடுத்தது — முன்னோட்டம்",
            lastRest = "கடைசி ஓய்வு",
            next = { "அடுத்து: $it" },
            lastExercise = "கடைசி பயிற்சி!",
            finalRest = "கடைசி ஓய்வு — மெதுவாக மூச்சு விடுங்கள். கிட்டத்தட்ட முடிந்தது!",
            noJump = { "குதிக்காமல்: $it" },
            pause = "இடைநிறுத்து", resume = "தொடர்", stop = "நிறுத்து",
            stopTitle = "பயிற்சியை நிறுத்தவா?",
            stopText = "இது பயிற்சியை முடித்து டைமரை மீட்டமைக்கும்.",
            keepGoing = "தொடர்கிறேன்",
            paused = {
                when (it) {
                    PauseReason.BACKGROUND -> "இடைநிறுத்தப்பட்டது — செயலி திரையை விட்டு வெளியேறியது. தொடர \"தொடர்\" அழுத்துங்கள்."
                    PauseReason.AUDIO_INTERRUPTION -> "இடைநிறுத்தப்பட்டது — ஒலி இடையூறு (எ.கா. அழைப்பு). தொடர \"தொடர்\" அழுத்துங்கள்."
                    else -> "இடைநிறுத்தப்பட்டது"
                }
            },
            greatJob = "அருமை!",
            completed = "பயிற்சி முடிந்தது",
            session = "நேரம்", exercisesDone = "முடித்த பயிற்சிகள்", fullRounds = "முழு கார்டியோ சுற்றுகள்",
            activeTime = "செயலில் இருந்த நேரம்",
            coolDown = "கூல் டவுன்: 3–5 நிமிடம் மெதுவான நடை மற்றும் லேசான ஸ்ட்ரெச். தண்ணீர் குடியுங்கள்!",
            backHome = "முகப்புக்கு திரும்பு",
            howto = "எப்படி செய்வது", close = "மூடு", replay = "↻  மீண்டும் பார்", back = "◀ பின்", home = "முகப்பு",
            nextBtn = "அடுத்து ▶", startWorkout = "பயிற்சியை தொடங்கு", skipVideos = "வீடியோக்களை தவிர்த்து தொடங்கு",
            gotIt = "புரிந்தது", videoMissing = "விளக்க வீடியோ இல்லை",
            motivation = {
                when (it) {
                    Cue.KEEP_GOING -> "தொடருங்கள், நன்றாக செய்கிறீர்கள்!"
                    Cue.YOU_GOT_THIS -> "உங்களால் முடியும்!"
                    Cue.GREAT_FORM -> "சரியான முறை, உறுதியாக இருங்கள்!"
                    Cue.PUSH_HARDER -> "இன்னும் கொஞ்சம் வேகமாக!"
                    Cue.STRONGER -> "ஒவ்வொரு முயற்சியும் உங்களை வலிமையாக்கும்!"
                    Cue.DONT_STOP -> "இப்போது நிறுத்தாதீர்கள்!"
                    Cue.WARMUP -> "மெதுவாக, உடலை தயார் செய்யுங்கள்."
                    Cue.HALFWAY -> "பாதி முடிந்தது! அருமை!"
                    Cue.LAST_ROUND -> "கடைசி சுற்று! முழு சக்தியுடன் செய்யுங்கள்!"
                    Cue.FINAL_PUSH -> "கடைசி பயிற்சி! வலிமையாக முடியுங்கள்!"
                    Cue.BREATHE -> "ஆழமாக மூச்சு விடுங்கள். அருமையாக செய்கிறீர்கள்."
                    Cue.GREAT_WORK -> "நல்ல வேலை! அடுத்ததுக்கு தயாராகுங்கள்."
                    Cue.PROUD -> "உங்களை நினைத்து பெருமைப்படுங்கள்!"
                    else -> ""
                }
            },
            dur = { if (it == SessionLength.MIN_60) "1 மணி நேரம்" else "${it.minutes} நிமிடம்" },
            coachTitle = "உங்கள் பயிற்சியாளர்",
            coachQuotes = listOf(
                "வியர்க்க தயாரா? இன்றைய கார்டியோவை சேர்ந்து செய்வோம்!",
                "தினமும் சிறு முயற்சி = பெரிய மாற்றம். தொடங்குவோம்!",
                "உங்கள் போட்டி நேற்றைய நீங்கள் மட்டுமே.",
                "ஒவ்வொரு 30 விநாடியாக — உங்களால் எதையும் செய்ய முடியும்!",
                "இன்று வாருங்கள். நாளைய நீங்கள் நன்றி சொல்வீர்கள்!",
            ),
            coachProud = "உங்கள் பயிற்சியாளர் உங்களை நினைத்து பெருமைப்படுகிறார்!",
        )
    }
}
