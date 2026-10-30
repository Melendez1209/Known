package com.melendez.known.util

/**
 * Information density of the history list. The value is persisted in the settings row, so the
 * numbers are part of the stored format and must never be renumbered
 */
enum class HistoryViewMode(val value: Int) {
    /** Exam name and total mark only, in one tight row */
    COMPACT(0),

    /** Two line card with the date, the mark against the full mark and the favourite state */
    STANDARD(1),

    /** The plain table like row of name, date and mark */
    LIST(2);

    companion object {
        /** Mode used before the user has made a choice, and for unknown stored values */
        val DEFAULT = STANDARD

        fun fromValue(value: Int): HistoryViewMode =
            entries.firstOrNull { it.value == value } ?: DEFAULT
    }
}
