package com.example.actitracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.actitracker.R
import com.example.actitracker.data.model.ActivityItem
import com.example.actitracker.ui.components.AppIcon

@Composable
fun QuickPanelToggleDialog(
    activity: ActivityItem,
    onDismiss: () -> Unit,
    onToggle: (ActivityItem) -> Unit,
    dialogBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    dialogContentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val newState = !activity.showInQuickPanel
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBackgroundColor,
        titleContentColor = dialogContentColor,
        textContentColor = dialogContentColor,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                AppIcon(
                    iconName = activity.icon,
                    tint = activity.color,
                    modifier = Modifier.size(
                        dimensionResource(R.dimen.activity_row_icon_size)
                    )
                )
                Spacer(
                    modifier = Modifier.width(
                        dimensionResource(R.dimen.activity_row_tag_spacing)
                    )
                )
                Text(
                    text = activity.name,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Text(
                    text = if (newState)
                        stringResource(
                            R.string.quick_panel_add_desc
                        )
                    else
                        stringResource(
                            R.string.quick_panel_remove_desc
                        )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onToggle(activity.copy(showInQuickPanel = newState))
                    onDismiss()
                },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = dialogContentColor,
                    contentColor = dialogBackgroundColor
                )
            ) {
                Text(
                    if (newState) stringResource(R.string.quick_panel_add_button) else stringResource(
                        R.string.quick_panel_remove_button
                    )
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = dialogContentColor
                )
            ) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}
