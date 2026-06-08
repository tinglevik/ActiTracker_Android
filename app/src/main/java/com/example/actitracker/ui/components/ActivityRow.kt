package com.example.actitracker.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actitracker.R
import com.example.actitracker.data.model.ActivityItem
import com.example.actitracker.data.model.TagItem
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun ActivityRow(
    activity: ActivityItem,
    isActive: Boolean,
    allTags: List<TagItem> = emptyList(),
    backgroundColor: Color = Color.Transparent,
    contentColor: Color = LocalContentColor.current,
    showTimer: Boolean = true,
    showFirstStart: Boolean = true,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]

    val timeFormatter = remember(locale) {
        android.icu.text.DateFormat
            .getTimeInstance(android.icu.text.DateFormat.SHORT, locale)
    }

    val activityTags = remember(activity.tagIds, allTags) {
        activity.tagIds.mapNotNull { id -> allTags.find { it.id == id } }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(
                    vertical = dimensionResource(R.dimen.activity_row_vertical_padding),
                    horizontal = dimensionResource(R.dimen.activity_row_horizontal_padding)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                        modifier = Modifier
                            .height(
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = activity.name,
                        fontSize = ActivityRowDimens.headerFontSize,
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (showFirstStart) {
                        activity.firstStartDayTime?.let { firstStart ->
                            val timeStr = remember(firstStart, timeFormatter) {
                                timeFormatter.format(Date(firstStart))
                            }
                            Text(
                                text = stringResource(
                                    R.string.first_start_label,
                                    timeStr
                                ),
                                fontSize = ActivityRowDimens.headerFontSize * 0.8,
                                color = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.padding(
                                    top = dimensionResource(
                                        R.dimen.activity_row_first_start_top_padding
                                    )
                                )
                            )
                        }
                    }
                }

                if (activityTags.isNotEmpty()) {

                    val tagRowHeight = dimensionResource(
                        R.dimen.activity_row_tag_row_height
                    )
                    val density = LocalDensity.current

                    val adaptiveFontSize = (12f / density.fontScale).sp
                    val tagIconSize = (tagRowHeight.value * 0.75f).dp

                    Spacer(
                        Modifier.width(
                            dimensionResource(R.dimen.activity_row_section_spacing)
                        )
                    )

                    Row(
                        modifier = Modifier.height(tagRowHeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (activityTags.size == 1) {

                            val tag = activityTags[0]

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Label,
                                contentDescription = null,
                                tint = tag.color,
                                modifier = Modifier.size(tagIconSize)
                            )

                            Spacer(
                                modifier = Modifier.width(
                                    dimensionResource(R.dimen.activity_row_tag_spacing)
                                )
                            )

                            Text(
                                text = tag.name,
                                fontSize = adaptiveFontSize,
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                        } else {

                            activityTags.take(3).forEach { tag ->

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(
                                        end = dimensionResource(
                                            R.dimen.activity_row_tag_spacing
                                        )
                                    )
                                ) {

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Label,
                                        contentDescription = null,
                                        tint = tag.color,
                                        modifier = Modifier.size(tagIconSize * 0.8f)
                                    )

                                    Text(
                                        text = tag.name.take(1).uppercase(),
                                        fontSize = adaptiveFontSize,
                                        color = contentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (activityTags.size > 3) {
                                Spacer(
                                    modifier = Modifier.width(
                                        dimensionResource(R.dimen.activity_row_tag_spacing)
                                    )
                                )
                                Text(
                                    text = "+${activityTags.size - 3}",
                                    fontSize = adaptiveFontSize,
                                    color = contentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(
                    Modifier.width(
                        dimensionResource(R.dimen.activity_row_section_spacing)
                    )
                )

                // ⏱️ Timer
                if (showTimer && (isActive || activity.elapsedSeconds > 0)) {
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
                horizontal = dimensionResource(
                    R.dimen.activity_row_divider_horizontal_padding
                )
            ),
            thickness =
                dimensionResource(R.dimen.activity_row_divider_thickness),
            color = contentColor.copy(alpha = 0.1f)
        )
    }
}

fun formatSeconds(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}

private val EllipsisMoveEasing = CubicBezierEasing(0.25f, 1f, 0.75f, 1f)

@Composable
fun DotsLoader(
    dotSize: Dp = dimensionResource(R.dimen.activity_row_dot_size),
    color: Color = Color.Gray
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ellipsis")

    val rawProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ellipsis-raw"
    )

    val animFraction = 350f / 500f
    val progress = if (rawProgress >= animFraction) {
        1f
    } else {
        val linearInPhase = rawProgress / animFraction
        EllipsisMoveEasing.transform(linearInPhase)
    }

    // Distance between nearest dots
    val spacing = dotSize + dimensionResource(R.dimen.activity_row_dot_spacing)
    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }

    // Container width = 3 dots with distances between
    val containerWidth = dotSize + spacing * 2

    Box(
        modifier = Modifier
            .width(containerWidth)
            .height(dotSize),
        contentAlignment = Alignment.CenterStart
    ) {
        EllipsisDot(
            dotSize = dotSize,
            color = color,
            offsetXPx = 0f,
            scale = progress
        )

        EllipsisDot(
            dotSize = dotSize,
            color = color,
            offsetXPx = spacingPx * progress,
            scale = 1f
        )

        EllipsisDot(
            dotSize = dotSize,
            color = color,
            offsetXPx = spacingPx + spacingPx * progress,
            scale = 1f
        )

        EllipsisDot(
            dotSize = dotSize,
            color = color,
            offsetXPx = spacingPx * 2f,
            scale = 1f - progress
        )
    }
}

@Composable
private fun EllipsisDot(
    dotSize: Dp,
    color: Color,
    offsetXPx: Float,
    scale: Float
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(offsetXPx.roundToInt(), 0) }
            .scale(scale)
            .size(dotSize)
            .background(color, CircleShape)
    )
}

@Preview(showBackground = true)
@Composable
fun ActivityRowPreview() {
    val sampleActivity = ActivityItem(
        id = 1,
        name = "Running very very long and actively",
        icon = "Exercise",
        color = Color(0xFF6200EE),
        elapsedSeconds = 754, // 12:34
        firstStartDayTime = System.currentTimeMillis() - 3_600_000,
        tagIds = listOf(1, 2)
    )

    val sampleTags = listOf(
        TagItem(
            id = 1,
            name = "Sport",
            color = Color(0xFF4CAF50)
        ),
        TagItem(
            id = 2,
            name = "Health",
            color = Color(0xFFFF9800)
        ),
        TagItem(
            id = 3,
            name = "Something1",
            color = Color(0xFF009800)
        ),
        TagItem(
            id = 4,
            name = "Something2",
            color = Color(0xFF55FF00)
        )
    )

    ActivityRow(
        activity = sampleActivity,
        isActive = true,
        allTags = sampleTags,
        showTimer = true,
        showFirstStart = true,
        onClick = {}
    )
}
