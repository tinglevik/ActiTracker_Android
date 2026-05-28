package com.example.actitracker.ui.components

import android.graphics.LinearGradient
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationValueChanged: (saturation: Float, value: Float) -> Unit,
    modifier: Modifier = Modifier,
    previousColor: Color? = null
) {
    var currentSaturation by remember(saturation) { mutableFloatStateOf(saturation) }
    var currentValue by remember(value) { mutableFloatStateOf(value) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(hue) {
                    detectTapGestures { offset ->
                        val s = (offset.x / size.width).coerceIn(0f, 1f)
                        val v = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                        currentSaturation = s
                        currentValue = v
                        onSaturationValueChanged(s, v)
                    }
                }
                .pointerInput(hue) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val s = (change.position.x / size.width).coerceIn(0f, 1f)
                        val v = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                        currentSaturation = s
                        currentValue = v
                        onSaturationValueChanged(s, v)
                    }
                }
        ) {
            drawSVPanel(hue)
            previousColor?.let { drawPreviousSelector(it) }
            drawSelector(currentSaturation, currentValue)
        }
    }
}

private fun DrawScope.drawSVPanel(hue: Float) {
    val width = size.width
    val height = size.height

    drawIntoCanvas { canvas ->
        val nativeCanvas = canvas.nativeCanvas

        // 1. Draw base color (Pure Hue)
        val huePaint = Paint().apply {
            color = android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
        }
        nativeCanvas.drawRect(0f, 0f, width, height, huePaint)

        // 2. Draw horizontal white-to-transparent gradient (Saturation)
        val satShader = LinearGradient(
            0f, 0f, width, 0f,
            android.graphics.Color.WHITE,
            android.graphics.Color.TRANSPARENT,
            android.graphics.Shader.TileMode.CLAMP
        )
        val satPaint = Paint().apply {
            shader = satShader
        }
        nativeCanvas.drawRect(0f, 0f, width, height, satPaint)

        // 3. Draw vertical transparent-to-black gradient (Value)
        val valShader = LinearGradient(
            0f, 0f, 0f, height,
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.BLACK,
            android.graphics.Shader.TileMode.CLAMP
        )
        val valPaint = Paint().apply {
            shader = valShader
        }
        nativeCanvas.drawRect(0f, 0f, width, height, valPaint)
    }
}

private fun DrawScope.drawSelector(saturation: Float, value: Float) {
    val x = saturation * size.width
    val y = (1f - value) * size.height
    val radius = 12.dp.toPx()

    drawCircle(
        color = Color.White,
        radius = radius,
        center = Offset(x, y),
        style = Stroke(width = 3.dp.toPx())
    )
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = radius + 1.dp.toPx(),
        center = Offset(x, y),
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun DrawScope.drawPreviousSelector(color: Color) {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    val s = hsv[1]
    val v = hsv[2]
    val x = s * size.width
    val y = (1f - v) * size.height
    val radius = 8.dp.toPx()

    drawCircle(
        color = Color.White.copy(alpha = 0.6f),
        radius = radius,
        center = Offset(x, y),
        style = Stroke(width = 2.dp.toPx())
    )
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = radius + 1.dp.toPx(),
        center = Offset(x, y),
        style = Stroke(width = 1.dp.toPx())
    )
}

@Composable
fun HueBar(
    hue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    previousColor: Color? = null
) {
    var currentHue by remember(hue) { mutableFloatStateOf(hue) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val h = (offset.x / size.width * 360f).coerceIn(0f, 360f)
                    currentHue = h
                    onHueChanged(h)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val h = (change.position.x / size.width * 360f).coerceIn(0f, 360f)
                    currentHue = h
                    onHueChanged(h)
                }
            }
    ) {
        drawHueBar()
        previousColor?.let { drawPreviousHueSelector(it) }
        drawHueSelector(currentHue)
    }
}

private fun DrawScope.drawHueBar() {
    val width = size.width
    val height = size.height

    drawIntoCanvas { canvas ->
        val nativeCanvas = canvas.nativeCanvas

        val hueColors = IntArray(361) { i ->
            android.graphics.Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
        }

        val positions = FloatArray(361) { i -> i / 360f }

        val shader = LinearGradient(
            0f, 0f, width, 0f,
            hueColors,
            positions,
            android.graphics.Shader.TileMode.CLAMP
        )

        val paint = Paint().apply {
            this.shader = shader
        }

        nativeCanvas.drawRoundRect(android.graphics.RectF(0f, 0f, width, height), 8f, 8f, paint)
    }
}

private fun DrawScope.drawHueSelector(hue: Float) {
    val x = (hue / 360f) * size.width
    val selectorWidth = 6.dp.toPx()
    val padding = 2.dp.toPx()

    drawRoundRect(
        color = Color.White,
        topLeft = Offset(x - selectorWidth / 2, padding),
        size = androidx.compose.ui.geometry.Size(selectorWidth, size.height - padding * 2),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawPreviousHueSelector(color: Color) {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    val h = hsv[0]
    val x = (h / 360f) * size.width
    val selectorWidth = 4.dp.toPx()
    val padding = 4.dp.toPx()

    drawRoundRect(
        color = Color.White.copy(alpha = 0.6f),
        topLeft = Offset(x - selectorWidth / 2, padding),
        size = androidx.compose.ui.geometry.Size(selectorWidth, size.height - padding * 2),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )
}
