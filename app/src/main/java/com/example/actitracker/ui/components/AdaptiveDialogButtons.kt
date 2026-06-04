package com.example.actitracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.actitracker.R

@Composable
fun AdaptiveDialogButtons(
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmColors: ButtonColors = ButtonDefaults.buttonColors(),
    dismissText: String = stringResource(R.string.cancel_button),
    dismissContentColor: Color = Color.Unspecified,
    confirmEnabled: Boolean = true,
    deleteText: String? = null,
    onDelete: (() -> Unit)? = null,
    deleteContainerColor: Color = colorResource(R.color.delete_button_bg),
    deleteContentColor: Color = colorResource(R.color.delete_button_text)
) {
    Layout(
        modifier = Modifier.fillMaxWidth(),
        content = {
            // Measurable 0: Confirm
            Button(
                onClick = onConfirm,
                enabled = confirmEnabled,
                shape = RectangleShape,
                colors = confirmColors
            ) {
                Text(confirmText, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
            }

            // Measurable 1: Dismiss
            TextButton(
                onClick = onDismiss,
                shape = RectangleShape,
                colors = ButtonDefaults.textButtonColors(contentColor = dismissContentColor)
            ) {
                Text(dismissText, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
            }

            // Measurable 2: Delete
            if (deleteText != null && onDelete != null) {
                Button(
                    onClick = onDelete,
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = deleteContainerColor,
                        contentColor = deleteContentColor
                    )
                ) {
                    Text(deleteText, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
                }
            }
        }
    ) { measurables, constraints ->
        val spacing = 8.dp.roundToPx()
        val maxAvailableWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else 2000

        // 1. Calculate required widths using intrinsics (without full measurement)
        val confirmReq = measurables[0].maxIntrinsicWidth(constraints.maxHeight)
        val dismissReq =
            if (measurables.size > 1) measurables[1].maxIntrinsicWidth(constraints.maxHeight) else 0
        val deleteReq =
            if (measurables.size > 2) measurables[2].maxIntrinsicWidth(constraints.maxHeight) else 0

        val totalReq = confirmReq + dismissReq + deleteReq + (measurables.size - 1) * spacing

        // All-or-nothing condition
        val fitsInRow = totalReq <= maxAvailableWidth && maxAvailableWidth >= 300.dp.roundToPx()

        if (fitsInRow) {
            // ROW MODE: Measure exactly once with wrap content
            val placeables =
                measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
            val height = placeables.maxOfOrNull { it.height } ?: 0
            val layoutWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else totalReq

            layout(layoutWidth, height) {
                val p0 = placeables[0]
                val p1 = if (placeables.size > 1) placeables[1] else null
                val p2 = if (placeables.size > 2) placeables[2] else null

                // Confirm on far right
                p0.placeRelative(layoutWidth - p0.width, (height - p0.height) / 2)

                // Dismiss to the left of Confirm
                p1?.placeRelative(
                    layoutWidth - p0.width - spacing - p1.width,
                    (height - p1.height) / 2
                )

                // Delete on far left
                p2?.placeRelative(0, (height - p2.height) / 2)
            }
        } else {
            // COLUMN MODE: Measure exactly once with fillMaxWidth
            val layoutWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else 0
            val placeables = measurables.map {
                it.measure(constraints.copy(minWidth = layoutWidth, maxWidth = layoutWidth))
            }

            val totalHeight = placeables.sumOf { it.height } + (placeables.size - 1) * spacing

            layout(layoutWidth, totalHeight) {
                var y = 0
                placeables.forEach { p ->
                    p.placeRelative(0, y)
                    y += p.height + spacing
                }
            }
        }
    }
}
