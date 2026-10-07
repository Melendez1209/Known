package com.melendez.known.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.melendez.known.R
import com.melendez.known.data.entity.ExamWithScores
import com.melendez.known.data.entity.ExamWithTotal
import com.melendez.known.data.entity.SubjectStat

/**
 * Centralised entry point for sharing exam results as image or text.
 */
object ShareManager {

    /**
     * Shares [examWithScores] as a formatted image card via the system share sheet.
     */
    fun shareAsImage(
        context: Context,
        examWithScores: ExamWithScores,
        allExams: List<ExamWithTotal>,
        subjectStats: List<SubjectStat>,
        subjectNameResolver: (String) -> String
    ) {
        Toast.makeText(context, context.getString(R.string.share_image_saving), Toast.LENGTH_SHORT).show()

        val uri = ShareCardGenerator.generateShareCard(
            context, examWithScores, allExams, subjectStats, subjectNameResolver
        )

        if (uri == null) {
            Toast.makeText(context, context.getString(R.string.share_image_failed), Toast.LENGTH_SHORT).show()
            return
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, context.getString(R.string.share_via))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Shares [examWithScores] as formatted plain text via the system share sheet.
     */
    fun shareAsText(
        context: Context,
        examWithScores: ExamWithScores,
        allExams: List<ExamWithTotal>,
        subjectStats: List<SubjectStat>,
        subjectNameResolver: (String) -> String
    ) {
        val text = ShareTextGenerator.buildText(
            examWithScores, allExams, subjectStats, subjectNameResolver
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        val chooser = Intent.createChooser(shareIntent, context.getString(R.string.share_via))
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
