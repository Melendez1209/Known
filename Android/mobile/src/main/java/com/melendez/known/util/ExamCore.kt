package com.melendez.known.util

import com.melendez.known.data.entity.ExamScore
import com.melendez.known.data.entity.ExamWithTotal
import com.melendez.known.data.entity.SubjectStat
import com.melendez.known.util.settings.examSubjectKeys
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Pure helpers for the exam feature, covering score-field parsing, slider arithmetic and the
 * aggregates shown on the history and detail screens. Kept free of Android types so every function
 * can be unit tested on the JVM; anything needing a Context stays in the view model or the screens.
 */

/** Longest string a score field may hold, which also keeps every value inside `Int` range. */
private const val MAX_SCORE_LENGTH = 7

/**
 * One editable row of the input screen: a subject key, whether the row is ticked, and its two text
 * fields. Rows that are not ticked are never persisted.
 */
data class ScoreInput(
    val subjectKey: String,
    val checked: Boolean = true,
    val mark: String = "",
    val fullMark: String = "150"
)

/** Full mark every score field starts with while the form is still blank. */
const val DEFAULT_FULL_MARK = "150"

/** The three subjects every exam is graded on, which are therefore never optional. */
const val COMPULSORY_SUBJECT_COUNT = 3

/**
 * The blank form shown when a new exam is created: every subject listed, every field reset.
 *
 * The compulsory subjects are always ticked because their rows carry no checkbox. The optional
 * ones follow [selectedSubjects], the student's own selection stored in the general settings, so
 * the input screen opens with the subjects they actually sit. An empty set means no selection has
 * been stored yet, in which case the historical default - every subject but PE - is kept.
 */
fun defaultScoreInputs(selectedSubjects: Set<String>): List<ScoreInput> =
    examSubjectKeys.mapIndexed { index, key ->
        ScoreInput(
            subjectKey = key,
            checked = when {
                index < COMPULSORY_SUBJECT_COUNT -> true
                selectedSubjects.isEmpty() -> index != examSubjectKeys.lastIndex
                else -> key in selectedSubjects
            },
            mark = "",
            fullMark = DEFAULT_FULL_MARK
        )
    }

/**
 * Parses a score the user may type, e.g. `100`, `98.5`, `.5`. Returns `null` when the input is not
 * a usable number, so callers never have to guard against `NumberFormatException`.
 */
fun parseScoreOrNull(input: String): Float? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return null
    val value = trimmed.toFloatOrNull() ?: return null
    if (!value.isFinite() || value < 0f) return null
    return value
}

/**
 * Validates an entry in a score field. Empty is allowed so the field can be cleared; otherwise the
 * value must be a non-negative integer or half-point no longer than [MAX_SCORE_LENGTH] characters,
 * matching the slider's step size.
 */
fun isValidScoreInput(input: String): Boolean {
    if (input.isEmpty()) return true
    if (input.length > MAX_SCORE_LENGTH) return false
    val value = parseScoreOrNull(input) ?: return false
    return value == value.toInt().toFloat() || input.endsWith(".5")
}

/**
 * Normalises a score for display, dropping a redundant trailing `.0` so `150.0` reads as `150`.
 * Unparsable input is returned unchanged rather than throwing.
 */
fun formatScoreInput(input: String): String {
    if (input.isEmpty()) return ""
    val value = input.toFloatOrNull() ?: return input
    return if (value == value.toInt().toFloat()) value.toInt().toString() else input
}

/**
 * The single pipeline a score text field runs on every change: a trailing `.` is completed to `.5`
 * so half-points can be typed naturally, then the result is normalised. Input that would not pass
 * [isValidScoreInput] is returned untouched so the field can hold it while the user keeps typing.
 */
fun processScoreInput(input: String): String {
    if (input.isEmpty()) return input
    val completed = if (input.endsWith(".")) input + "5" else input
    return if (isValidScoreInput(completed)) formatScoreInput(completed) else input
}

/**
 * Slider position for a row, as `0f..1f`. Returns `0f` when either field is blank or unparsable, so
 * a partially filled form can never throw.
 */
fun sliderValue(mark: String, fullMark: String): Float {
    val markValue = parseScoreOrNull(mark) ?: return 0f
    val fullValue = parseScoreOrNull(fullMark) ?: return 0f
    if (fullValue <= 0f) return 0f
    return (markValue / fullValue).coerceIn(0f, 1f)
}

/**
 * Number of slider stops for a row. The mark is snapped to half-point increments, hence two stops
 * per full-mark unit. Returns `0` when [fullMark] is blank, unparsable or zero, so a cleared or
 * half-typed full mark can no longer crash the slider.
 */
fun sliderSteps(fullMark: String): Int {
    val fullValue = parseScoreOrNull(fullMark) ?: return 0
    if (fullValue <= 0f) return 0
    return ((fullValue * 2).toInt() - 1).coerceAtLeast(0)
}

/**
 * Snaps a fractional slider position back to the nearest half-point on a `0..[fullMark]` scale, so
 * dragging never produces a value the input fields would reject. Returns `""` when [fullMark] is
 * unusable.
 */
fun snappedScore(fraction: Float, fullMark: String): String {
    val fullValue = parseScoreOrNull(fullMark) ?: return ""
    val rounded = (fullValue * fraction * 2f).roundToInt() / 2f
    return formatScoreInput(rounded.toString())
}

/** Why a set of input rows cannot be saved yet. */
enum class ScoreValidation {
    /** Every ticked row is usable and at least one mark has been typed. */
    VALID,

    /** Nothing has been typed into any ticked row. */
    NO_MARK_ENTERED,

    /** A row carrying a mark that is unparsable or greater than its full mark. */
    MARK_EXCEEDS_FULL
}

/**
 * Turns the input screen's rows into persistable [ExamScore] rows. Unticked and blank rows are
 * skipped rather than crashing the save; a mark greater than its full mark, or a full mark that
 * does not parse, is reported through [onInvalidRow] so the caller can surface an error instead of
 * silently dropping the row.
 *
 * Returns an empty list when [inputs] contains no usable score at all.
 */
fun buildExamScores(
    examId: Long,
    inputs: List<ScoreInput>,
    onInvalidRow: (ScoreInput) -> Unit = {}
): List<ExamScore> = inputs.mapNotNull { input ->
    if (!input.checked || input.mark.isBlank()) return@mapNotNull null
    val mark = parseScoreOrNull(input.mark)
    val fullMark = parseScoreOrNull(input.fullMark)
    if (mark == null || fullMark == null || mark > fullMark) {
        onInvalidRow(input)
        return@mapNotNull null
    }
    ExamScore(examId = examId, subjectKey = input.subjectKey, mark = mark, fullMark = fullMark)
}

/**
 * The subjects of [order] that actually carry a score, in display order. Subjects the student
 * never ticked, or left blank, have no [ExamScore] row at all, so the detail screen hides them
 */
fun recordedSubjectKeys(order: List<String>, scores: List<ExamScore>): List<String> =
    order.filter { key -> scores.any { it.subjectKey == key } }

/** Whether [inputs] can be saved, so the caller can block the save and explain why. */
fun validateScoreInputs(inputs: List<ScoreInput>): ScoreValidation {
    val ticked = inputs.filter { it.checked }
    if (ticked.none { it.mark.isNotBlank() }) return ScoreValidation.NO_MARK_ENTERED
    // A ticked row the student left empty is skipped on save, so only rows carrying a mark count
    val invalid = ticked.any { input ->
        if (input.mark.isBlank()) return@any false
        val mark = parseScoreOrNull(input.mark)
        val fullMark = parseScoreOrNull(input.fullMark)
        mark == null || fullMark == null || mark > fullMark
    }
    return if (invalid) ScoreValidation.MARK_EXCEEDS_FULL else ScoreValidation.VALID
}

/** Sum of every recorded mark, used for the totals shown in the list and on the detail screen. */
fun totalMark(scores: List<ExamScore>): Float = scores.sumOf { it.mark.toDouble() }.toFloat()

/** Sum of every full mark, i.e. the denominator of the overall percentage. */
fun totalFullMark(scores: List<ExamScore>): Float =
    scores.sumOf { it.fullMark.toDouble() }.toFloat()

/**
 * Percentage of [mark] against [fullMark], as `0f..100f`. Returns `0f` when nothing has been
 * entered, avoiding a division by zero.
 */
fun percentage(mark: Float, fullMark: Float): Float =
    if (fullMark <= 0f) 0f else (mark / fullMark * 100f).coerceIn(0f, 100f)

/**
 * 1-based rank of [examId] among [exams], ordered by percentage (highest first). Exams without any
 * score count as 0 % and sort last. Returns `1` for unknown or empty input rather than throwing.
 */
fun rankOf(examId: Long, exams: List<ExamWithTotal>): Int {
    if (exams.isEmpty()) return 1
    val target = exams.firstOrNull { it.exam.id == examId } ?: return 1
    val targetPercentage = percentage(target.totalMark, target.totalFullMark)
    // Strictly greater, so tied exams share the same rank
    return exams.count { percentage(it.totalMark, it.totalFullMark) > targetPercentage } + 1
}

/**
 * Looks up the historical average of [subjectKey] among [stats], which already exclude the current
 * exam. Returns `null` when there is no history to compare against.
 */
fun historicalAverage(subjectKey: String, stats: List<SubjectStat>): SubjectStat? =
    stats.firstOrNull { it.subjectKey == subjectKey }

/**
 * Signed difference, in percentage points, between the current exam's [mark] for one subject and
 * its historical average. Positive means the student improved. Returns `null` when there is no
 * history or either full mark is meaningless.
 */
fun averageDelta(mark: Float, fullMark: Float, stat: SubjectStat?): Float? {
    if (stat == null || stat.avgFullMark <= 0f || fullMark <= 0f) return null
    return percentage(mark, fullMark) - percentage(stat.avgMark, stat.avgFullMark)
}

/** Default name for a new exam, derived from its start date, e.g. `2026-10-04`. */
fun defaultExamName(startDate: Long, locale: Locale = Locale.getDefault()): String =
    SimpleDateFormat("yyyy-MM-dd", locale).format(Date(startDate))

/**
 * Date range of an exam for the history list. Both ends collapse into a single date when the exam
 * lasts only one day.
 */
fun formatDateRange(
    startDate: Long,
    endDate: Long,
    locale: Locale = Locale.getDefault()
): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", locale)
    val start = formatter.format(Date(startDate))
    val end = formatter.format(Date(endDate))
    return if (start == end) start else "$start - $end"
}
