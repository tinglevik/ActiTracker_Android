package com.example.actitracker.ui.screens

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.actitracker.R
import com.example.actitracker.ui.components.ReportScreenDimens
import com.example.actitracker.ui.theme.ActitrackerTheme
import com.example.actitracker.viewmodel.ReportMode
import com.example.actitracker.viewmodel.ReportStats
import com.example.actitracker.viewmodel.ReportViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.Calendar
import java.util.Date
import android.graphics.Color as AndroidColor

@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    contentColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    val period by viewModel.selectedPeriod.collectAsState()
    val reportStats by viewModel.statsData.collectAsState()
    val reportMode by viewModel.reportMode.collectAsState()
    val dateOffset by viewModel.dateOffset.collectAsState()
    val customRange by viewModel.customDateRange.collectAsState()

    var showRangePicker by rememberSaveable { mutableStateOf(false) }

    if (showRangePicker) {
        CustomDateRangePicker(
            onDismiss = { showRangePicker = false },
            onRangeSelected = { from, to ->
                viewModel.setCustomRange(from, to)
                showRangePicker = false
            }
        )
    }

    ReportScreenContent(
        period = period,
        reportStats = reportStats,
        reportMode = reportMode,
        dateOffset = dateOffset,
        customRange = customRange,
        contentColor = contentColor,
        backgroundColor = backgroundColor,
        onPeriodSelected = {
            if (it == ReportPeriod.CUSTOM_RANGE) {
                showRangePicker = true
            } else {
                viewModel.selectPeriod(it)
            }
        },
        onPreviousDay = { viewModel.previousDay() },
        onNextDay = { viewModel.nextDay() },
        onToggleReportMode = { viewModel.toggleReportMode() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePicker(
    onDismiss: () -> Unit,
    onRangeSelected: (Long, Long) -> Unit
) {
    val state = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = state.selectedStartDateMillis
                    val end = state.selectedEndDateMillis
                    if (start != null && end != null) {
                        // Set end to end of day
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = end
                        cal.set(Calendar.HOUR_OF_DAY, 23)
                        cal.set(Calendar.MINUTE, 59)
                        cal.set(Calendar.SECOND, 59)
                        cal.set(Calendar.MILLISECOND, 999)
                        onRangeSelected(start, cal.timeInMillis)
                    }
                },
                enabled =
                    state.selectedStartDateMillis != null && state.selectedEndDateMillis != null
            ) {
                Text(stringResource(R.string.ok_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    ) {
        DateRangePicker(
            state = state,
            title = {
                Text(
                    text = stringResource(R.string.select_date_range),
                    modifier = Modifier.padding(16.dp)
                )
            },
            headline = {
                // Default headline is fine or we can customize
            },
            showModeToggle = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ReportScreenContent(
    period: ReportPeriod,
    reportStats: ReportStats,
    reportMode: ReportMode,
    dateOffset: Int,
    customRange: Pair<Long, Long>? = null,
    contentColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    onPeriodSelected: (ReportPeriod) -> Unit = {},
    onPreviousDay: () -> Unit = {},
    onNextDay: () -> Unit = {},
    onToggleReportMode: () -> Unit = {}
) {
    val sortedReport = remember(reportStats) {
        reportStats.data.filter { it.value > 0 }.toList().sortedByDescending { it.second }
    }

    val scrollState = rememberLazyListState()
    var expanded by rememberSaveable { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val locale = configuration.locales[0]
    val dateText = remember(dateOffset, period, customRange, locale) {
        if (period == ReportPeriod.TODAY) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, dateOffset)
            java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG, locale)
                .format(cal.time)
        } else if (period == ReportPeriod.CUSTOM_RANGE) {
            customRange?.let {
                val df = java.text.DateFormat.getDateInstance(
                    java.text.DateFormat.SHORT,
                    locale
                )
                "${df.format(Date(it.first))} - ${df.format(Date(it.second))}"
            } ?: ""
        } else ""
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            // Header: Dropdown, Date, Arrows in one Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ReportScreenDimens.paddingGeneral, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PeriodDropdown(
                    period = period,
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    onPeriodSelected = onPeriodSelected,
                    contentColor = contentColor,
                    backgroundColor = backgroundColor,
                    modifier = Modifier.weight(1f)
                )

                if (dateText.isNotEmpty()) {
                    Text(
                        text = dateText,
                        color = contentColor,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                if (period != ReportPeriod.CUSTOM_RANGE) {
                    NavigationArrows(
                        onPreviousDay = onPreviousDay,
                        onNextDay = onNextDay,
                        contentColor = contentColor
                    )
                }
            }

            // Body: Chart (left), List (right)
            Row(modifier = Modifier.fillMaxSize()) {
                ChartWithToggle(
                    reportStats = reportStats,
                    reportMode = reportMode,
                    contentColor = contentColor,
                    backgroundColor = backgroundColor,
                    onToggleReportMode = onToggleReportMode,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                ReportList(
                    sortedReport = sortedReport,
                    reportStats = reportStats,
                    reportMode = reportMode,
                    contentColor = contentColor,
                    scrollState = scrollState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            // Separate rows for Portrait
            PeriodDropdown(
                period = period,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onPeriodSelected = onPeriodSelected,
                contentColor = contentColor,
                backgroundColor = backgroundColor
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ReportScreenDimens.paddingGeneral, vertical = 0.dp)
            ) {
                if (dateText.isNotEmpty()) {
                    Text(
                        text = dateText,
                        color = contentColor,
                        modifier = Modifier.align(Alignment.CenterStart),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (period != ReportPeriod.CUSTOM_RANGE) {
                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        NavigationArrows(
                            onPreviousDay = onPreviousDay,
                            onNextDay = onNextDay,
                            contentColor = contentColor
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                ChartWithToggle(
                    reportStats = reportStats,
                    reportMode = reportMode,
                    contentColor = contentColor,
                    backgroundColor = backgroundColor,
                    onToggleReportMode = onToggleReportMode,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ReportScreenDimens.paddingGeneral))

                ReportList(
                    sortedReport = sortedReport,
                    reportStats = reportStats,
                    reportMode = reportMode,
                    contentColor = contentColor,
                    scrollState = scrollState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun PeriodDropdown(
    period: ReportPeriod,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPeriodSelected: (ReportPeriod) -> Unit,
    contentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable { onExpandedChange(true) }
            .padding(vertical = 12.dp, horizontal = ReportScreenDimens.paddingGeneral)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = if (modifier == Modifier) Modifier.fillMaxWidth() else Modifier
        ) {
            Text(
                text = stringResource(period.displayNameRes),
                color = contentColor,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(backgroundColor)
        ) {
            ReportPeriod.entries.forEach { p ->
                val isSelected = p == period
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(p.displayNameRes),
                            color =
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else contentColor,
                            style =
                                if (isSelected) MaterialTheme.typography.titleMedium
                                else MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        onPeriodSelected(p)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
fun NavigationArrows(
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    contentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .padding(
                    horizontal = ReportScreenDimens.paddingGeneral,
                    vertical = ReportScreenDimens.paddingGeneral / 2
                )
                .size(36.dp)
                .clickable { onPreviousDay() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left_circle),
                contentDescription = stringResource(R.string.prev_period),
                tint = contentColor
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clickable { onNextDay() }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right_circle),
                contentDescription = stringResource(R.string.next_period),
                tint = contentColor
            )
        }
    }
}

@Composable
fun ChartWithToggle(
    reportStats: ReportStats,
    reportMode: ReportMode,
    contentColor: Color,
    backgroundColor: Color,
    onToggleReportMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val chartModifier = if (
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            Modifier
                .fillMaxHeight()
                .padding(16.dp)
        } else {
            Modifier.size(280.dp)
        }
            .aspectRatio(1f)
            .clip(CircleShape)

        ReportPieChart(
            reportStats = reportStats,
            mode = reportMode,
            contentColor = contentColor,
            backgroundColor = backgroundColor,
            modifier = chartModifier
        )

        // Mode toggle (Activities / Tags)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = ReportScreenDimens.paddingGeneral / 2)
                .clickable { onToggleReportMode() },
            contentAlignment = Alignment.Center
        ) {
            val iconRes = if (reportMode == ReportMode.ACTIVITIES)
                R.drawable.ic_today_outline else R.drawable.ic_label_outline
            val contentDesc = if (reportMode == ReportMode.ACTIVITIES)
                R.string.switch_to_tags else R.string.switch_to_activities

            Icon(
                painter = painterResource(iconRes),
                contentDescription = stringResource(contentDesc),
                tint = contentColor,
                modifier = Modifier
                    .padding(ReportScreenDimens.paddingGeneral / 2)
                    .size(24.dp)
            )
        }
    }
}

@Composable
fun ReportList(
    sortedReport: List<Pair<String, Long>>,
    reportStats: ReportStats,
    reportMode: ReportMode,
    contentColor: Color,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = scrollState
    ) {
        if (sortedReport.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.no_report_data),
                    color = contentColor,
                    modifier = Modifier.padding(ReportScreenDimens.paddingGeneral)
                )
            }
        } else {
            items(sortedReport) { (name, seconds) ->
                ReportActivityItem(
                    name = name,
                    seconds = seconds,
                    color = reportStats.colors[name] ?: Color.Gray,
                    reportMode = reportMode,
                    contentColor = contentColor
                )
            }
        }
    }
}

@Composable
fun ReportActivityItem(
    name: String,
    seconds: Long,
    color: Color,
    reportMode: ReportMode,
    contentColor: Color
) {
    val noTagLabel = stringResource(R.string.no_tag_label)
    val unknownTagLabel = stringResource(R.string.unknown_tag_label)
    val displayName = when (name) {
        "No Tag" -> noTagLabel
        "Unknown Tag" -> unknownTagLabel
        else -> name
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ReportScreenDimens.paddingGeneral,
                vertical = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical narrow color strip
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (reportMode == ReportMode.TAGS && name != "No Tag") {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    displayName,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(formatTime(seconds), color = contentColor)
        }
    }
}

@Composable
fun ReportPieChart(
    reportStats: ReportStats,
    mode: ReportMode,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    val contentColorArgb = remember(contentColor) { contentColor.toArgb() }
    val contentColorArgbLighted = remember(contentColor) {
        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(contentColor.toArgb(), hsl)
        hsl[2] = (hsl[2] + 0.7f).coerceAtMost(1f) // increase lightness
        androidx.core.graphics.ColorUtils.HSLToColor(hsl)
    }
    val backgroundColorArgb = remember(backgroundColor) { backgroundColor.toArgb() }
    val totalStr = remember(reportStats.totalSeconds) {
        formatTime(reportStats.totalSeconds)
    }

    val noTagLabel = stringResource(R.string.no_tag_label)

    // Thickness in DP
    val strokeWidthDp = 2f

    AndroidView(
        factory = { ctx: Context ->
            PieChart(ctx).apply {
                description.isEnabled = false
                isRotationEnabled = false
                setTouchEnabled(false)
                setUsePercentValues(true)
                legend.isEnabled = false

                isDrawHoleEnabled = true
                setHoleColor(backgroundColorArgb)

                // Border for the hole using transparent circle
                setTransparentCircleColor(contentColorArgb)
                setTransparentCircleAlpha(255)
                holeRadius = 48f
                transparentCircleRadius = 51f

                setDrawCenterText(true)
                setCenterTextColor(contentColorArgbLighted)

                // Background of the chart view will be visible through sliceSpace and extraOffsets
                setBackgroundColor(contentColorArgb)
                setExtraOffsets(
                    strokeWidthDp,
                    strokeWidthDp,
                    strokeWidthDp,
                    strokeWidthDp
                )
            }
        },
        update = { pieChart ->
            val filteredData = if (mode == ReportMode.TAGS) {
                reportStats.data.filter { it.key != "No Tag" && it.value > 0 }
            } else {
                reportStats.data.filter { it.value > 0 }
            }

            val entries = filteredData.map { entry ->
                val label = if (entry.key == "No Tag") noTagLabel else entry.key
                PieEntry(entry.value.toFloat(), label)
            }.toMutableList()
            val sliceColors = filteredData.map { (name, _) ->
                reportStats.colors[name]?.toArgb() ?: AndroidColor.GRAY
            }

            // Update theme colors
            pieChart.setHoleColor(backgroundColorArgb)
            pieChart.setTransparentCircleColor(contentColorArgbLighted)
            pieChart.setBackgroundColor(contentColorArgbLighted)
            pieChart.setCenterTextColor(contentColorArgb)
            pieChart.setEntryLabelTextSize(11f)
            pieChart.setEntryLabelColor(contentColorArgb)

            if (entries.isEmpty()) {
                pieChart.centerText = ""
                pieChart.data = null
            } else {
                val totalLabel = pieChart.context.getString(R.string.total_time_label)
                pieChart.centerText = "$totalLabel\n$totalStr"
                pieChart.setCenterTextSize(14f)

                val dataSet = PieDataSet(entries, "").apply {
                    colors = sliceColors
                    sliceSpace = strokeWidthDp
                    valueTextColor = contentColorArgb
                    valueTextSize = 10f
                    setDrawValues(false) // Cleaner look with border
                }
                pieChart.data = PieData(dataSet)
            }

            pieChart.invalidate()
        },
        modifier = modifier
    )
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    ActitrackerTheme {
        ReportScreenContent(
            period = ReportPeriod.TODAY,
            reportStats = SampleReportStats,
            reportMode = ReportMode.ACTIVITIES,
            dateOffset = 0
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReportPieChartPreview() {
    ActitrackerTheme {
        ReportPieChart(
            reportStats = SampleReportStats,
            mode = ReportMode.ACTIVITIES
        )
    }
}

private val SampleReportStats = ReportStats(
    data = mapOf(
        "Coding" to 3600L * 4,
        "Reading" to 3600L * 2,
        "Exercise" to 1800L,
        "Sleep" to 3600L * 8
    ),
    colors = mapOf(
        "Coding" to Color(0xFF2196F3),
        "Reading" to Color(0xFFFF9800),
        "Exercise" to Color(0xFF4CAF50),
        "Sleep" to Color(0xFF9C27B0)
    ),
    totalSeconds = (3600L * 4) + (3600L * 2) + 1800L + (3600L * 8)
)
