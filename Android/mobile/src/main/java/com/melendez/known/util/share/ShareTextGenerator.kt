package com.melendez.known.util.share

import com.melendez.known.data.entity.ExamWithScores
import com.melendez.known.data.entity.ExamWithTotal
import com.melendez.known.data.entity.SubjectStat
import com.melendez.known.util.averageDelta
import com.melendez.known.util.formatDateRange
import com.melendez.known.util.percentage
import com.melendez.known.util.rankOf
import com.melendez.known.util.totalFullMark
import com.melendez.known.util.totalMark

/**
 * Builds a formatted plain-text summary of an exam for sharing via text.
 */
object ShareTextGenerator {

    /**
     * Produces a human-readable text summary of [examWithScores], including total marks,
     * percentage, rank, and per-subject breakdown.
     */
    fun buildText(
        examWithScores: ExamWithScores,
        allExams: List<ExamWithTotal>,
        subjectStats: List<SubjectStat>,
        subjectNameResolver: (String) -> String
    ): String {
        val exam = examWithScores.exam
        val scores = examWithScores.scores
        val totalMark = totalMark(scores)
        val totalFullMark = totalFullMark(scores)
        val pct = percentage(totalMark, totalFullMark)
        val rank = rankOf(exam.id, allExams)
        val examCount = allExams.size

        val dateRange = formatDateRange(exam.startDate, exam.endDate)

        val sb = StringBuilder()
        sb.appendLine("Known | Score Share")
        sb.appendLine()
        sb.appendLine("Exam: ${exam.name}")
        sb.appendLine("Date: $dateRange")
        sb.appendLine()
        sb.appendLine("━━━━━━━━━━━━━━━━━━")
        sb.appendLine(
            "Total: ${formatScore(totalMark)} / ${formatScore(totalFullMark)} (${
                "%.1f".format(pct)
            }%)"
        )
        sb.appendLine("Rank: $rank / $examCount")

        // Historical comparison
        val historicalAvg = allExams
            .filter { it.exam.id != exam.id }
            .map { percentage(it.totalMark, it.totalFullMark) }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat()
        if (historicalAvg != null) {
            val delta = pct - historicalAvg
            val sign = if (delta >= 0) "+" else ""
            sb.appendLine("vs History: ${"%.1f".format(historicalAvg)}% ($sign${"%.1f".format(delta)})")
        }

        sb.appendLine("━━━━━━━━━━━━━━━━━━")
        sb.appendLine()
        sb.appendLine("Subjects:")

        for ((_, _, subjectKey, mark, fullMark) in scores) {
            val subjectName = subjectNameResolver(subjectKey)
            val subjectPct = percentage(mark, fullMark)
            val stat = subjectStats.firstOrNull { it.subjectKey == subjectKey }
            val deltaStr = if (stat != null) {
                val delta = averageDelta(mark, fullMark, stat)
                if (delta != null) {
                    val sign = if (delta >= 0) "+" else ""
                    " ($sign${"%.1f".format(delta)})"
                } else ""
            } else ""
            sb.appendLine(
                "  $subjectName: ${formatScore(mark)} / ${formatScore(fullMark)} (${
                    "%.1f".format(subjectPct)
                }%)$deltaStr"
            )
        }

        sb.appendLine()
        sb.appendLine("━━━━━━━━━━━━━━━━━━")
        sb.appendLine("Shared from Known")

        return sb.toString()
    }

    private fun formatScore(value: Float): String =
        if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
}
