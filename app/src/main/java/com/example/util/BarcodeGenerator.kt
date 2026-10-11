package com.example.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

@Composable
fun BarcodeView(
    code: String,
    modifier: Modifier = Modifier,
    height: Int = 48,
    showText: Boolean = true
) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Deterministic pseudo-code 128 barcode pattern generated from code hash
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
        ) {
            val totalWidth = size.width
            val barHeight = size.height
            val charCodes = code.map { it.code }
            val barCount = 45

            val barWidth = totalWidth / (barCount * 1.5f)
            var currentX = (totalWidth - (barCount * 1.5f * barWidth)) / 2f

            for (i in 0 until barCount) {
                val seed = if (charCodes.isNotEmpty()) (charCodes[i % charCodes.size] * (i + 13)).absoluteValue else i * 7
                val isThick = (seed % 3 == 0)
                val isDrawn = (seed % 7 != 0)

                val w = if (isThick) barWidth * 2f else barWidth
                if (isDrawn) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(currentX, 0f),
                        size = Size(w, barHeight)
                    )
                }
                currentX += w + barWidth * 0.5f
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = code,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.Black,
                letterSpacing = 1.sp
            )
        }
    }
}
