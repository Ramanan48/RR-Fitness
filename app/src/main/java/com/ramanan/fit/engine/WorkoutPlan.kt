package com.ramanan.fit.engine

/**
 * Pure-Kotlin workout schedule. No Android imports — unit-tested on the JVM.
 *
 * One cycle = 30 s exercise + 20 s rest = 50 s. Cardio HIIT structure:
 *  - Warm-up: exercises 0 and 1, once, at the start of the session.
 *  - Then cardio rounds of exercises 2..9 (8 per round) repeat until time is up.
 *    The last round may be shorter so the session ends exactly on time.
 * Sessions include the final rest, so a session lasts exactly its selected length.
 */
object Timing {
    const val EXERCISE_MS = 30_000L
    const val REST_MS = 20_000L
    const val CYCLE_MS = EXERCISE_MS + REST_MS
    const val WARMUP_COUNT = 2
    const val ROUND_SIZE = 8
    const val EXERCISE_COUNT = WARMUP_COUNT + ROUND_SIZE
    const val COUNTDOWN_FROM = 5
}

enum class SessionLength(val minutes: Int, val label: String) {
    MIN_15(15, "15 min"),
    MIN_30(30, "30 min"),
    MIN_45(45, "45 min"),
    MIN_60(60, "1 hour");

    val totalMs: Long get() = minutes * 60_000L
    val cycles: Int get() = (totalMs / Timing.CYCLE_MS).toInt()
    /** Cardio intervals after the warm-up. */
    val mainCycles: Int get() = cycles - Timing.WARMUP_COUNT
    val fullRounds: Int get() = mainCycles / Timing.ROUND_SIZE
    /** Rounds including a shorter final round, if any. */
    val totalRounds: Int get() = (mainCycles + Timing.ROUND_SIZE - 1) / Timing.ROUND_SIZE
    /** Exercises in the final round (= ROUND_SIZE when it is a full round). */
    val lastRoundSize: Int get() = mainCycles - (totalRounds - 1) * Timing.ROUND_SIZE

    init {
        require(totalMs % Timing.CYCLE_MS == 0L) { "Session must be a whole number of cycles" }
        require(mainCycles > 0)
    }

    companion object {
        val DEFAULT = MIN_30
        fun fromName(name: String?): SessionLength = entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

/** Which of the 8 exercises a given cycle uses. */
fun exerciseForCycle(cycle: Int): Int =
    if (cycle < Timing.WARMUP_COUNT) cycle
    else Timing.WARMUP_COUNT + (cycle - Timing.WARMUP_COUNT) % Timing.ROUND_SIZE

enum class Phase { EXERCISE, REST, FINISHED }

/** Snapshot of where the session is at a given active (un-paused) elapsed time. */
data class Position(
    val length: SessionLength,
    val activeElapsedMs: Long,
    val phase: Phase,
    /** 0-based cycle number across the whole session. */
    val cycleIndex: Int,
    /** 0..9 — which exercise this cycle uses (0-1 warm-up, 2-9 cardio round). */
    val exerciseIndex: Int,
    /** true during the two warm-up cycles. */
    val isWarmup: Boolean,
    /** 0 during warm-up, then 1-based cardio round number. */
    val round: Int,
    /** 1-based position within the warm-up or the current round. */
    val exerciseNumber: Int,
    /** Exercises in the warm-up (2) or in the current round (8, or fewer in a short final round). */
    val groupSize: Int,
    val phaseElapsedMs: Long,
    val phaseRemainingMs: Long,
    /** Exercise that follows the current cycle, or null on the final cycle. */
    val nextExerciseIndex: Int?,
) {
    val sessionRemainingMs: Long get() = (length.totalMs - activeElapsedMs).coerceAtLeast(0)
    val progress: Float get() = (activeElapsedMs.toDouble() / length.totalMs).toFloat().coerceIn(0f, 1f)

    /** Big countdown number: 30→1 (exercise), 20→1 (rest). Shows 0 only when finished. */
    val displaySeconds: Int get() = ceilDiv(phaseRemainingMs, 1000L).toInt()

    /** Exercises (warm-up included) whose 30 s work interval has fully elapsed. */
    val completedExercises: Int
        get() = when (phase) {
            Phase.FINISHED -> length.cycles
            Phase.REST -> cycleIndex + 1
            Phase.EXERCISE -> cycleIndex
        }

    /** Full 8-exercise cardio rounds whose last work interval has fully elapsed. */
    val completedRounds: Int
        get() = ((completedExercises - Timing.WARMUP_COUNT).coerceAtLeast(0)) / Timing.ROUND_SIZE
}

internal fun ceilDiv(a: Long, b: Long): Long = if (a <= 0) 0 else (a + b - 1) / b

private fun groupInfo(length: SessionLength, cycle: Int): Triple<Int, Int, Int> {
    // (round, exerciseNumber, groupSize)
    if (cycle < Timing.WARMUP_COUNT) return Triple(0, cycle + 1, Timing.WARMUP_COUNT)
    val m = cycle - Timing.WARMUP_COUNT
    val round = m / Timing.ROUND_SIZE + 1
    val size = if (round == length.totalRounds) length.lastRoundSize else Timing.ROUND_SIZE
    return Triple(round, m % Timing.ROUND_SIZE + 1, size)
}

fun positionAt(length: SessionLength, activeElapsedMs: Long): Position {
    val elapsed = activeElapsedMs.coerceIn(0L, length.totalMs)
    if (elapsed >= length.totalMs) {
        val last = length.cycles - 1
        val (r, n, g) = groupInfo(length, last)
        return Position(
            length, length.totalMs, Phase.FINISHED, last, exerciseForCycle(last), false,
            r, n, g, Timing.REST_MS, 0L, null,
        )
    }
    val cycle = (elapsed / Timing.CYCLE_MS).toInt()
    val inCycle = elapsed - cycle * Timing.CYCLE_MS
    val isExercise = inCycle < Timing.EXERCISE_MS
    val phaseElapsed = if (isExercise) inCycle else inCycle - Timing.EXERCISE_MS
    val phaseLen = if (isExercise) Timing.EXERCISE_MS else Timing.REST_MS
    val (round, number, size) = groupInfo(length, cycle)
    return Position(
        length = length,
        activeElapsedMs = elapsed,
        phase = if (isExercise) Phase.EXERCISE else Phase.REST,
        cycleIndex = cycle,
        exerciseIndex = exerciseForCycle(cycle),
        isWarmup = cycle < Timing.WARMUP_COUNT,
        round = round,
        exerciseNumber = number,
        groupSize = size,
        phaseElapsedMs = phaseElapsed,
        phaseRemainingMs = phaseLen - phaseElapsed,
        nextExerciseIndex = if (cycle + 1 < length.cycles) exerciseForCycle(cycle + 1) else null,
    )
}

/** Voice cues. Each maps to one clip per language, stored in assets/voice.pack as "<lang>/<asset>". */
enum class Cue(val asset: String) {
    LETS_START("lets_start"),
    FIVE("five"), FOUR("four"), THREE("three"), TWO("two"), ONE("one"),
    TAKE_REST("take_rest"),
    NEXT_EXERCISE("next_exercise"),
    COMPLETED("completed"),

    // Motivation — spoken 15 s into an exercise (when the display shows 15).
    KEEP_GOING("keep_going"), YOU_GOT_THIS("you_got_this"), GREAT_FORM("great_form"),
    PUSH_HARDER("push_harder"), STRONGER("stronger"), DONT_STOP("dont_stop"),
    WARMUP("warmup"), HALFWAY("halfway"), LAST_ROUND("last_round"), FINAL_PUSH("final_push"),

    // Rest praise — spoken 3 s into each rest.
    BREATHE("breathe"), GREAT_WORK("great_work"), PROUD("proud");

    companion object {
        val COUNTDOWN = listOf(FIVE, FOUR, THREE, TWO, ONE)
        val MOTIVATION_ROTATION = listOf(KEEP_GOING, YOU_GOT_THIS, GREAT_FORM, PUSH_HARDER, STRONGER, DONT_STOP)
        val REST_ROTATION = listOf(BREATHE, GREAT_WORK, PROUD)
    }
}

/** When the mid-exercise motivation line is spoken (ms into the exercise). */
const val MOTIVATION_AT_MS = 15_000L
/** When the rest praise line is spoken (ms into the rest). */
const val REST_PRAISE_AT_MS = 3_000L

/** The motivation line for a cycle: warm-up, last round, final exercise, halfway, else a rotation. */
fun motivationFor(length: SessionLength, cycle: Int): Cue {
    val firstOfLastRound = Timing.WARMUP_COUNT + (length.totalRounds - 1) * Timing.ROUND_SIZE
    val halfwayCycle = ((length.totalMs / 2) / Timing.CYCLE_MS).toInt()
    return when {
        cycle < Timing.WARMUP_COUNT -> Cue.WARMUP
        cycle == length.cycles - 1 -> Cue.FINAL_PUSH
        length.totalRounds > 1 && cycle == firstOfLastRound -> Cue.LAST_ROUND
        cycle == halfwayCycle -> Cue.HALFWAY
        else -> Cue.MOTIVATION_ROTATION[cycle % Cue.MOTIVATION_ROTATION.size]
    }
}

/** The praise line spoken during a cycle's rest. */
fun restPraiseFor(length: SessionLength, cycle: Int): Cue =
    if (cycle == length.cycles - 1) Cue.PROUD else Cue.REST_ROTATION[cycle % Cue.REST_ROTATION.size]

data class TimedCue(val atMs: Long, val cue: Cue)

/**
 * Every cue in the session, sorted by time.
 * - Cycle start: "Let's start!" (first) or "Next exercise."
 * - 15 s into each exercise: a motivation line ([motivationFor]).
 * - Final five seconds of exercise and of rest: Five…One, one per second
 *   ("Five" when the display turns 5, "One" when it turns 1).
 * - End of exercise: "Take a rest." 3 s into the rest: a praise line ([restPraiseFor]).
 * - End of session (after the final rest): "Great job!…"
 */
fun cueSchedule(length: SessionLength): List<TimedCue> {
    val out = ArrayList<TimedCue>(length.cycles * 15 + 1)
    for (c in 0 until length.cycles) {
        val start = c * Timing.CYCLE_MS
        out += TimedCue(start, if (c == 0) Cue.LETS_START else Cue.NEXT_EXERCISE)
        out += TimedCue(start + MOTIVATION_AT_MS, motivationFor(length, c))
        addCountdown(out, start + Timing.EXERCISE_MS)
        out += TimedCue(start + Timing.EXERCISE_MS, Cue.TAKE_REST)
        out += TimedCue(start + Timing.EXERCISE_MS + REST_PRAISE_AT_MS, restPraiseFor(length, c))
        addCountdown(out, start + Timing.CYCLE_MS)
    }
    out += TimedCue(length.totalMs, Cue.COMPLETED)
    return out
}

private fun addCountdown(out: MutableList<TimedCue>, phaseEnd: Long) {
    Cue.COUNTDOWN.forEachIndexed { i, cue ->
        out += TimedCue(phaseEnd - (Timing.COUNTDOWN_FROM - i) * 1000L, cue)
    }
}
