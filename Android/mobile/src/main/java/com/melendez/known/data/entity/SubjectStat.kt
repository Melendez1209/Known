package com.melendez.known.data.entity

/**
 * Average score of one subject across every recorded exam, as aggregated by the detail screen. The
 * current exam is excluded by the query so it is never compared against itself.
 */
data class SubjectStat(
    val subjectKey: String,
    val avgMark: Float,
    val avgFullMark: Float,
    val examCount: Int
)
