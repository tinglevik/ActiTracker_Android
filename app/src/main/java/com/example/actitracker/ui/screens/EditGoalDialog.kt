package com.example.actitracker.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    val dummyFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
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
                            stringResource(R.string.goal_name_label),
                            color = dialogContentColor.copy(alpha = 0.7f)
                        )
                    },
                    isError = isError,
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth(),
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
            dummyFocusRequester.requestFocus()
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
