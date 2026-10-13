package com.ramanan.fit.data

import androidx.annotation.RawRes
import com.ramanan.fit.R

enum class Lang(val code: String, val label: String) {
    EN("en", "English"), TA("ta", "தமிழ்");

    companion object {
        fun fromCode(c: String?): Lang = entries.firstOrNull { it.code == c } ?: EN
    }
}

enum class Level(private val en: String, private val ta: String) {
    WARMUP("Warm-up", "வார்ம்-அப்"),
    BEGINNER("Beginner", "தொடக்கம்"),
    ADVANCED("Advanced", "மேம்பட்டது"),
    HARDER("Harder", "கடினம்");

    fun label(lang: Lang) = if (lang == Lang.TA) ta else en
}

/** Text for one exercise in one language. */
data class ExText(
    val name: String,
    val steps: List<String>,      // start → movement → return
    val tips: List<String>,
    val easier: String?,          // no-jump / easier option
    val prep: String,             // shown during the preceding rest
)

/**
 * The 10 cardio exercises, in session order. Index 0-1 = warm-up (once per session),
 * 2-9 = one cardio round (repeats). English text includes simple Tanglish tips;
 * Tamil text is in Tamil script.
 */
data class Exercise(
    val id: String,               // matches assets/poses/poses.json
    val level: Level,
    val en: ExText,
    val ta: ExText,
    @RawRes val videoEn: Int,
    @RawRes val videoTa: Int,
) {
    fun t(lang: Lang) = if (lang == Lang.TA) ta else en
    @RawRes fun video(lang: Lang) = if (lang == Lang.TA) videoTa else videoEn
}

object Exercises {
    val all: List<Exercise> = listOf(
        Exercise(
            "march_arms", Level.WARMUP,
            en = ExText(
                "March with Arm Swings",
                listOf("Stand tall, feet hip-width.", "March on the spot, knees to mid height, arms swinging big.", "Keep a steady, comfortable rhythm."),
                listOf("Easy pace — idhu warm-up dhaan, body-ah ready pannunga.", "Relaxed shoulders, breathe steadily."),
                null, "Warm-up first: march pannunga, kai nalla swing pannunga.",
            ),
            ta = ExText(
                "கை வீச்சுடன் நடை",
                listOf("நேராக நில்லுங்கள், கால்கள் இடுப்பு அகலத்தில்.", "ஒரே இடத்தில் நடங்கள்; முழங்கால் பாதி உயரம், கைகளை நன்றாக வீசுங்கள்.", "சீரான, வசதியான வேகத்தில் தொடருங்கள்."),
                listOf("மெதுவாக — இது வார்ம்-அப் தான்.", "தோள்களை தளர்வாக வைத்து சீராக மூச்சு விடுங்கள்."),
                null, "முதலில் வார்ம்-அப்: நடந்து, கைகளை வீசுங்கள்.",
            ),
            R.raw.howto_march_arms_en, R.raw.howto_march_arms_ta,
        ),
        Exercise(
            "step_jacks", Level.WARMUP,
            en = ExText(
                "Step Jacks",
                listOf("Stand tall, arms by your sides.", "Step one foot out as both arms sweep overhead.", "Step back in, arms down. Alternate sides."),
                listOf("Soft knees — mutti lock pannaadheenga.", "Full arm reach, shoulders warm aagum."),
                null, "Kai pakkathula, ready-ah nillunga.",
            ),
            ta = ExText(
                "ஸ்டெப் ஜாக்ஸ்",
                listOf("நேராக நில்லுங்கள், கைகள் பக்கத்தில்.", "ஒரு காலை பக்கவாட்டில் வைத்து, இரு கைகளையும் மேலே தூக்குங்கள்.", "காலை உள்ளே கொண்டு வந்து கைகளை இறக்குங்கள். மாறி மாறி செய்யுங்கள்."),
                listOf("முழங்காலை லாக் செய்யாதீர்கள்.", "கைகளை முழுமையாக மேலே நீட்டுங்கள்."),
                null, "கைகள் பக்கத்தில், தயாராக நில்லுங்கள்.",
            ),
            R.raw.howto_step_jacks_en, R.raw.howto_step_jacks_ta,
        ),
        Exercise(
            "high_knees", Level.BEGINNER,
            en = ExText(
                "High Knees",
                listOf("Stand tall, arms bent.", "Drive one knee up to hip height, then the other — fast.", "Stay on the balls of your feet, pump the arms."),
                listOf("Chest up — kuninju paakaadheenga.", "Land softly, quiet feet."),
                "Fast march: oru kaal eppovum tharaiyila.", "Kaal hip-width, kai bend panni ready.",
            ),
            ta = ExText(
                "ஹை நீஸ்",
                listOf("நேராக நில்லுங்கள், கைகளை மடக்குங்கள்.", "ஒரு முழங்காலை இடுப்பு உயரம் வரை தூக்குங்கள், பின் மற்றொன்று — வேகமாக.", "கால் விரல் பகுதியில் இருந்து, கைகளை அசைத்தபடி செய்யுங்கள்."),
                listOf("மார்பை நிமிர்த்துங்கள், குனியாதீர்கள்.", "மென்மையாக கால் வையுங்கள்."),
                "வேகமான நடை: ஒரு கால் எப்போதும் தரையில்.", "கால்கள் இடுப்பு அகலத்தில், கைகளை மடக்கி தயாராகுங்கள்.",
            ),
            R.raw.howto_high_knees_en, R.raw.howto_high_knees_ta,
        ),
        Exercise(
            "butt_kicks", Level.BEGINNER,
            en = ExText(
                "Butt Kicks",
                listOf("Stand tall, slight forward lean.", "Kick one heel up towards the hip, then the other — quick rhythm.", "Arms pump like running."),
                listOf("Knee points down — thigh munnaadi poga vendaam.", "Core lightly tight."),
                "Walk-pace heel curls, no hop.", "Lightly munnaadi lean pannunga.",
            ),
            ta = ExText(
                "பட் கிக்ஸ்",
                listOf("நேராக நில்லுங்கள், சற்று முன்னே சாயுங்கள்.", "ஒரு குதிகாலை இடுப்பை நோக்கி உதையுங்கள், பின் மற்றொன்று — வேகமாக.", "ஓடுவது போல கைகளை அசையுங்கள்."),
                listOf("முழங்கால் கீழ்நோக்கி இருக்க வேண்டும்.", "வயிற்றை லேசாக இறுக்குங்கள்."),
                "மெதுவான நடையுடன் குதிகால் மடக்குதல், குதிக்காமல்.", "லேசாக முன்னே சாய்ந்து தயாராகுங்கள்.",
            ),
            R.raw.howto_butt_kicks_en, R.raw.howto_butt_kicks_ta,
        ),
        Exercise(
            "shadow_boxing", Level.BEGINNER,
            en = ExText(
                "Shadow Boxing",
                listOf("Boxing stance, fists up near the chin.", "Punch straight forward with one arm, then the other — quick jab, cross.", "Stay light on your feet with a small bounce."),
                listOf("Bring each punch back to the chin — guard up!", "Don't lock the elbow — kai muzhusaa lock pannaadheenga."),
                "Slower punches, no bounce.", "Fists up, oru kaal munnaadi. Ready!",
            ),
            ta = ExText(
                "ஷேடோ பாக்ஸிங்",
                listOf("குத்துச்சண்டை நிலையில் நில்லுங்கள், முஷ்டிகள் தாடை அருகே.", "ஒரு கையால் நேராக குத்துங்கள், பின் மற்ற கை — வேகமாக.", "லேசாக துள்ளியபடி கால்களை தயாராக வையுங்கள்."),
                listOf("ஒவ்வொரு குத்துக்கும் பின் கையை தாடைக்கு திருப்புங்கள்.", "முழங்கையை முழுதாக லாக் செய்யாதீர்கள்."),
                "மெதுவான குத்துகள், துள்ளல் இல்லாமல்.", "முஷ்டிகளை உயர்த்தி, ஒரு காலை முன்னே வைத்து தயாராகுங்கள்.",
            ),
            R.raw.howto_shadow_boxing_en, R.raw.howto_shadow_boxing_ta,
        ),
        Exercise(
            "jumping_jacks", Level.ADVANCED,
            en = ExText(
                "Jumping Jacks",
                listOf("Feet together, arms down.", "Jump feet wide as arms go overhead.", "Jump back together, arms down. Keep going."),
                listOf("Land on the front of the foot, soft knees.", "Steady rhythm — speed-ku form vida koodaadhu."),
                "Step jacks — jump illaama side step.", "Kaal sethu, kai keezha. Jump-ku ready!",
            ),
            ta = ExText(
                "ஜம்பிங் ஜாக்ஸ்",
                listOf("கால்கள் சேர்த்து, கைகள் கீழே.", "கால்களை அகலமாக குதித்து, கைகளை மேலே தூக்குங்கள்.", "மீண்டும் கால்களை சேர்த்து குதித்து, கைகளை இறக்குங்கள்."),
                listOf("கால் விரல் பகுதியில் மென்மையாக இறங்குங்கள்.", "சீரான வேகம் — வேகத்துக்காக சரியான முறையை விடாதீர்கள்."),
                "ஸ்டெப் ஜாக்ஸ் — குதிக்காமல் பக்கவாட்டில் அடி வையுங்கள்.", "கால்கள் சேர்த்து, கைகள் கீழே. குதிக்க தயார்!",
            ),
            R.raw.howto_jumping_jacks_en, R.raw.howto_jumping_jacks_ta,
        ),
        Exercise(
            "skaters", Level.ADVANCED,
            en = ExText(
                "Skaters",
                listOf("Stand on one leg, slight squat.", "Hop sideways onto the other leg; back foot sweeps behind.", "Swing the arms across, then hop back to the first side."),
                listOf("Land soft and bend the standing knee.", "Small hops first, then go wider."),
                "Side step + tap back foot behind, no hop.", "Oru kaal-la light squat, side-ku hop panna ready.",
            ),
            ta = ExText(
                "ஸ்கேட்டர்ஸ்",
                listOf("ஒரு காலில் லேசான ஸ்குவாட்டில் நில்லுங்கள்.", "பக்கவாட்டில் மற்ற காலுக்கு தாவுங்கள்; பின் கால் பின்னால் செல்லும்.", "கைகளை குறுக்கே வீசி, மீண்டும் முதல் பக்கத்துக்கு தாவுங்கள்."),
                listOf("மென்மையாக இறங்கி, நிற்கும் முழங்காலை மடக்குங்கள்.", "முதலில் சிறிய தாவல், பின் அகலமாக."),
                "பக்கவாட்டில் அடி வைத்து பின் காலை தொடுங்கள், குதிக்காமல்.", "ஒரு காலில் லேசான ஸ்குவாட், பக்கவாட்டில் தாவ தயார்.",
            ),
            R.raw.howto_skaters_en, R.raw.howto_skaters_ta,
        ),
        Exercise(
            "mountain_climbers", Level.ADVANCED,
            en = ExText(
                "Mountain Climbers",
                listOf("High plank: hands under shoulders, body straight.", "Drive one knee towards the chest.", "Switch legs quickly, like running on the floor."),
                listOf("Hips low — hip mela thookaadheenga.", "Hands firm, shoulders over wrists."),
                "Hands on a chair or bed, slow alternating steps.", "Floor-la plank position-ku ready aagunga.",
            ),
            ta = ExText(
                "மவுண்டன் கிளைம்பர்ஸ்",
                listOf("ஹை பிளாங்க்: கைகள் தோள்களுக்கு கீழே, உடல் நேராக.", "ஒரு முழங்காலை மார்பை நோக்கி கொண்டு வாருங்கள்.", "தரையில் ஓடுவது போல கால்களை வேகமாக மாற்றுங்கள்."),
                listOf("இடுப்பை தாழ்வாக வையுங்கள், மேலே தூக்காதீர்கள்.", "கைகள் உறுதியாக, தோள்கள் மணிக்கட்டுக்கு மேலே."),
                "நாற்காலி அல்லது கட்டிலில் கைகளை வைத்து மெதுவாக கால்களை மாற்றுங்கள்.", "தரையில் பிளாங்க் நிலைக்கு தயாராகுங்கள்.",
            ),
            R.raw.howto_mountain_climbers_en, R.raw.howto_mountain_climbers_ta,
        ),
        Exercise(
            "squat_jumps", Level.HARDER,
            en = ExText(
                "Squat Jumps",
                listOf("Feet shoulder-width, squat down with arms back.", "Jump up explosively, arms swinging overhead.", "Land softly and go straight into the next squat."),
                listOf("Land quietly on the balls of the feet — soft knees.", "Knees follow the toes — mutti ulla vara koodaadhu."),
                "Squat and rise onto your toes — no jump.", "Kaal shoulder-width, squat-ku ready!",
            ),
            ta = ExText(
                "ஸ்குவாட் ஜம்ப்ஸ்",
                listOf("கால்கள் தோள் அகலத்தில், கைகளை பின்னால் வைத்து ஸ்குவாட் செய்யுங்கள்.", "கைகளை மேலே வீசி வேகமாக மேலே குதியுங்கள்.", "மென்மையாக இறங்கி உடனே அடுத்த ஸ்குவாட் செய்யுங்கள்."),
                listOf("கால் விரல் பகுதியில் சத்தமில்லாமல் இறங்குங்கள்.", "முழங்கால்கள் கால் விரல்களின் திசையில் இருக்க வேண்டும்."),
                "ஸ்குவாட் செய்து கால் விரல்களில் எழுங்கள் — குதிக்காமல்.", "கால்கள் தோள் அகலத்தில், ஸ்குவாட்டுக்கு தயார்!",
            ),
            R.raw.howto_squat_jumps_en, R.raw.howto_squat_jumps_ta,
        ),
        Exercise(
            "burpees", Level.HARDER,
            en = ExText(
                "Burpees",
                listOf("Squat and place both hands on the floor.", "Jump feet back to a plank, then jump them back in.", "Stand and jump up, arms overhead. Repeat."),
                listOf("Back straight in the plank — idupu thongakoodaadhu.", "Your pace — steady-ah pannunga."),
                "Step back, step in, stand up and reach — no jumps.", "Last one — Burpees! Full energy, kaal shoulder-width.",
            ),
            ta = ExText(
                "பர்பீஸ்",
                listOf("ஸ்குவாட் செய்து இரு கைகளையும் தரையில் வையுங்கள்.", "கால்களை பின்னால் குதித்து பிளாங்க், பின் மீண்டும் உள்ளே குதியுங்கள்.", "எழுந்து, கைகளை மேலே தூக்கி குதியுங்கள். மீண்டும் செய்யுங்கள்."),
                listOf("பிளாங்கில் முதுகு நேராக — இடுப்பு தொங்கக்கூடாது.", "உங்கள் வேகத்தில், சீராக செய்யுங்கள்."),
                "பின்னால் அடி, உள்ளே அடி, எழுந்து கைகளை நீட்டுங்கள் — குதிக்காமல்.", "கடைசி பயிற்சி — பர்பீஸ்! முழு சக்தியுடன்.",
            ),
            R.raw.howto_burpees_en, R.raw.howto_burpees_ta,
        ),
    )

    init {
        require(all.size == 10)
    }
}
