package com.melendez.known.ui.screens

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Screens : NavKey {
    @Serializable
    data object Main : NavKey

    @Serializable
    data object Settings : NavKey

    @Serializable
    data object General : NavKey

    @Serializable
    data object Appearance : NavKey

    @Serializable
    data object Dark : NavKey

    @Serializable
    data object Language : NavKey

    /**
     * Date range picker that opens the score input screen. A non-zero [examId] means an existing
     * exam is being re-dated, so [startDate] and [endDate] preselect the range it already covers.
     */
    @Serializable
    data class DRP(
        val examId: Long = 0L,
        val startDate: Long = 0L,
        val endDate: Long = 0L
    ) : NavKey

    /**
     * Score input screen. A non-zero [examId] means an existing exam is edited in place rather than
     * a new one being created.
     */
    @Serializable
    data class Inputting(
        val startDate: Long = 0L,
        val endDate: Long = 0L,
        val examId: Long = 0L
    ) : NavKey

    @Serializable
    data object About : NavKey

    @Serializable
    data object Signin : NavKey

    /** Exam detail screen for the single exam identified by [examId]. */
    @Serializable
    data class Detail(val examId: Long = 0L) : NavKey

    @Serializable
    data object Prophets : NavKey

    @Serializable
    data object Credits : NavKey

    @Serializable
    data object Guide : NavKey
}
