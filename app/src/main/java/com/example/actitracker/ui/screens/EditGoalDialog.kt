package com.example.actitracker.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import com.example.actitracker.R
import com.example.actitracker.data.model.GoalItem
import com.example.actitracker.ui.components.AdaptiveDialogButtons
import com.example.actitracker.ui.theme.ActitrackerTheme

@Composable
fun EditGoalDialog(
    goal: GoalItem,
    onDismiss: () -> Unit,
    onSave: (GoalItem) -> Unit,
    onDelete: () -> Unit,
    isCreating: Boolean = false,
    dialogBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    dialogContentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var name by rememberSaveable { mutableStateOf(goal.name) }
    var targetHours by rememberSaveable { mutableStateOf((goal.targetSeconds / 3600).toString()) }
    var period by rememberSaveable { mutableStateOf(goal.period) }
    var isError by rememberSaveable { mutableStateOf(false) }

    var focusedField by rememberSaveable { mutableStateOf<String?>(null) }
    var isInitialized by remember { mutableStateOf(false) }
    val nameFocusRequester = remember { FocusRequester() }
    val hoursFocusRequester = remember { FocusRequester() }
    val dummyFocusRequester = remember { FocusRequester() }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
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
                if (isCreating) stringResource(R.string.create_goal_title) else stringResource(
                    R.string.edit_goal_title
                )
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Box(
                    modifier = Modifier
                        .size(1.dp)
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
                            stringResource(R.string.goal_name_label),
                            color = dialogContentColor.copy(alpha = 0.7f)
                        )
                    },
                    isError = isError,
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocusRequester)
                        .onFocusChanged {
                            if (isInitialized) {
                                if (it.isFocused) focusedField = "name"
                                else if (focusedField == "name") focusedField = null
                            }
                        },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dialogContentColor,
                        unfocusedTextColor = dialogContentColor,
                        focusedBorderColor = dialogContentColor,
                        unfocusedBorderColor = dialogContentColor.copy(alpha = 0.5f),
                        cursorColor = dialogContentColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetHours,
                    onValueChange = { targetHours = it.filter { char -> char.isDigit() } },
                    label = {
                        Text(
                            stringResource(R.string.target_hours_label),
                            color = dialogContentColor.copy(alpha = 0.7f)
                        )
                    },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(hoursFocusRequester)
                        .onFocusChanged {
                            if (isInitialized) {
                                if (it.isFocused) focusedField = "hours"
                                else if (focusedField == "hours") focusedField = null
                            }
                        },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = dialogContentColor,
                        unfocusedTextColor = dialogContentColor,
                        focusedBorderColor = dialogContentColor,
                        unfocusedBorderColor = dialogContentColor.copy(alpha = 0.5f),
                        cursorColor = dialogContentColor
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.goal_period_label), color = dialogContentColor)
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = period == "DAILY", onClick = { period = "DAILY" })
                        Text(
                            stringResource(R.string.period_daily),
                            color = dialogContentColor
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = period == "WEEKLY", onClick = { period = "WEEKLY" })
                        Text(
                            stringResource(R.string.period_weekly),
                            color = dialogContentColor
                        )
                    }
                }
            }
        },
        confirmButton = {
            AdaptiveDialogButtons(
                confirmText = stringResource(R.string.save_button),
                onConfirm = {
                    if (name.isNotBlank()) {
                        val seconds = (targetHours.toLongOrNull() ?: 0L) * 3600
                        onSave(
                            goal.copy(
                                name = name.trim(),
                                targetSeconds = seconds,
                                period = period
                            )
                        )
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

    val isInspect = LocalInspectionMode.current
    LaunchedEffect(Unit) {
        if (!isInspect) {
            delay(100)
            when (focusedField) {
                "name" -> nameFocusRequester.requestFocus()
                "hours" -> hoursFocusRequester.requestFocus()
                else -> {
                    focusManager.clearFocus(force = true)
                    dummyFocusRequester.requestFocus()
                    keyboardController?.hide()
                }
            }
            isInitialized = true
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditGoalDialogPreview() {
    val sampleGoal = GoalItem(
        id = 1L,
        name = "Study Android",
        targetSeconds = 10800L,
        period = "DAILY"
    )

    ActitrackerTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EditGoalDialog(
                goal = sampleGoal,
                onDismiss = {},
                onSave = {},
                onDelete = {},
                isCreating = false
            )
        }
    }
}
