package com.melendez.known.ui.viewmodel.exam

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.melendez.known.data.AppDatabase
import com.melendez.known.data.entity.Exam
import com.melendez.known.data.entity.ExamScore
import com.melendez.known.data.entity.ExamWithScores
import com.melendez.known.data.entity.ExamWithTotal
import com.melendez.known.data.entity.SubjectStat
import com.melendez.known.data.repository.ExamRepository
import kotlinx.coroutines.flow.Flow

/**
 * Exposes the exam queries and writes to the history, detail and input screens.
 *
 * The query methods return cold [Flow]s rather than sharing one subscription, so a screen can ask
 * for the exam it cares about (`examId` may differ between two instances of the same route) and
 * collect only that. Writes are `suspend` functions so the calling screen decides what happens
 * after the transaction commits.
 */
class ExamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExamRepository(AppDatabase.getDatabase(application))

    /** Every exam with its summed marks, newest first. Backs the history list. */
    val exams: Flow<List<ExamWithTotal>> = repository.exams

    /** Exams whose name contains [query]. */
    fun search(query: String): Flow<List<ExamWithTotal>> = repository.searchExams(query)

    /** One exam together with its subject scores, or `null` while it does not exist yet. */
    fun examWithScores(examId: Long): Flow<ExamWithScores?> = repository.getExamWithScores(examId)

    /**
     * Per-subject averages across every exam except [examId], used by the detail screen to compare
     * the current exam against the student's own history.
     */
    fun subjectStats(examId: Long): Flow<List<SubjectStat>> = repository.getSubjectStats(examId)

    /** Inserts a new exam and its scores, returning the generated exam id. */
    suspend fun insertExam(exam: Exam, scores: List<ExamScore>): Long =
        repository.insertExam(exam, scores)

    /** Overwrites an existing exam and replaces all of its scores. */
    suspend fun updateExam(exam: Exam, scores: List<ExamScore>) =
        repository.updateExam(exam, scores)

    /** Deletes one exam. Its scores go with it. */
    suspend fun deleteExam(examId: Long) = repository.deleteExam(examId)

    /** Deletes several exams at once, used by the history list's multi-select mode. */
    suspend fun deleteExams(examIds: List<Long>) = repository.deleteExams(examIds)
}
