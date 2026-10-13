package com.ramanan.fit.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import com.ramanan.fit.data.Lang
import com.ramanan.fit.engine.Cue
import org.json.JSONObject

/**
 * Plays the bundled, prerecorded voice clips (packed in assets/voice.pack). Fully offline — no TTS.
 * Both languages are loaded up front (small clips); [lang] picks which one plays.
 *
 * - One voice at a time: a new clip stops the current one, so nothing overlaps or queues.
 * - stop() cuts audio immediately (used on Pause / Stop / backgrounding).
 * - Audio focus is held only while a clip plays (other audio ducks briefly).
 *   A transient focus loss (e.g. an incoming call) stops the clip and reports an interruption.
 */
class VoicePlayer(
    context: Context,
    private val onInterrupted: () -> Unit,
) {
    private val app = context.applicationContext
    private val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val main = Handler(Looper.getMainLooper())

    private val attrs = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private val pool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(attrs).build()
    private val soundIds = HashMap<Pair<Lang, Cue>, Int>()
    private val durationsMs = HashMap<Pair<Lang, Cue>, Long>()
    private val loaded = HashSet<Int>()
    private var currentStream = 0
    private var holdingFocus = false

    var lang: Lang = Lang.EN
        set(value) { if (field != value) stop(); field = value }

    var volume: Float = 0.9f
    var muted: Boolean = false
        set(value) { field = value; if (value) stop() }

    /** Clips that failed to load — reported in the UI instead of silently missing. */
    val missingClips: List<String>
        get() = Lang.entries.flatMap { l -> Cue.entries.filter { (l to it) !in soundIds }.map { "${l.code}/${it.asset}" } }

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                stopStreamOnly()
                holdingFocus = false
                if (change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) onInterrupted()
            }
            else -> Unit
        }
    }

    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(attrs)
        .setOnAudioFocusChangeListener(focusListener, main)
        .build()

    private val releaseFocus = Runnable { abandonFocus() }

    /** Kept open for the player's lifetime: SoundPool reads clips from it at byte offsets. */
    private var packFd: android.content.res.AssetFileDescriptor? = null

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status -> if (status == 0) loaded += sampleId }
        // All clips live back-to-back in assets/voice.pack; assets/voice.json maps
        // "<lang>/<cue>" → [offset, length, durationMs] (made by tools/pack_voice.py).
        try {
            val index = JSONObject(app.assets.open("voice.json").bufferedReader().use { it.readText() })
            val afd = app.assets.openFd("voice.pack")
            packFd = afd
            for (l in Lang.entries) for (cue in Cue.entries) {
                val e = index.optJSONArray("${l.code}/${cue.asset}") ?: continue
                soundIds[l to cue] = pool.load(afd.fileDescriptor, afd.startOffset + e.getLong(0), e.getLong(1), 1)
                durationsMs[l to cue] = e.getLong(2)
            }
        } catch (_: Exception) {
            // anything not loaded shows up in missingClips
        }
    }

    fun play(cue: Cue) {
        stopStreamOnly()
        if (muted || volume <= 0f) return
        val id = soundIds[lang to cue] ?: soundIds[Lang.EN to cue] ?: return
        if (id !in loaded) return
        if (!holdingFocus) {
            holdingFocus = audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            if (!holdingFocus) return // e.g. during a phone call: stay silent
        }
        currentStream = pool.play(id, volume, volume, 1, 0, 1f)
        main.removeCallbacks(releaseFocus)
        main.postDelayed(releaseFocus, (durationsMs[lang to cue] ?: 2000L) + 150L)
    }

    /** Immediately silence any clip and release audio focus. */
    fun stop() {
        stopStreamOnly()
        main.removeCallbacks(releaseFocus)
        abandonFocus()
    }

    private fun stopStreamOnly() {
        if (currentStream != 0) {
            pool.stop(currentStream)
            currentStream = 0
        }
    }

    private fun abandonFocus() {
        if (holdingFocus) {
            audioManager.abandonAudioFocusRequest(focusRequest)
            holdingFocus = false
        }
    }

    fun release() {
        stop()
        pool.release()
        try { packFd?.close() } catch (_: Exception) {}
    }
}
