package com.melendez.known.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.melendez.known.R
import com.melendez.known.data.entity.ExamWithScores
import com.melendez.known.data.entity.ExamWithTotal
import com.melendez.known.data.entity.SubjectStat
import java.io.File
import java.io.FileOutputStream

/**
 * Generates a well-formatted share card as a bitmap, rather than a plain screenshot.
 *
 * The card is drawn on a fixed 1080x1350 canvas (4:5 aspect ratio) with:
 * - A gradient header with the app branding
 * - Exam name and date
 * - Large total score display
 * - Rank and historical comparison
 * - Per-subject breakdown with progress bars
 * - Footer with app attribution
 */
object ShareCardGenerator {

    private const val WIDTH = 1080
    private const val HEIGHT = 1600
    private const val PADDING = 64f
    private const val CARD_RADIUS = 32f

    // Brand colors matching the app's green seed
    private val BRAND_GREEN = Color.parseColor("#A3D48D")
    private val BRAND_GREEN_DARK = Color.parseColor("#7BB861")
    private val SURFACE_LIGHT = Color.parseColor("#FAFAFA")
    private val SURFACE_CARD = Color.parseColor("#FFFFFF")
    private val TEXT_PRIMARY = Color.parseColor("#1A1A1A")
    private val TEXT_SECONDARY = Color.parseColor("#666666")
    private val TEXT_ON_BRAND = Color.parseColor("#1A3A0A")
    private val DIVIDER_COLOR = Color.parseColor("#E0E0E0")
    private val PROGRESS_BG = Color.parseColor("#E8E8E8")

    /**
     * Generates a share card bitmap for [examWithScores] and saves it to the app's cache directory.
     * Returns a content URI that can be shared via an Intent.
     */
    fun generateShareCard(
        context: Context,
        examWithScores: ExamWithScores,
        allExams: List<ExamWithTotal>,
        subjectStats: List<SubjectStat>,
        subjectNameResolver: (String) -> String
    ): Uri? {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawCard(context, canvas, examWithScores, allExams, subjectStats, subjectNameResolver)

        // Save to cache
        val cacheDir = File(context.cacheDir, "share_images")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val file =
            File(cacheDir, "share_${examWithScores.exam.id}_${System.currentTimeMillis()}.png")

        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            bitmap.recycle()
            null
        }
    }

    private fun drawCard(
        context: Context,
        canvas: Canvas,
        examWithScores: ExamWithScores,
        allExams: List<ExamWithTotal>,
        subjectStats: List<SubjectStat>,
        subjectNameResolver: (String) -> String
    ) {
        val exam = examWithScores.exam
        val scores = examWithScores.scores
        val totalMark = totalMark(scores)
        val totalFullMark = totalFullMark(scores)
        val pct = percentage(totalMark, totalFullMark)
        val rank = rankOf(exam.id, allExams)
        val examCount = allExams.size
        val dateRange = formatDateRange(exam.startDate, exam.endDate)

        // Background
        canvas.drawColor(SURFACE_LIGHT)

        // Header gradient
        val headerHeight = 280f
        val headerPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, WIDTH.toFloat(), headerHeight,
                BRAND_GREEN, BRAND_GREEN_DARK,
                Shader.TileMode.CLAMP
            )
        }
        val headerRect = RectF(0f, 0f, WIDTH.toFloat(), headerHeight)
        canvas.drawRoundRect(headerRect, CARD_RADIUS, CARD_RADIUS, headerPaint)
        // Square off the bottom of the header
        canvas.drawRect(0f, headerHeight - CARD_RADIUS, WIDTH.toFloat(), headerHeight, headerPaint)

        // Header content
        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_ON_BRAND
            typeface = Typeface.DEFAULT_BOLD
        }

        // App name
        headerTextPaint.textSize = 42f
        canvas.drawText("Known", PADDING, 90f, headerTextPaint)

        // Exam name
        headerTextPaint.textSize = 52f
        canvas.drawText(exam.name, PADDING, 160f, headerTextPaint)

        // Date
        headerTextPaint.textSize = 36f
        headerTextPaint.typeface = Typeface.DEFAULT
        canvas.drawText(dateRange, PADDING, 220f, headerTextPaint)

        // Main content area
        var y = headerHeight + 60f

        // Total score card
        val scoreCardHeight = 200f
        val scoreCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SURFACE_CARD
            setShadowLayer(12f, 0f, 4f, Color.parseColor("#1A000000"))
        }
        val scoreRect = RectF(PADDING, y, WIDTH - PADDING, y + scoreCardHeight)
        canvas.drawRoundRect(scoreRect, 24f, 24f, scoreCardPaint)

        // Total score text
        val scoreLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_SECONDARY
            textSize = 32f
        }
        canvas.drawText("Total Score", PADDING + 40f, y + 60f, scoreLabelPaint)

        val scoreValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_PRIMARY
            textSize = 72f
            typeface = Typeface.DEFAULT_BOLD
        }
        val scoreText = "${formatScoreNum(totalMark)} / ${formatScoreNum(totalFullMark)}"
        canvas.drawText(scoreText, PADDING + 40f, y + 140f, scoreValuePaint)

        // Percentage badge
        val pctPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BRAND_GREEN_DARK
            textSize = 48f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("${"%.1f".format(pct)}%", WIDTH - PADDING - 40f, y + 140f, pctPaint)

        y += scoreCardHeight + 40f

        // Rank and history row
        val infoCardHeight = 120f
        val infoRect = RectF(PADDING, y, WIDTH - PADDING, y + infoCardHeight)
        canvas.drawRoundRect(infoRect, 24f, 24f, scoreCardPaint)

        val infoLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_SECONDARY
            textSize = 28f
        }
        val infoValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_PRIMARY
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
        }

        // Rank
        canvas.drawText("Rank", PADDING + 40f, y + 50f, infoLabelPaint)
        canvas.drawText("$rank / $examCount", PADDING + 40f, y + 100f, infoValuePaint)

        // Historical comparison
        val historicalAvg = allExams
            .filter { it.exam.id != exam.id }
            .map { percentage(it.totalMark, it.totalFullMark) }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat()

        val centerX = WIDTH / 2f
        canvas.drawText("vs History", centerX + 40f, y + 50f, infoLabelPaint)
        if (historicalAvg != null) {
            val delta = pct - historicalAvg
            val sign = if (delta >= 0) "+" else ""
            val deltaColor =
                if (delta >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
            infoValuePaint.color = deltaColor
            canvas.drawText(
                "${"%.1f".format(historicalAvg)}% ($sign${"%.1f".format(delta)})",
                centerX + 40f,
                y + 100f,
                infoValuePaint
            )
        } else {
            infoValuePaint.color = TEXT_SECONDARY
            canvas.drawText("—", centerX + 40f, y + 100f, infoValuePaint)
        }

        y += infoCardHeight + 48f

        // Section title
        val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_PRIMARY
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Subjects", PADDING, y + 10f, sectionTitlePaint)
        y += 50f

        // Subject rows
        val rowHeight = 80f
        val subjectNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_PRIMARY
            textSize = 32f
        }
        val subjectScorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_SECONDARY
            textSize = 30f
        }
        val progressBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = PROGRESS_BG
            style = Paint.Style.FILL
        }
        val progressFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = BRAND_GREEN_DARK
            style = Paint.Style.FILL
        }

        for (score in scores) {
            val subjectName = subjectNameResolver(score.subjectKey)
            val subjectPct = percentage(score.mark, score.fullMark)

            // Subject name
            canvas.drawText(subjectName, PADDING + 16f, y + 36f, subjectNamePaint)

            // Score text
            val scoreStr = "${formatScoreNum(score.mark)} / ${formatScoreNum(score.fullMark)}"
            val scoreWidth = subjectScorePaint.measureText(scoreStr)
            canvas.drawText(
                scoreStr,
                WIDTH - PADDING - 16f - scoreWidth,
                y + 36f,
                subjectScorePaint
            )

            // Progress bar
            val barY = y + 52f
            val barWidth = WIDTH - 2 * PADDING - 32f
            val barRect = RectF(PADDING + 16f, barY, PADDING + 16f + barWidth, barY + 12f)
            canvas.drawRoundRect(barRect, 6f, 6f, progressBgPaint)

            val fillWidth = barWidth * (subjectPct / 100f).coerceIn(0f, 1f)
            val fillRect = RectF(PADDING + 16f, barY, PADDING + 16f + fillWidth, barY + 12f)
            canvas.drawRoundRect(fillRect, 6f, 6f, progressFillPaint)

            y += rowHeight
        }

        // QR Code section
        val qrCodeSize = 160f
        val qrCodeMargin = 40f
        val qrCodeY = y + 20f

        // Load QR code from drawable
        val qrCodeBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.qrcode)
        if (qrCodeBitmap != null) {
            // Draw white background for QR code
            val qrBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                setShadowLayer(8f, 0f, 2f, Color.parseColor("#1A000000"))
            }
            val qrBgRect = RectF(
                qrCodeMargin - 8f, qrCodeY - 8f,
                qrCodeMargin + qrCodeSize + 8f, qrCodeY + qrCodeSize + 8f
            )
            canvas.drawRoundRect(qrBgRect, 16f, 16f, qrBgPaint)

            // Draw QR code scaled to fit
            val scaledQr = Bitmap.createScaledBitmap(
                qrCodeBitmap,
                qrCodeSize.toInt(),
                qrCodeSize.toInt(),
                false
            )
            canvas.drawBitmap(scaledQr, qrCodeMargin, qrCodeY, null)

            // QR code label
            val qrLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = TEXT_SECONDARY
                textSize = 24f
            }
            canvas.drawText("Scan to open source", qrCodeMargin + qrCodeSize + 24f, qrCodeY + 60f, qrLabelPaint)
            canvas.drawText("github.com/Melendez1209/Known", qrCodeMargin + qrCodeSize + 24f, qrCodeY + 100f, qrLabelPaint)
        }

        y = qrCodeY + qrCodeSize + 40f

        // Footer
        val footerY = HEIGHT - 80f
        val dividerPaint = Paint().apply {
            color = DIVIDER_COLOR
            strokeWidth = 2f
        }
        canvas.drawLine(PADDING, footerY - 20f, WIDTH - PADDING, footerY - 20f, dividerPaint)

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_SECONDARY
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Shared from Known", WIDTH / 2f, footerY + 30f, footerPaint)
    }

    private fun formatScoreNum(value: Float): String =
        if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
}
