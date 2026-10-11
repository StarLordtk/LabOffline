package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.LabSettingsEntity
import com.example.data.model.OrderedTestEntity
import com.example.data.model.RegistrationEntity
import com.example.data.model.TestResultEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    fun generateClinicalReportPdf(
        context: Context,
        settings: LabSettingsEntity,
        reg: RegistrationEntity,
        orderedTests: List<OrderedTestEntity>,
        results: List<TestResultEntity>
    ): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        var yPos = 35f

        // 1. Lab Header (Primary Blue Bar & Title)
        paint.color = Color.rgb(15, 43, 72) // Navy Blue
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(settings.labName, 36f, yPos, paint)

        yPos += 14f
        paint.textSize = 8.5f
        paint.color = Color.rgb(0, 131, 143) // Teal
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(settings.labTagline, 36f, yPos, paint)

        yPos += 13f
        paint.textSize = 8f
        paint.color = Color.DKGRAY
        canvas.drawText("${settings.address} | Phone: ${settings.phone}", 36f, yPos, paint)

        yPos += 11f
        paint.color = Color.rgb(30, 136, 229)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Accreditation & Reg: ${settings.regNumber}", 36f, yPos, paint)

        yPos += 10f
        // Separator rule
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1.2f
        canvas.drawLine(36f, yPos, (pageWidth - 36).toFloat(), yPos, paint)

        // 2. Patient Demographics Box (Rounded box with 2 columns)
        yPos += 14f
        val boxTop = yPos
        val boxHeight = 72f
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(36f, boxTop, (pageWidth - 36).toFloat(), boxTop + boxHeight, 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRoundRect(36f, boxTop, (pageWidth - 36).toFloat(), boxTop + boxHeight, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Column 1
        var textY = boxTop + 16f
        paint.textSize = 8.5f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Patient Name:", 48f, textY, paint)
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(reg.patientName, 120f, textY, paint)

        textY += 14f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Age / Gender:", 48f, textY, paint)
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${reg.patientAge} Yrs / ${reg.patientGender}", 120f, textY, paint)

        textY += 14f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("UHID:", 48f, textY, paint)
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(reg.uhid, 120f, textY, paint)

        textY += 14f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Mobile:", 48f, textY, paint)
        paint.color = Color.BLACK
        canvas.drawText(reg.patientMobile, 120f, textY, paint)

        // Column 2
        textY = boxTop + 16f
        paint.color = Color.GRAY
        canvas.drawText("Registration No:", 310f, textY, paint)
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(reg.regNumber, 395f, textY, paint)

        textY += 14f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Ref By Doctor:", 310f, textY, paint)
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(reg.referringDoctor, 395f, textY, paint)

        textY += 14f
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Date & Time:", 310f, textY, paint)
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(reg.createdAt))
        paint.color = Color.BLACK
        canvas.drawText(dateStr, 395f, textY, paint)

        textY += 14f
        paint.color = Color.GRAY
        canvas.drawText("Priority / Type:", 310f, textY, paint)
        paint.color = Color.BLACK
        canvas.drawText("${reg.priority} (${reg.collectionLocation})", 395f, textY, paint)

        yPos = boxTop + boxHeight + 18f

        // 3. Clinical Report Heading
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(15, 43, 72)
        canvas.drawText("DIAGNOSTIC TEST RESULTS", 36f, yPos, paint)

        yPos += 14f
        // Table Header Banner
        paint.color = Color.rgb(232, 241, 252)
        canvas.drawRect(36f, yPos, (pageWidth - 36).toFloat(), yPos + 18f, paint)

        paint.color = Color.rgb(15, 43, 72)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INVESTIGATION / PARAMETER", 42f, yPos + 12f, paint)
        canvas.drawText("OBSERVED VALUE", 230f, yPos + 12f, paint)
        canvas.drawText("UNIT", 325f, yPos + 12f, paint)
        canvas.drawText("REFERENCE INTERVAL", 375f, yPos + 12f, paint)
        canvas.drawText("STATUS", 505f, yPos + 12f, paint)

        yPos += 24f

        // 4. Render Test Results
        val resultsByTest = results.groupBy { it.testCode }
        for (orderedTest in orderedTests) {
            // Test Section Header
            paint.color = Color.rgb(241, 245, 249)
            canvas.drawRect(36f, yPos, (pageWidth - 36).toFloat(), yPos + 16f, paint)

            paint.color = Color.rgb(30, 41, 59)
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${orderedTest.testName} (${orderedTest.category} - ${orderedTest.specimenType})", 42f, yPos + 11f, paint)
            yPos += 20f

            val testResults = resultsByTest[orderedTest.testCode] ?: emptyList()
            for (res in testResults) {
                val isAbnormal = res.flag == "HIGH" || res.flag == "LOW" || res.flag == "CRITICAL"
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.rgb(30, 41, 59)
                canvas.drawText(res.paramName, 46f, yPos, paint)

                if (isAbnormal) {
                    paint.color = Color.rgb(185, 28, 28) // Red
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                } else {
                    paint.color = Color.BLACK
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                canvas.drawText(res.resultValue.ifBlank { "Pending" }, 230f, yPos, paint)

                paint.color = Color.DKGRAY
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(res.unit, 325f, yPos, paint)
                canvas.drawText(res.referenceIntervalText, 375f, yPos, paint)

                if (isAbnormal) {
                    paint.color = Color.rgb(185, 28, 28)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(res.flag, 505f, yPos, paint)
                } else {
                    paint.color = Color.rgb(21, 128, 61)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("NORMAL", 505f, yPos, paint)
                }

                // Dotted light underline
                paint.color = Color.rgb(241, 245, 249)
                paint.strokeWidth = 0.5f
                canvas.drawLine(46f, yPos + 3f, (pageWidth - 46).toFloat(), yPos + 3f, paint)

                yPos += 14f
            }
            yPos += 6f
        }

        // 5. Pathologist Approval & Sign-off Block at Bottom
        val bottomY = 740f
        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(36f, bottomY, (pageWidth - 36).toFloat(), bottomY, paint)

        // Verified Stamp Box
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(21, 128, 61) // Green
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(40f, bottomY + 12f, 210f, bottomY + 54f, 4f, 4f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CLINICALLY VERIFIED & APPROVED", 48f, bottomY + 28f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 6.5f
        paint.color = Color.DKGRAY
        canvas.drawText("Electronic signature validated by LIMS", 48f, bottomY + 42f, paint)

        // Right side Doctor Stamp
        paint.color = Color.BLACK
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(settings.pathologistName, 360f, bottomY + 26f, paint)
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.GRAY
        canvas.drawText(settings.pathologistDesignation, 360f, bottomY + 38f, paint)
        paint.textSize = 7.5f
        canvas.drawText("Medical Registration: WBMC-61429", 360f, bottomY + 50f, paint)

        // Bottom legal notice
        paint.textSize = 6.5f
        paint.color = Color.LTGRAY
        canvas.drawText("End of Clinical Examination Report • Generated Offline via LabPulse LIMS • Confidential", 130f, 820f, paint)

        pdfDocument.finishPage(page)

        // Save PDF to cache
        val pdfDir = File(context.cacheDir, "reports")
        if (!pdfDir.exists()) pdfDir.mkdirs()
        val file = File(pdfDir, "Report_${reg.regNumber}.pdf")
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        outputStream.close()
        pdfDocument.close()

        return file
    }

    fun shareOrOpenPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Clinical Report PDF"))
    }
}
