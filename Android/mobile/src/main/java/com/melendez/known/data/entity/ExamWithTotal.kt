package com.melendez.known.data.entity

import androidx.room.Embedded

/**
 * An exam together with the sum of its subject scores, as read by the history list. The totals come
 * from a `LEFT JOIN` aggregate, so they are `0` for an exam that has not been scored yet.
 */
data class ExamWithTotal(
    @Embedded val exam: Exam,
    val totalMark: Float,
    val totalFullMark: Float
)
