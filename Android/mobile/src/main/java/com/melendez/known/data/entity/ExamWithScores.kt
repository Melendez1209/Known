package com.melendez.known.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * An exam together with every subject score recorded for it, as read by the detail screen.
 */
data class ExamWithScores(
    @Embedded val exam: Exam,
    @Relation(parentColumn = "id", entityColumn = "examId")
    val scores: List<ExamScore>
)
