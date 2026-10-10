package com.melendez.known.util.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.melendez.known.data.entity.ExamWithScores
import com.melendez.known.data.entity.ExamWithTotal
import com.melendez.known.util.share.ShareCardGenerator
import java.io.FileOutputStream

/**
 * Wraps Android's native PrintManager to print exam results as a PDF.
 */
object PrintManager {

    /**
     * Generates share cards for [examWithScoresList] and starts the system print dialog.
     */
    fun printExams(
        context: Context,
        examWithScoresList: List<ExamWithScores>,
        allExams: List<ExamWithTotal>,
        subjectNameResolver: (String) -> String
    ) {
        if (examWithScoresList.isEmpty()) return

        val uris = examWithScoresList.mapNotNull { examWithScores ->
            ShareCardGenerator.generateShareCard(
                context, examWithScores, allExams, subjectNameResolver
            )
        }

        if (uris.isEmpty()) return

        val printManager = context.getSystemService(Context.PRINT_SERVICE) as android.print.PrintManager
        val printAdapter = ExamPrintDocumentAdapter(context, uris)
        printManager.print("Exam Results", printAdapter, PrintAttributes.Builder().build())
    }

    /**
     * Renders multiple share card images into a multi-page PDF document.
     */
    private class ExamPrintDocumentAdapter(
        private val context: Context,
        private val imageUris: List<Uri>
    ) : PrintDocumentAdapter() {

        private var pdfFile: ParcelFileDescriptor? = null

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback?,
            extras: Bundle?
        ) {
            val info = PrintDocumentInfo.Builder("Exam Results.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(imageUris.size)
                .build()
            callback?.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor?,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback?
        ) {
            if (destination == null) {
                callback?.onWriteFailed("Destination is null")
                return
            }

            try {
                val document = PdfDocument()

                imageUris.forEachIndexed { index, uri ->
                    val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }

                    if (bitmap != null) {
                        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                        val page = document.startPage(pageInfo)
                        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                        document.finishPage(page)
                        bitmap.recycle()
                    }
                }

                FileOutputStream(destination.fileDescriptor).use { out ->
                    document.writeTo(out)
                }

                document.close()
                pdfFile = destination
                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: Exception) {
                callback?.onWriteFailed(e.message)
            }
        }

        override fun onFinish() {
            pdfFile?.close()
            pdfFile = null
        }
    }
}
