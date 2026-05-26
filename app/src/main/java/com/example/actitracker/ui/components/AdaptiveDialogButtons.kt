package com.example.actitracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
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
    deleteContentColor: Color = Color.Red
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isNarrow = maxWidth < 300.dp

        if (isNarrow) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    enabled = confirmEnabled,
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxWidth(),
                    colors = confirmColors
                ) {
                    Text(confirmText)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = dismissContentColor)
                ) {
                    Text(dismissText)
                }

                if (deleteText != null && onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = deleteContentColor)
                    ) {
                        Text(deleteText)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (deleteText != null && onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = deleteContentColor)
                    ) {
                        Text(deleteText)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }

                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = dismissContentColor)
                ) {
                    Text(dismissText)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onConfirm,
                    enabled = confirmEnabled,
                    shape = RectangleShape,
                    colors = confirmColors
                ) {
                    Text(confirmText)
                }
            }
        }
    }
}
