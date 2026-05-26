package com.example.actitracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.actitracker.R
import com.example.actitracker.data.model.GoalItem
import com.example.actitracker.ui.components.AdaptiveDialogButtons

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
    var name by remember { mutableStateOf(goal.name) }
    var targetHours by remember { mutableStateOf((goal.targetSeconds / 3600).toString()) }
    var period by remember { mutableStateOf(goal.period) }
    var isError by remember { mutableStateOf(false) }

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
            Column {
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
                Row {
                    RadioButton(selected = period == "DAILY", onClick = { period = "DAILY" })
                    Text(
                        stringResource(R.string.period_daily),
                        modifier = Modifier.padding(start = 8.dp),
                        color = dialogContentColor
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = period == "WEEKLY", onClick = { period = "WEEKLY" })
                    Text(
                        stringResource(R.string.period_weekly),
                        modifier = Modifier.padding(start = 8.dp),
                        color = dialogContentColor
                    )
                }
            }
        },
        confirmButton = {
            AdaptiveDialogButtons(
                confirmText = stringResource(R.string.save_button),
                onConfirm = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
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
}
