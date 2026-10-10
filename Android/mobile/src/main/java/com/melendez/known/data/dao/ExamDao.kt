package com.melendez.known.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.melendez.known.data.entity.Exam
import com.melendez.known.data.entity.ExamScore
import com.melendez.known.data.entity.ExamWithScores
import com.melendez.known.data.entity.ExamWithTotal
import com.melendez.known.data.entity.SubjectStat
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {

    @Transaction
    @Query(
        """
        SELECT exams.*, 
               IFNULL(SUM(exam_scores.mark), 0) AS totalMark, 
               IFNULL(SUM(exam_scores.fullMark), 0) AS totalFullMark
        FROM exams
        LEFT JOIN exam_scores ON exam_scores.examId = exams.id
        GROUP BY exams.id
        ORDER BY exams.startDate DESC
        """
    )
    fun getExamsWithTotal(): Flow<List<ExamWithTotal>>

    @Transaction
    @Query(
        """
        SELECT exams.*, 
               IFNULL(SUM(exam_scores.mark), 0) AS totalMark, 
               IFNULL(SUM(exam_scores.fullMark), 0) AS totalFullMark
        FROM exams
        LEFT JOIN exam_scores ON exam_scores.examId = exams.id
        WHERE exams.name LIKE '%' || :query || '%'
        GROUP BY exams.id
        ORDER BY exams.startDate DESC
        """
    )
    fun searchExams(query: String): Flow<List<ExamWithTotal>>

    @Query("SELECT * FROM exams WHERE id = :examId")
    fun getExam(examId: Long): Flow<Exam?>

    @Transaction
    @Query("SELECT * FROM exams WHERE id = :examId")
    fun getExamWithScores(examId: Long): Flow<ExamWithScores?>

    @Query("SELECT * FROM exam_scores WHERE examId = :examId")
    fun getScores(examId: Long): Flow<List<ExamScore>>

    /**
     * Per-subject averages over every exam except [excludeExamId], so the detail screen can show
     * the current exam against the historical average without counting itself.
     */
    @Query(
        """
        SELECT subjectKey, 
               AVG(mark) AS avgMark, 
               AVG(fullMark) AS avgFullMark, 
               COUNT(*) AS examCount
        FROM exam_scores
        WHERE examId != :excludeExamId
        GROUP BY subjectKey
        """
    )
    fun getSubjectStats(excludeExamId: Long): Flow<List<SubjectStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScores(scores: List<ExamScore>)

    @Update
    suspend fun updateExam(exam: Exam)

    @Query("DELETE FROM exam_scores WHERE examId = :examId")
    suspend fun deleteScores(examId: Long)

    @Query("DELETE FROM exams WHERE id = :examId")
    suspend fun deleteExam(examId: Long)

    @Query("DELETE FROM exams WHERE id IN (:examIds)")
    suspend fun deleteExams(examIds: List<Long>)

    @Query("UPDATE exams SET isFavorite = :isFavorite WHERE id = :examId")
    suspend fun updateFavorite(examId: Long, isFavorite: Boolean)

    @Query("SELECT * FROM exams WHERE id IN (:examIds)")
    fun getExamsByIds(examIds: List<Long>): Flow<List<Exam>>

    @Transaction
    @Query("SELECT * FROM exams WHERE id IN (:examIds)")
    fun getExamsWithScoresByIds(examIds: List<Long>): Flow<List<ExamWithScores>>

    @Query("SELECT * FROM exams WHERE isFavorite = 1")
    fun getFavoriteExams(): Flow<List<Exam>>
}
