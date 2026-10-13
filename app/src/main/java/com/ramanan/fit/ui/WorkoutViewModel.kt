package com.ramanan.fit.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ramanan.fit.audio.VoicePlayer
import com.ramanan.fit.data.HistoryEntry
import com.ramanan.fit.data.Lang
import com.ramanan.fit.data.Prefs
import com.ramanan.fit.engine.MonotonicClock
import com.ramanan.fit.engine.PauseReason
import com.ramanan.fit.engine.Position
import com.ramanan.fit.engine.RunState
import com.ramanan.fit.engine.SessionLength
import com.ramanan.fit.engine.WorkoutSession
import com.ramanan.fit.engine.positionAt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Screen { HOME, HOWTO, WORKOUT, DONE }

data class UiState(
    val screen: Screen = Screen.HOME,
    val selected: SessionLength = SessionLength.DEFAULT,
    val runState: RunState = RunState.READY,
    val pauseReason: PauseReason? = null,
    val position: Position? = null,
    val volume: Float = 0.9f,
    val muted: Boolean = false,
    val avatar: Boolean = true,
    val lang: Lang = Lang.EN,
    val howtoBefore: Boolean = true,
    /** Exercise shown on the how-to screen, and whether it is the pre-workout walkthrough. */
    val howtoIndex: Int = 0,
    val howtoWalkthrough: Boolean = false,
    val history: List<HistoryEntry> = emptyList(),
    val missingClips: List<String> = emptyList(),
)

/**
 * Owns the single WorkoutSession and the single ticker coroutine. Survives rotation.
 * Timer logic lives in engine/ (pure Kotlin); this class only wires it to audio and UI.
 */
class WorkoutViewModel(app: Application, private val saved: SavedStateHandle) : AndroidViewModel(app) {

    private val prefs = Prefs(app)
    private val clock = MonotonicClock { SystemClock.elapsedRealtime() }
    private val voice = VoicePlayer(app) { pause(PauseReason.AUDIO_INTERRUPTION) }

    private var session: WorkoutSession? = null
    private var ticker: Job? = null

    private val _state = MutableStateFlow(
        UiState(
            selected = prefs.sessionLength,
            volume = prefs.voiceVolume,
            muted = prefs.muted,
            avatar = prefs.avatarGuides,
            lang = prefs.lang,
            howtoBefore = prefs.howtoBeforeWorkout,
            history = prefs.history(),
            missingClips = voice.missingClips,
        )
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        voice.volume = prefs.voiceVolume
        voice.muted = prefs.muted
        voice.lang = prefs.lang
        restoreAfterProcessDeath()
    }

    /** Live active-elapsed time for smooth per-frame UI (countdown + animation). */
    fun liveElapsedMs(): Long = session?.activeElapsedMs() ?: 0L

    fun livePosition(): Position? = session?.let { positionAt(it.length, it.activeElapsedMs()) }

    // ---------------- settings ----------------

    fun select(length: SessionLength) {
        if (session != null) return            // locked while a session exists
        prefs.sessionLength = length
        _state.update { it.copy(selected = length) }
    }

    fun setVolume(v: Float) {
        voice.volume = v
        prefs.voiceVolume = v
        _state.update { it.copy(volume = v) }
    }

    fun setMuted(m: Boolean) {
        voice.muted = m
        prefs.muted = m
        _state.update { it.copy(muted = m) }
    }

    fun setLang(l: Lang) {
        prefs.lang = l
        voice.lang = l
        _state.update { it.copy(lang = l) }
    }

    fun setHowtoBefore(on: Boolean) {
        prefs.howtoBeforeWorkout = on
        _state.update { it.copy(howtoBefore = on) }
    }

    // ---------------- how-to videos ----------------

    /** START on the home screen: show the how-to walkthrough first (if enabled), else start. */
    fun onStartPressed() {
        if (session != null) return
        if (prefs.howtoBeforeWorkout) {
            _state.update { it.copy(screen = Screen.HOWTO, howtoIndex = 0, howtoWalkthrough = true) }
        } else start()
    }

    /** Watch one exercise's how-to video from the home screen. */
    fun openHowTo(index: Int) {
        if (session != null) return
        _state.update { it.copy(screen = Screen.HOWTO, howtoIndex = index, howtoWalkthrough = false) }
    }

    fun howtoNext() {
        val s = _state.value
        if (s.howtoIndex < com.ramanan.fit.data.Exercises.all.size - 1) {
            _state.update { it.copy(howtoIndex = it.howtoIndex + 1) }
        } else if (s.howtoWalkthrough) start() else closeHowTo()
    }

    fun howtoBack() {
        if (_state.value.howtoIndex > 0) _state.update { it.copy(howtoIndex = it.howtoIndex - 1) } else closeHowTo()
    }

    fun closeHowTo() {
        if (session != null) return
        _state.update { it.copy(screen = Screen.HOME) }
    }

    fun setAvatar(on: Boolean) {
        prefs.avatarGuides = on
        _state.update { it.copy(avatar = on) }
    }

    /** Plays "Let's start!" so the user can check volume. Only from the home screen. */
    fun testVoice() {
        if (session == null) voice.play(com.ramanan.fit.engine.Cue.LETS_START)
    }

    // ---------------- controls ----------------

    fun start() {
        if (session != null) return            // duplicate Start presses are ignored
        val s = WorkoutSession(_state.value.selected, clock)
        if (!s.start()) return
        session = s
        _state.update { it.copy(screen = Screen.WORKOUT) }
        publish()
        startTicker()
    }

    fun pause(reason: PauseReason = PauseReason.USER) {
        val s = session ?: return
        if (s.pause(reason)) {
            ticker?.cancel(); ticker = null
            voice.stop()
            saveSnapshot(s)
            publish()
        } else {
            voice.stop()
        }
    }

    fun resume() {
        val s = session ?: return
        if (s.resume()) {
            clearSnapshot()
            publish()
            startTicker()
        }
    }

    /** Stop: end the session, cancel timer and audio, return to the ready screen. */
    fun stop() {
        ticker?.cancel(); ticker = null
        voice.stop()
        session = null
        clearSnapshot()
        _state.update {
            it.copy(screen = Screen.HOME, runState = RunState.READY, pauseReason = null, position = null,
                history = prefs.history())
        }
    }

    /** Called when the app leaves the foreground: reliable background timing is not implemented, so pause. */
    fun onBackgrounded() {
        if (session?.state == RunState.RUNNING) pause(PauseReason.BACKGROUND) else voice.stop()
    }

    // ---------------- internals ----------------

    private fun startTicker() {
        if (ticker?.isActive == true) return    // never two timer loops
        ticker = viewModelScope.launch {
            while (isActive) {
                val s = session ?: break
                val t = s.tick()
                t.cue?.let { voice.play(it) }
                publish()
                if (s.state == RunState.FINISHED) {
                    onFinished(t.position)
                    break
                }
                if (s.state != RunState.RUNNING) break
                delay(TICK_MS)
            }
        }
    }

    private fun onFinished(p: Position) {
        prefs.addHistory(
            HistoryEntry(System.currentTimeMillis(), p.length.minutes, p.completedExercises, p.completedRounds)
        )
        clearSnapshot()
        _state.update { it.copy(screen = Screen.DONE, history = prefs.history()) }
    }

    private fun publish() {
        val s = session
        _state.update {
            it.copy(
                runState = s?.state ?: RunState.READY,
                pauseReason = s?.pauseReason,
                position = s?.position(),
            )
        }
    }

    private fun saveSnapshot(s: WorkoutSession) {
        val snap = s.snapshot()
        saved[K_LEN] = snap.lengthName
        saved[K_ELAPSED] = snap.activeElapsedMs
        saved[K_CUE] = snap.nextCueIdx
    }

    private fun clearSnapshot() {
        saved.remove<String>(K_LEN); saved.remove<Long>(K_ELAPSED); saved.remove<Int>(K_CUE)
    }

    private fun restoreAfterProcessDeath() {
        val len = saved.get<String>(K_LEN) ?: return
        val elapsed = saved.get<Long>(K_ELAPSED) ?: return
        val cue = saved.get<Int>(K_CUE) ?: return
        val s = WorkoutSession.restorePaused(WorkoutSession.Snapshot(len, elapsed, cue), clock)
        session = s
        _state.update { it.copy(screen = Screen.WORKOUT, selected = s.length) }
        publish()
    }

    override fun onCleared() {
        ticker?.cancel()
        voice.release()
    }

    private companion object {
        const val TICK_MS = 50L
        const val K_LEN = "snap_len"
        const val K_ELAPSED = "snap_elapsed"
        const val K_CUE = "snap_cue"
    }
}
