package com.melendez.known.data.repository

import androidx.room.withTransaction
import com.melendez.known.data.AppDatabase
import com.melendez.known.data.entity.Exam
import com.melendez.known.data.entity.ExamScore
import com.melendez.known.data.entity.ExamWithScores
import com.melendez.known.data.entity.ExamWithTotal
import com.melendez.known.data.entity.SubjectStat
import kotlinx.coroutines.flow.Flow

/**
 * Exam storage and queries. Takes the database rather than the DAO alone because compound writes
 * (an exam plus its subject scores) must run inside a single transaction.
 */
class ExamRepository(private val database: AppDatabase) {

    private val examDao = database.examDao()

    val exams: Flow<List<ExamWithTotal>> = examDao.getExamsWithTotal()

    fun searchExams(query: String): Flow<List<ExamWithTotal>> = examDao.searchExams(query)

    fun getExam(examId: Long): Flow<Exam?> = examDao.getExam(examId)

    fun getExamWithScores(examId: Long): Flow<ExamWithScores?> = examDao.getExamWithScores(examId)

    fun getSubjectStats(excludeExamId: Long): Flow<List<SubjectStat>> =
        examDao.getSubjectStats(excludeExamId)

    /**
     * Insert a new exam with its subject scores. Returns the generated exam id.
     */
    suspend fun insertExam(exam: Exam, scores: List<ExamScore>): Long = database.withTransaction {
        val examId = examDao.insertExam(exam)
        examDao.insertScores(scores.map { it.copy(id = 0, examId = examId) })
        examId
    }

    /**
     * Replace an existing exam and all of its subject scores, keeping the original id.
     */
    suspend fun updateExam(exam: Exam, scores: List<ExamScore>) {
        database.withTransaction {
            examDao.updateExam(exam)
            examDao.deleteScores(exam.id)
            examDao.insertScores(scores.map { it.copy(id = 0, examId = exam.id) })
        }
    }

    suspend fun deleteExam(examId: Long) {
        database.withTransaction {
            examDao.deleteScores(examId)
            examDao.deleteExam(examId)
        }
    }

    suspend fun deleteExams(examIds: List<Long>) {
        database.withTransaction {
            examIds.forEach { examDao.deleteScores(it) }
            examDao.deleteExams(examIds)
        }
    }

    suspend fun setFavorite(examId: Long, isFavorite: Boolean) {
        examDao.updateFavorite(examId, isFavorite)
    }

    fun getFavoriteExams(): Flow<List<Exam>> = examDao.getFavoriteExams()
}
