package com.example.actitracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.actitracker.R
import com.example.actitracker.data.model.ActivityItem
import com.example.actitracker.ui.theme.ActitrackerTheme
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableActivityRow(
    activity: ActivityItem,
    isActive: Boolean,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit,
    onSwipe: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]

    val timeFormatter = remember(locale) {
        android.icu.text.DateFormat.getTimeInstance(
            android.icu.text.DateFormat.SHORT,
            locale
        )
    }

    val dismissState = rememberSwipeToDismissBoxState()

    if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
        androidx.compose.runtime.LaunchedEffect(dismissState.targetValue) {
            onSwipe()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(
                        vertical =
                            dimensionResource(R.dimen.activity_row_vertical_padding),
                        horizontal =
                            dimensionResource(R.dimen.activity_row_horizontal_padding)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Icon + carousel
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppIcon(
                            iconName = activity.icon,
                            tint = activity.color,
                            modifier = Modifier.size(
                                dimensionResource(R.dimen.activity_row_icon_size)
                            )
                        )

                        Spacer(
                            modifier = Modifier.height(
                                dimensionResource(R.dimen.activity_row_icon_to_dot_spacing)
                            )
                        )

                        Box(
                            modifier = Modifier.height(
                                dimensionResource(R.dimen.activity_row_dot_size)
                            ),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            if (isActive) {
                                DotsLoader(color = contentColor)
                            }
                        }
                    }

                    Spacer(
                        Modifier.width(
                            dimensionResource(R.dimen.activity_row_section_spacing)
                        )
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(
                                dimensionResource(R.dimen.activity_row_min_height)
                            ),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = activity.name,
                            fontSize = ActivityRowDimens.headerFontSize,
                            fontWeight = FontWeight.Medium,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        activity.firstStartDayTime?.let { firstStart ->
                            val timeStr = remember(firstStart, timeFormatter) {
                                timeFormatter.format(Date(firstStart))
                            }

                            Text(
                                text = stringResource(R.string.started_at_format, timeStr),
                                fontSize = ActivityRowDimens.firstStartDayTimeFontSize,
                                color = contentColor.copy(alpha = 0.7f),
                            )
                        }
                    }

                    if (isActive || activity.elapsedSeconds > 0) {
                        Text(
                            text = formatSeconds(activity.elapsedSeconds),
                            fontSize = ActivityRowDimens.headerFontSize,
                            fontWeight = FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(
                    horizontal =
                        dimensionResource(R.dimen.activity_row_divider_horizontal_padding)
                ),
                thickness = dimensionResource(R.dimen.activity_row_divider_thickness),
                color = contentColor.copy(alpha = 0.1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SwipeableActivityRowPreview() {
    val sampleActivity = ActivityItem(
        id = 1,
        name = "Walking",
        icon = "Walking",
        color = Color(0xFF2196F3),
        elapsedSeconds = 754, // 12:34
        firstStartDayTime = System.currentTimeMillis() - 3_600_000,
        tagIds = listOf(1, 2)
    )

    ActitrackerTheme {
        SwipeableActivityRow(
            activity = sampleActivity,
            isActive = true,
            onClick = {},
            onSwipe = {}
        )
    }
}
