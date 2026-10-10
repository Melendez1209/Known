package com.melendez.known.util

import android.content.Context
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
 * Wraps Android's native PrintManager to print an exam result as a PDF.
 */
object PrintManager {

    /**
     * Generates a share card for [examWithScores] and starts the system print dialogue.
     */
    fun printExam(
        context: Context,
        examWithScores: ExamWithScores,
        allExams: List<ExamWithTotal>,
        subjectNameResolver: (String) -> String
    ) {
        val uri = ShareCardGenerator.generateShareCard(
            context,
            examWithScores,
            allExams,
            subjectNameResolver
        )

        if (uri == null) return

        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = ExamPrintDocumentAdapter(context, uri)
        printManager.print("Exam Result", printAdapter, PrintAttributes.Builder().build())
    }

    /**
     * Renders the generated share card image into a single-page PDF document.
     */
    private class ExamPrintDocumentAdapter(
        private val context: Context,
        private val imageUri: Uri
    ) : PrintDocumentAdapter() {

        private var pdfFile: ParcelFileDescriptor? = null

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback?,
            extras: Bundle?
        ) {
            val info = PrintDocumentInfo.Builder("Exam Result.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
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
                val bitmap = context.contentResolver.openInputStream(imageUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }

                if (bitmap == null) {
                    callback?.onWriteFailed("Failed to decode image")
                    return
                }

                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = document.startPage(pageInfo)
                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(page)

                FileOutputStream(destination.fileDescriptor).use { out ->
                    document.writeTo(out)
                }

                document.close()
                bitmap.recycle()

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
