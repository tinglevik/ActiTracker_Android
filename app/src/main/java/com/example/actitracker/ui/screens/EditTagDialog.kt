package com.example.actitracker.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.actitracker.util.ColorSaver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.actitracker.R
import com.example.actitracker.data.model.TagItem
import com.example.actitracker.ui.components.AdaptiveDialogButtons

@Composable
fun EditTagDialog(
    tag: TagItem,
    onDismiss: () -> Unit,
    onSave: (TagItem) -> Unit,
    onDelete: () -> Unit,
    isCreating: Boolean = false,
    dialogBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    dialogContentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var name by rememberSaveable { mutableStateOf(tag.name) }
    var selectedColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(tag.color) }
    var isError by rememberSaveable { mutableStateOf(false) }

    var showColorPicker by rememberSaveable { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dummyFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(if (isLandscape) 0.95f else 0.9f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = dialogBackgroundColor,
        titleContentColor = dialogContentColor,
        textContentColor = dialogContentColor,
        title = {
            Text(
                if (isCreating) stringResource(R.string.create_tag_title) else stringResource(
                    R.string.edit_tag_title
                )
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Box(
                    modifier = Modifier
                        .size(0.dp)
                        .focusRequester(dummyFocusRequester)
                        .focusable()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    label = {
                        Text(
                            stringResource(R.string.tag_name_label),
                            color = if (isError) MaterialTheme.colorScheme.error else dialogContentColor.copy(
                                alpha = 0.7f
                            )
                        )
                    },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(stringResource(R.string.error_tag_name_empty))
                        }
                    },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dialogContentColor,
                        unfocusedTextColor = dialogContentColor,
                        focusedBorderColor = dialogContentColor,
                        unfocusedBorderColor = dialogContentColor.copy(alpha = 0.5f),
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        errorLabelColor = MaterialTheme.colorScheme.error,
                        errorSupportingTextColor = MaterialTheme.colorScheme.error,
                        cursorColor = dialogContentColor
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Color selection button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            showColorPicker = true
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.tag_color_label), color = dialogContentColor)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(selectedColor!!)
                            .border(1.dp, dialogContentColor.copy(alpha = 0.3f), CircleShape)
                    )
                }
            }
        },
        confirmButton = {
            AdaptiveDialogButtons(
                confirmText = stringResource(R.string.save_button),
                onConfirm = {
                    val trimmedName = name.trim()
                    if (trimmedName.isNotEmpty()) {
                        onSave(tag.copy(name = trimmedName, color = selectedColor!!))
                    }
                },
                onDismiss = onDismiss,
                confirmEnabled = name.isNotBlank(),
                confirmColors = ButtonDefaults.buttonColors(
                    containerColor = dialogContentColor,
                    contentColor = dialogBackgroundColor
                ),
                deleteText = if (!isCreating) stringResource(R.string.delete_button) else null,
                onDelete = if (!isCreating) onDelete else null,
                dismissContentColor = dialogContentColor
            )
        },
        dismissButton = null
    )

    if (showColorPicker) {
        Dialog(
            onDismissRequest = { showColorPicker = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ColorPickerScreen(
                    initialColor = selectedColor!!,
                    onColorConfirmed = {
                        selectedColor = it
                        showColorPicker = false
                    },
                    onDismiss = { showColorPicker = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(16.dp))
                        .wrapContentHeight(),
                    backgroundColor = dialogBackgroundColor,
                    contentColor = dialogContentColor,
                    previousColor = tag.color
                )
            }
        }
    }

    val isInspect = LocalInspectionMode.current
    LaunchedEffect(Unit) {
        if (!isInspect) {
            dummyFocusRequester.requestFocus()
        }
    }
}
