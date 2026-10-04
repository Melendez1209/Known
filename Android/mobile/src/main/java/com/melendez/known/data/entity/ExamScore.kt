package com.melendez.known.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exam_scores",
    foreignKeys = [
        ForeignKey(
            entity = Exam::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["examId"]),
        Index(value = ["examId", "subjectKey"], unique = true)
    ]
)
data class ExamScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val examId: Long,
    val subjectKey: String,
    val mark: Float,
    val fullMark: Float
)
