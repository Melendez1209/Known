package com.melendez.known.util

import com.melendez.known.data.entity.Exam
import com.melendez.known.data.entity.ExamScore
import com.melendez.known.data.entity.ExamWithTotal
import com.melendez.known.data.entity.SubjectStat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExamCoreKtTest {

    private fun score(subjectKey: String, mark: Float, fullMark: Float = 150f) =
        ExamScore(examId = 7L, subjectKey = subjectKey, mark = mark, fullMark = fullMark)

    private fun examWith(id: Long, mark: Float, fullMark: Float) = ExamWithTotal(
        exam = Exam(id = id, name = "Exam $id", startDate = 0L, endDate = 0L),
        totalMark = mark,
        totalFullMark = fullMark
    )

    private fun at(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(year, month - 1, day)
        return calendar.timeInMillis
    }

    // --- parseScoreOrNull ---

    @Test
    fun `Parses plain and half scores`() {
        assertEquals(100f, parseScoreOrNull("100")!!, 0f)
        assertEquals(98.5f, parseScoreOrNull("98.5")!!, 0f)
        assertEquals(0.5f, parseScoreOrNull(".5")!!, 0f)
        assertEquals(100f, parseScoreOrNull(" 100 ")!!, 0f)
    }

    @Test
    fun `Rejects unusable score text`() {
        assertNull(parseScoreOrNull(""))
        assertNull(parseScoreOrNull("."))
        assertNull(parseScoreOrNull("-1"))
        assertNull(parseScoreOrNull("abc"))
    }

    // --- isValidScoreInput ---

    @Test
    fun `Only integers and half points are accepted`() {
        assertTrue(isValidScoreInput(""))
        assertTrue(isValidScoreInput("0"))
        assertTrue(isValidScoreInput("100"))
        assertTrue(isValidScoreInput("98.5"))
        assertFalse(isValidScoreInput("98.4"))
        assertFalse(isValidScoreInput("."))
        assertFalse(isValidScoreInput("-1"))
    }

    @Test
    fun `Overlong input is refused so the field cannot overflow`() {
        assertTrue(isValidScoreInput("1234567"))
        assertFalse(isValidScoreInput("12345678"))
    }

    // --- formatScoreInput and processScoreInput ---

    @Test
    fun `Redundant trailing zero is dropped`() {
        assertEquals("150", formatScoreInput("150.0"))
        assertEquals("98.5", formatScoreInput("98.5"))
        assertEquals("", formatScoreInput(""))
        assertEquals("abc", formatScoreInput("abc"))
    }

    @Test
    fun `Trailing dot is completed to a half point`() {
        assertEquals("98.5", processScoreInput("98."))
        assertEquals("98", processScoreInput("98"))
        assertEquals("98.4", processScoreInput("98.4"))
        assertEquals("", processScoreInput(""))
    }

    // --- slider helpers: a blank or half-typed full mark must never throw ---

    @Test
    fun `Slider steps stay safe for blank and half-typed full marks`() {
        assertEquals(0, sliderSteps(""))
        assertEquals(0, sliderSteps("."))
        assertEquals(0, sliderSteps("0"))
        assertEquals(99, sliderSteps("50"))
        assertEquals(299, sliderSteps("150"))
    }

    @Test
    fun `Slider value falls back to zero rather than throwing`() {
        assertEquals(0f, sliderValue("", "150"), 0f)
        assertEquals(0f, sliderValue(".", "150"), 0f)
        assertEquals(0f, sliderValue("75", ""), 0f)
        assertEquals(0f, sliderValue("75", "0"), 0f)
        assertEquals(0.5f, sliderValue("75", "150"), 0f)
        // Dragging past the full mark is clamped rather than reported above 1
        assertEquals(1f, sliderValue("200", "150"), 0f)
    }

    @Test
    fun `Slider positions snap back to half points`() {
        assertEquals("75", snappedScore(0.5f, "150"))
        assertEquals("50", snappedScore(0.333f, "150"))
        assertEquals("50", snappedScore(1f, "50"))
        assertEquals("0.5", snappedScore(0.5f, "1"))
        assertEquals("", snappedScore(0.5f, "."))
    }

    // --- buildExamScores ---

    @Test
    fun `Unticked and blank rows are never persisted`() {
        val inputs = listOf(
            ScoreInput("chinese", checked = true, mark = "120", fullMark = "150"),
            ScoreInput("maths", checked = false, mark = "140", fullMark = "150"),
            ScoreInput("physics", checked = true, mark = "", fullMark = "100")
        )

        val scores = buildExamScores(7L, inputs)

        assertEquals(1, scores.size)
        assertEquals("chinese", scores[0].subjectKey)
        assertEquals(7L, scores[0].examId)
        assertEquals(120f, scores[0].mark, 0f)
    }

    @Test
    fun `Impossible rows are reported instead of silently dropped`() {
        val inputs = listOf(
            ScoreInput("chinese", checked = true, mark = "160", fullMark = "150"),
            ScoreInput("maths", checked = true, mark = "140", fullMark = "150")
        )
        val invalid = mutableListOf<ScoreInput>()

        val scores = buildExamScores(7L, inputs) { invalid.add(it) }

        assertEquals(1, scores.size)
        assertEquals(listOf("chinese"), invalid.map { it.subjectKey })
    }

    // --- validateScoreInputs ---

    @Test
    fun `A form without a single mark cannot be saved`() {
        val inputs = listOf(
            ScoreInput("chinese", checked = true, mark = "", fullMark = "150"),
            ScoreInput("pe", checked = false, mark = "", fullMark = "100")
        )

        assertEquals(ScoreValidation.NO_MARK_ENTERED, validateScoreInputs(inputs))
    }

    @Test
    fun `A mark above its full mark blocks the save`() {
        val inputs = listOf(
            ScoreInput("chinese", checked = true, mark = "160", fullMark = "150"),
            ScoreInput("maths", checked = true, mark = "140", fullMark = "150")
        )

        assertEquals(ScoreValidation.MARK_EXCEEDS_FULL, validateScoreInputs(inputs))
    }

    @Test
    fun `A mark without a usable full mark blocks the save`() {
        val inputs = listOf(
            ScoreInput("chinese", checked = true, mark = "140", fullMark = ".")
        )

        assertEquals(ScoreValidation.MARK_EXCEEDS_FULL, validateScoreInputs(inputs))
    }

    @Test
    fun `One usable mark is enough to save`() {
        val inputs = listOf(
            ScoreInput("chinese", checked = true, mark = "140", fullMark = "150"),
            ScoreInput("maths", checked = true, mark = "", fullMark = "150")
        )

        assertEquals(ScoreValidation.VALID, validateScoreInputs(inputs))
    }

    // --- defaultScoreInputs ---

    @Test
    fun `Compulsory subjects are always ticked`() {
        val inputs = defaultScoreInputs(setOf("physics"))

        assertEquals(examSubjectKeys, inputs.map { it.subjectKey })
        assertTrue(inputs.take(COMPULSORY_SUBJECT_COUNT).all { it.checked })
        assertFalse(inputs.last().checked)
    }

    @Test
    fun `Optional subjects follow the stored selection`() {
        val inputs = defaultScoreInputs(setOf("physics", "geography"))

        assertEquals(
            listOf("chinese", "maths", "foreign_language", "physics", "geography"),
            inputs.filter { it.checked }.map { it.subjectKey }
        )
    }

    @Test
    fun `An empty selection keeps the historical default`() {
        val inputs = defaultScoreInputs(emptySet())

        assertEquals(
            examSubjectKeys - "pe",
            inputs.filter { it.checked }.map { it.subjectKey }
        )
        assertTrue(inputs.all { it.mark.isEmpty() && it.fullMark == DEFAULT_FULL_MARK })
    }

    // --- recordedSubjectKeys ---

    @Test
    fun `Only subjects carrying a score are kept`() {
        val scores = listOf(
            score("geography", 80f),
            score("chinese", 90f),
            score("chinese", 88f)
        )

        assertEquals(
            listOf("chinese", "geography"),
            recordedSubjectKeys(examSubjectKeys, scores)
        )
    }

    @Test
    fun `No score at all means no subject`() {
        assertEquals(emptyList<String>(), recordedSubjectKeys(examSubjectKeys, emptyList()))
    }

    @Test
    fun `A zero mark still counts as recorded data`() {
        val scores = listOf(score("pe", 0f, fullMark = 100f))

        assertEquals(listOf("pe"), recordedSubjectKeys(examSubjectKeys, scores))
    }

    // --- totals and percentage ---

    @Test
    fun `Totals sum every recorded mark`() {
        val scores = listOf(
            score("chinese", 120f, 150f),
            score("maths", 98.5f, 150f)
        )

        assertEquals(218.5f, totalMark(scores), 0f)
        assertEquals(300f, totalFullMark(scores), 0f)
    }

    @Test
    fun `Percentage never divides by zero and never exceeds one hundred`() {
        assertEquals(0f, percentage(50f, 0f), 0f)
        assertEquals(50f, percentage(50f, 100f), 0f)
        assertEquals(100f, percentage(200f, 100f), 0f)
    }

    // --- rankOf ---

    @Test
    fun `Rank follows the percentage, highest first`() {
        val exams = listOf(
            examWith(1L, 90f, 100f),
            examWith(2L, 80f, 100f),
            examWith(3L, 70f, 100f)
        )

        assertEquals(1, rankOf(1L, exams))
        assertEquals(2, rankOf(2L, exams))
        assertEquals(3, rankOf(3L, exams))
    }

    @Test
    fun `Equal percentages share a rank`() {
        val exams = listOf(examWith(1L, 80f, 100f), examWith(2L, 80f, 100f))

        assertEquals(1, rankOf(1L, exams))
        assertEquals(1, rankOf(2L, exams))
    }

    @Test
    fun `Unknown and empty inputs default to first place`() {
        assertEquals(1, rankOf(1L, emptyList()))
        assertEquals(1, rankOf(42L, listOf(examWith(1L, 80f, 100f))))
    }

    // --- historical average ---

    @Test
    fun `Historical average is looked up by subject key`() {
        val stats = listOf(
            SubjectStat("physics", 80f, 100f, 3),
            SubjectStat("chemistry", 70f, 100f, 3)
        )

        assertEquals(stats[0], historicalAverage("physics", stats))
        assertNull(historicalAverage("geography", stats))
        assertNull(historicalAverage("physics", emptyList()))
    }

    @Test
    fun `Average delta compares against the recorded history`() {
        val stat = SubjectStat("physics", 80f, 100f, 3)

        assertEquals(10f, averageDelta(90f, 100f, stat)!!, 0.01f)
        assertEquals(-5f, averageDelta(75f, 100f, stat)!!, 0.01f)
    }

    @Test
    fun `Average delta is unavailable without history or a sensible full mark`() {
        val stat = SubjectStat("physics", 80f, 100f, 3)

        assertNull(averageDelta(90f, 100f, null))
        assertNull(averageDelta(90f, 0f, stat))
        assertNull(averageDelta(90f, 100f, SubjectStat("physics", 80f, 0f, 3)))
    }

    // --- dates ---

    @Test
    fun `A single day formats without a range`() {
        val day = at(2026, 10, 4)

        assertEquals("2026-10-04", formatDateRange(day, day))
        assertEquals("2026-10-04", defaultExamName(day))
    }

    @Test
    fun `A longer exam formats both ends of the range`() {
        assertEquals(
            "2026-10-04 - 2026-10-05",
            formatDateRange(at(2026, 10, 4), at(2026, 10, 5))
        )
    }
}
