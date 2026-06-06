package com.example.actitracker.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.actitracker.util.ColorSaver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.actitracker.R
import com.example.actitracker.ui.components.AdaptiveDialogButtons
import com.example.actitracker.ui.theme.ActitrackerTheme

@Composable
fun ContrastSuggestionDialog(
    backgroundColor: Color,
    textColor: Color,
    isBackgroundChange: Boolean,
    suggestions: List<Pair<String, Color>>,
    onSuggestionSelected: (Color) -> Unit,
    onOpenColorPicker: () -> Unit,
    onKeepAnyway: () -> Unit,
    onDismiss: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    var selectedColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(suggestions.firstOrNull()?.second) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                    .padding(bottom = 24.dp + bottomPadding)
                    .clickable(enabled = false) {}, // Prevent closing when clicking on dialog
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        stringResource(R.string.contrast_dialog_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        if (isBackgroundChange)
                            stringResource(R.string.contrast_bg_change_desc)
                        else
                            stringResource(R.string.contrast_text_change_desc),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(backgroundColor)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.contrast_preview_text),
                            color = selectedColor ?: textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.contrast_choose_color_label),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Color options
                    suggestions.forEach { (name, color) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedColor = color }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedColor == color,
                                onClick = { selectedColor = color }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.dp, Color.Gray, CircleShape)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(name, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // "Pick manually" button
                    OutlinedButton(
                        onClick = onOpenColorPicker,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape
                    ) {
                        Text(
                            if (isBackgroundChange) stringResource(R.string.contrast_pick_text_manual)
                            else stringResource(R.string.contrast_pick_bg_manual)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    AdaptiveDialogButtons(
                        confirmText = stringResource(R.string.apply_button),
                        onConfirm = { selectedColor?.let { onSuggestionSelected(it) } },
                        onDismiss = onDismiss,
                        confirmEnabled = selectedColor != null,
                        deleteText = stringResource(R.string.contrast_keep_anyway),
                        onDelete = onKeepAnyway,
                        deleteContentColor = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContrastSuggestionDialogPreview() {
    ActitrackerTheme {
        ContrastSuggestionDialog(
            backgroundColor = Color.White,
            textColor = Color.LightGray,
            isBackgroundChange = false,
            suggestions = listOf(
                "Black" to Color.Black,
                "Dark Gray" to Color.DarkGray,
                "Blue" to Color.Blue
            ),
            onSuggestionSelected = {},
            onOpenColorPicker = {},
            onKeepAnyway = {},
            onDismiss = {}
        )
    }
}
