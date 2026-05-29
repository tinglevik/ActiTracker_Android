package com.example.actitracker.ui.screens

import android.os.Parcelable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.actitracker.R
import com.example.actitracker.ui.components.AdaptiveDialogButtons
import com.example.actitracker.ui.components.ContrastUtils
import com.example.actitracker.ui.theme.ActitrackerTheme
import com.example.actitracker.util.ColorSaver
import com.example.actitracker.viewmodel.SettingsViewModel
import kotlinx.parcelize.Parcelize
import java.io.InputStreamReader
import java.io.OutputStreamWriter

private enum class ColorPickerTarget { BACKGROUND, TEXT }

@Parcelize
data class BackupOptions(
    val activities: Boolean = true,
    val tags: Boolean = true,
    val goals: Boolean = true,
    val logs: Boolean = true,
    val settings: Boolean = true
) : Parcelable

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToLicenses: () -> Unit,
    contentColor: Color = Color.Black,
    shouldHighlight: Boolean = false
) {
    val backgroundColorState by settingsViewModel.backgroundColor.collectAsState()
    val savedContentColor by settingsViewModel.contentColor.collectAsState()
    val previousBg by settingsViewModel.previousBg.collectAsState()
    val previousText by settingsViewModel.previousText.collectAsState()
    val showWarningDrawer by settingsViewModel.showWarningDrawer.collectAsState()
    val snackbarMessage by settingsViewModel.snackbarMessage.collectAsState()
    val dimScreen by settingsViewModel.dimScreen.collectAsState()
    val highlightDataManagement by settingsViewModel.highlightDataManagement.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(shouldHighlight) {
        if (shouldHighlight) {
            settingsViewModel.triggerHighlight()
        }
    }

    SettingsScreenContent(
        backgroundColorState = backgroundColorState,
        savedContentColor = savedContentColor,
        previousBg = previousBg,
        previousText = previousText,
        showWarningDrawer = showWarningDrawer,
        snackbarMessage = snackbarMessage,
        dimScreen = dimScreen,
        highlightDataManagement = highlightDataManagement,
        onBackgroundColorChange = { settingsViewModel.saveBackgroundColor(it) },
        onContentColorChange = { settingsViewModel.saveContentColor(it) },
        onShowWarning = { bg, txt -> settingsViewModel.showWarning(bg, txt) },
        onNavigateToLicenses = onNavigateToLicenses,
        onClearSnackbar = { settingsViewModel.clearSnackbar() },
        onCreateBackup = { options, onResult ->
            settingsViewModel.createBackup(
                options.activities,
                options.tags,
                options.goals,
                options.logs,
                options.settings,
                onResult
            )
        },
        onRestoreBackup = { json, options ->
            settingsViewModel.restoreBackup(
                json,
                options.activities,
                options.tags,
                options.goals,
                options.logs,
                options.settings,
                context
            )
        },
        onClearData = { options ->
            settingsViewModel.clearData(
                options.activities,
                options.tags,
                options.goals,
                options.logs,
                options.settings,
                context
            )
        },
        onShowSnackbar = { settingsViewModel.showSnackbar(it) },
        contentColor = contentColor
    )
}

@Composable
fun SettingsScreenContent(
    backgroundColorState: Color,
    savedContentColor: Color,
    previousBg: Color,
    previousText: Color,
    showWarningDrawer: Boolean,
    snackbarMessage: String?,
    dimScreen: Boolean,
    highlightDataManagement: Boolean,
    onBackgroundColorChange: (Color) -> Unit,
    onContentColorChange: (Color) -> Unit,
    onShowWarning: (Color, Color) -> Unit,
    onNavigateToLicenses: () -> Unit,
    onClearSnackbar: () -> Unit,
    onCreateBackup: (BackupOptions, (String) -> Unit) -> Unit,
    onRestoreBackup: (String, BackupOptions) -> Unit,
    onClearData: (BackupOptions) -> Unit,
    onShowSnackbar: (String) -> Unit,
    contentColor: Color = Color.Black
) {
    var colorPickerTarget by rememberSaveable { mutableStateOf<ColorPickerTarget?>(null) }
    var showContrastDialog by rememberSaveable { mutableStateOf(false) }
    var pendingColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf<Color?>(null) }
    var contrastDialogSource by rememberSaveable { mutableStateOf<ColorPickerTarget?>(null) }
    var openedFromContrastDialog by rememberSaveable { mutableStateOf(false) }
    var colorBeforeContrastFlow by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf<Color?>(null) }

    var showImportExportDialog by rememberSaveable { mutableStateOf(false) }
    var showImportFlow by rememberSaveable { mutableStateOf(false) }
    var importIsConfirmStep by rememberSaveable { mutableStateOf(false) }

    var showClearDataFlow by rememberSaveable { mutableStateOf(false) }
    var clearDataIsConfirmStep by rememberSaveable { mutableStateOf(false) }

    var importJsonContent by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingImportOptions by rememberSaveable { mutableStateOf<BackupOptions?>(null) }
    var pendingClearOptions by rememberSaveable { mutableStateOf<BackupOptions?>(null) }
    var exportOptions by rememberSaveable { mutableStateOf<BackupOptions?>(null) }
    val context = LocalContext.current
    val exportSuccessMessage = stringResource(R.string.export_success)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            exportOptions?.let { options ->
                onCreateBackup(options) { json ->
                    try {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->
                                writer.write(json)
                            }
                        }
                        onShowSnackbar(exportSuccessMessage)
                    } catch (e: Exception) {
                        onShowSnackbar("Export failed: ${e.message}")
                    }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    importJsonContent = InputStreamReader(inputStream).readText()
                    importIsConfirmStep = false
                    showImportFlow = true
                }
            } catch (e: Exception) {
                onShowSnackbar("Import failed: ${e.message}")
            }
        }
    }

    val onColorConfirmedInternal: (Color) -> Unit = { color ->
        val target = colorPickerTarget
        colorPickerTarget = null
        openedFromContrastDialog = false
        colorBeforeContrastFlow = null
        when (target) {
            ColorPickerTarget.BACKGROUND -> {
                if (!ContrastUtils.isReadable(savedContentColor, color)) {
                    pendingColor = color
                    contrastDialogSource = ColorPickerTarget.BACKGROUND
                    showContrastDialog = true
                } else {
                    onBackgroundColorChange(color)
                }
            }

            ColorPickerTarget.TEXT -> {
                if (!ContrastUtils.isReadable(color, backgroundColorState)) {
                    pendingColor = color
                    contrastDialogSource = ColorPickerTarget.TEXT
                    showContrastDialog = true
                } else {
                    onContentColorChange(color)
                }
            }

            null -> {}
        }
    }

    val onDismissInternal: () -> Unit = {
        if (openedFromContrastDialog && colorBeforeContrastFlow != null) {
            when (contrastDialogSource) {
                ColorPickerTarget.BACKGROUND ->
                    onBackgroundColorChange(colorBeforeContrastFlow!!)

                ColorPickerTarget.TEXT ->
                    onContentColorChange(colorBeforeContrastFlow!!)

                null -> {}
            }
        }
        colorPickerTarget = null
        openedFromContrastDialog = false
        colorBeforeContrastFlow = null
        contrastDialogSource = null
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = backgroundColorState,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->

        LaunchedEffect(snackbarMessage) {
            snackbarMessage?.let {
                snackbarHostState.showSnackbar(it)
                onClearSnackbar()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .alpha(if (dimScreen) 0.5f else 1.0f)
        ) {
            when {
                colorPickerTarget != null && !openedFromContrastDialog -> {
                    val isBackground = colorPickerTarget == ColorPickerTarget.BACKGROUND
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        ColorPickerScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding(),
                            initialColor = if (isBackground)
                                backgroundColorState else savedContentColor,
                            contrastWarning = if (isBackground)
                                stringResource(R.string.contrast_warning_background)
                            else
                                stringResource(R.string.contrast_warning_text),
                            onColorConfirmed = onColorConfirmedInternal,
                            onDismiss = onDismissInternal,
                            backgroundColor = if (showWarningDrawer) previousBg else MaterialTheme.colorScheme.background,
                            contentColor = if (showWarningDrawer) previousText else MaterialTheme.colorScheme.onBackground,
                            previousColor = if (isBackground) previousBg else previousText
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.settings_title),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Text(
                            text = stringResource(R.string.settings_appearance),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        SettingsColorCard(
                            title = stringResource(R.string.settings_background_color),
                            subtitle = stringResource(R.string.settings_background_color_desc),
                            color = backgroundColorState,
                            onClick = {
                                colorPickerTarget = ColorPickerTarget.BACKGROUND
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsColorCard(
                            title = stringResource(R.string.settings_content_color),
                            subtitle = stringResource(R.string.settings_content_color_desc),
                            color = savedContentColor,
                            onClick = {
                                colorPickerTarget = ColorPickerTarget.TEXT
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.settings_data_management),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        SettingsActionCard(
                            title = stringResource(R.string.settings_import_export),
                            subtitle = stringResource(R.string.settings_import_export_desc),
                            icon = Icons.Default.SaveAlt,
                            onClick = { showImportExportDialog = true },
                            highlight = highlightDataManagement
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsActionCard(
                            title = stringResource(R.string.settings_clear_data),
                            subtitle = stringResource(R.string.settings_clear_data_desc),
                            icon = Icons.Default.Delete,
                            onClick = {
                                clearDataIsConfirmStep = false
                                showClearDataFlow = true
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.settings_about),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        SettingsActionCard(
                            title = stringResource(R.string.settings_licenses),
                            subtitle = stringResource(R.string.settings_licenses_desc),
                            icon = Icons.Default.Description,
                            onClick = onNavigateToLicenses
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            if (showImportExportDialog) {
                ImportExportDialog(
                    onDismiss = { showImportExportDialog = false },
                    onExport = { activities, tags, goals, logs, settings ->
                        exportOptions = BackupOptions(activities, tags, goals, logs, settings)
                        exportLauncher.launch("actitracker_backup.json")
                    },
                    onImport = {
                        importLauncher.launch(
                            arrayOf("application/json", "text/plain", "*/*")
                        )
                    }
                )
            }

            if (showImportFlow && importJsonContent != null) {
                ImportFlowContainer(
                    isConfirmStep = importIsConfirmStep,
                    pendingOptions = pendingImportOptions,
                    onDismiss = {
                        showImportFlow = false
                        importJsonContent = null
                    },
                    onGoToConfirm = { options ->
                        pendingImportOptions = options
                        importIsConfirmStep = true
                    },
                    onFinalConfirm = {
                        onRestoreBackup(importJsonContent!!, pendingImportOptions!!)
                        showImportFlow = false
                        showImportExportDialog = false
                        importJsonContent = null
                        pendingImportOptions = null
                    }
                )
            }

            if (showClearDataFlow) {
                ClearDataFlowContainer(
                    isConfirmStep = clearDataIsConfirmStep,
                    pendingOptions = pendingClearOptions,
                    onDismiss = { showClearDataFlow = false },
                    onGoToConfirm = { options ->
                        pendingClearOptions = options
                        clearDataIsConfirmStep = true
                    },
                    onFinalConfirm = {
                        onClearData(pendingClearOptions!!)
                        showClearDataFlow = false
                        pendingClearOptions = null
                    }
                )
            }

            if (colorPickerTarget != null && openedFromContrastDialog) {
                val isBackground = colorPickerTarget == ColorPickerTarget.BACKGROUND
                Dialog(
                    onDismissRequest = onDismissInternal,
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
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .clip(RoundedCornerShape(16.dp))
                                .wrapContentHeight(),
                            initialColor = if (isBackground)
                                backgroundColorState else savedContentColor,
                            contrastWarning = if (isBackground)
                                stringResource(R.string.contrast_warning_background)
                            else
                                stringResource(R.string.contrast_warning_text),
                            onColorConfirmed = onColorConfirmedInternal,
                            onDismiss = onDismissInternal,
                            backgroundColor = previousBg,
                            contentColor = previousText,
                            previousColor = if (isBackground) previousBg else previousText
                        )
                    }
                }
            }

            if (showContrastDialog && pendingColor != null && contrastDialogSource != null) {
                val isFromBg = contrastDialogSource == ColorPickerTarget.BACKGROUND
                ContrastSuggestionDialog(
                    backgroundColor = if (isFromBg) pendingColor!! else backgroundColorState,
                    textColor = if (isFromBg) savedContentColor else pendingColor!!,
                    isBackgroundChange = isFromBg,
                    suggestions = if (isFromBg)
                        ContrastUtils.suggestTextColors(pendingColor!!)
                    else
                        ContrastUtils.suggestBackgroundColors(pendingColor!!),
                    onSuggestionSelected = { suggested ->
                        if (isFromBg) {
                            onBackgroundColorChange(pendingColor!!)
                            onContentColorChange(suggested)
                        } else {
                            onContentColorChange(pendingColor!!)
                            onBackgroundColorChange(suggested)
                        }
                        showContrastDialog = false
                        pendingColor = null
                        contrastDialogSource = null
                    },
                    onOpenColorPicker = {
                        colorBeforeContrastFlow =
                            if (isFromBg) backgroundColorState else savedContentColor
                        openedFromContrastDialog = true
                        if (isFromBg) {
                            onBackgroundColorChange(pendingColor!!)
                            colorPickerTarget = ColorPickerTarget.TEXT
                        } else {
                            onContentColorChange(pendingColor!!)
                            colorPickerTarget = ColorPickerTarget.BACKGROUND
                        }
                        showContrastDialog = false
                        pendingColor = null
                    },
                    onKeepAnyway = {
                        if (isFromBg) {
                            onBackgroundColorChange(pendingColor!!)
                        } else {
                            onContentColorChange(pendingColor!!)
                        }
                        onShowWarning(backgroundColorState, savedContentColor)
                        showContrastDialog = false
                        pendingColor = null
                        contrastDialogSource = null
                    },
                    onDismiss = {
                        showContrastDialog = false
                        pendingColor = null
                        contrastDialogSource = null
                    }
                )
            }
        }
    }
}

@Composable
fun ImportFlowContainer(
    isConfirmStep: Boolean,
    pendingOptions: BackupOptions?,
    onDismiss: () -> Unit,
    onGoToConfirm: (BackupOptions) -> Unit,
    onFinalConfirm: () -> Unit
) {
    val fixedCardBg = Color(0xFF1E1E1E)
    val fixedTitleColor = Color(0xFFF5F5F5)
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // STEP 1: Selection (Fade exit only)
            var activities by rememberSaveable { mutableStateOf(true) }
            var tags by rememberSaveable { mutableStateOf(true) }
            var goals by rememberSaveable { mutableStateOf(true) }
            var logs by rememberSaveable { mutableStateOf(true) }
            var settings by rememberSaveable { mutableStateOf(true) }

            AnimatedVisibility(
                visible = !isConfirmStep,
                enter = fadeIn(tween(0)),
                exit = fadeOut(tween(1500))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(28.dp),
                    color = fixedCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            stringResource(R.string.import_title),
                            color = fixedTitleColor,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.import_confirm_desc),
                            color = fixedTitleColor.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = activities, onCheckedChange = { activities = it })
                            Text(
                                stringResource(R.string.export_activities),
                                color = fixedTitleColor
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = tags, onCheckedChange = { tags = it })
                            Text(stringResource(R.string.export_tags), color = fixedTitleColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = goals, onCheckedChange = { goals = it })
                            Text(stringResource(R.string.export_goals), color = fixedTitleColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = logs, onCheckedChange = { logs = it })
                            Text(stringResource(R.string.export_logs), color = fixedTitleColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = settings, onCheckedChange = { settings = it })
                            Text(stringResource(R.string.export_settings), color = fixedTitleColor)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        AdaptiveDialogButtons(
                            confirmText = stringResource(R.string.import_button),
                            onConfirm = {
                                onGoToConfirm(
                                    BackupOptions(
                                        activities,
                                        tags,
                                        goals,
                                        logs,
                                        settings
                                    )
                                )
                            },
                            onDismiss = onDismiss,
                            dismissContentColor = fixedTitleColor
                        )
                    }
                }
            }

            // STEP 2: Confirmation (Instant appearance)
            if (isConfirmStep && pendingOptions != null) {
                val confirmScrollState = rememberScrollState()
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(28.dp),
                    color = fixedCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(confirmScrollState)
                    ) {
                        Text(
                            stringResource(R.string.import_confirm_title),
                            color = fixedTitleColor,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.import_confirm_desc),
                            color = fixedTitleColor.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.import_confirm_question),
                            color = fixedTitleColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        AdaptiveDialogButtons(
                            confirmText = stringResource(R.string.continue_button),
                            onConfirm = onFinalConfirm,
                            onDismiss = onDismiss,
                            dismissContentColor = fixedTitleColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClearDataFlowContainer(
    isConfirmStep: Boolean,
    pendingOptions: BackupOptions?,
    onDismiss: () -> Unit,
    onGoToConfirm: (BackupOptions) -> Unit,
    onFinalConfirm: () -> Unit
) {
    val fixedCardBg = Color(0xFF1E1E1E)
    val fixedTitleColor = Color(0xFFF5F5F5)
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // STEP 1: Selection
            var activities by rememberSaveable { mutableStateOf(false) }
            var tags by rememberSaveable { mutableStateOf(false) }
            var goals by rememberSaveable { mutableStateOf(false) }
            var logs by rememberSaveable { mutableStateOf(false) }
            var settings by rememberSaveable { mutableStateOf(false) }

            AnimatedVisibility(
                visible = !isConfirmStep,
                enter = fadeIn(tween(0)),
                exit = fadeOut(tween(1500))
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(28.dp),
                    color = fixedCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            stringResource(R.string.clear_data_title),
                            color = fixedTitleColor,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = activities, onCheckedChange = { activities = it })
                            Text(
                                stringResource(R.string.export_activities),
                                color = fixedTitleColor
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = tags, onCheckedChange = { tags = it })
                            Text(stringResource(R.string.export_tags), color = fixedTitleColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = goals, onCheckedChange = { goals = it })
                            Text(stringResource(R.string.export_goals), color = fixedTitleColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = logs, onCheckedChange = { logs = it })
                            Text(stringResource(R.string.export_logs), color = fixedTitleColor)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = settings, onCheckedChange = { settings = it })
                            Text(stringResource(R.string.export_settings), color = fixedTitleColor)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        AdaptiveDialogButtons(
                            confirmText = stringResource(R.string.clear_button),
                            onConfirm = {
                                onGoToConfirm(
                                    BackupOptions(
                                        activities,
                                        tags,
                                        goals,
                                        logs,
                                        settings
                                    )
                                )
                            },
                            onDismiss = onDismiss,
                            confirmEnabled = activities || tags || goals || logs || settings,
                            confirmColors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.delete_button_bg),
                                contentColor = colorResource(R.color.delete_button_text)
                            ),
                            dismissContentColor = fixedTitleColor
                        )
                    }
                }
            }

            // STEP 2: Confirmation
            if (isConfirmStep && pendingOptions != null) {
                val deletedItems = mutableListOf<String>()
                if (pendingOptions.activities) deletedItems.add(stringResource(R.string.export_activities))
                if (pendingOptions.tags) deletedItems.add(stringResource(R.string.export_tags))
                if (pendingOptions.goals) deletedItems.add(stringResource(R.string.export_goals))
                if (pendingOptions.logs) deletedItems.add(stringResource(R.string.export_logs))
                if (pendingOptions.settings) deletedItems.add(stringResource(R.string.export_settings))

                val confirmScrollState = rememberScrollState()
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(28.dp),
                    color = fixedCardBg
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(confirmScrollState)
                    ) {
                        Text(
                            stringResource(R.string.clear_confirm_title),
                            color = fixedTitleColor,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(
                                R.string.clear_confirm_desc,
                                deletedItems.joinToString(", ")
                            ), color = fixedTitleColor.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        AdaptiveDialogButtons(
                            confirmText = stringResource(R.string.clear_button),
                            onConfirm = onFinalConfirm,
                            onDismiss = onDismiss,
                            confirmColors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.delete_button_bg),
                                contentColor = colorResource(R.color.delete_button_text)
                            ),
                            dismissContentColor = fixedTitleColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImportExportDialog(
    onDismiss: () -> Unit,
    onExport: (
        activities: Boolean,
        tags: Boolean,
        goals: Boolean,
        logs: Boolean,
        settings: Boolean
    ) -> Unit,
    onImport: () -> Unit,
) {
    var activities by rememberSaveable { mutableStateOf(true) }
    var tags by rememberSaveable { mutableStateOf(true) }
    var goals by rememberSaveable { mutableStateOf(true) }
    var logs by rememberSaveable { mutableStateOf(true) }
    var settings by rememberSaveable { mutableStateOf(true) }

    val fixedCardBg = Color(0xFF1E1E1E)
    val fixedTitleColor = Color(0xFFF5F5F5)
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = fixedCardBg,
        title = { Text(stringResource(R.string.settings_import_export), color = fixedTitleColor) },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = activities, onCheckedChange = { activities = it })
                    Text(stringResource(R.string.export_activities), color = fixedTitleColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = tags, onCheckedChange = { tags = it })
                    Text(stringResource(R.string.export_tags), color = fixedTitleColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = goals, onCheckedChange = { goals = it })
                    Text(stringResource(R.string.export_goals), color = fixedTitleColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = logs, onCheckedChange = { logs = it })
                    Text(stringResource(R.string.export_logs), color = fixedTitleColor)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = settings, onCheckedChange = { settings = it })
                    Text(stringResource(R.string.export_settings), color = fixedTitleColor)
                }
            }
        },
        confirmButton = {
            AdaptiveDialogButtons(
                confirmText = stringResource(R.string.export_button),
                onConfirm = {
                    onExport(activities, tags, goals, logs, settings)
                    onDismiss()
                },
                onDismiss = onDismiss,
                deleteText = stringResource(R.string.import_button),
                onDelete = onImport,
                deleteContainerColor = Color.Transparent, // Transparent since it's an action, not deletion
                deleteContentColor = MaterialTheme.colorScheme.primary, // Using primary for non-destructive action
                dismissContentColor = fixedTitleColor
            )
        },
        dismissButton = null
    )
}

@Composable
private fun SettingsColorCard(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    val fixedCardBg = Color(0xFF1E1E1E)
    val fixedTitleColor = Color(0xFFF5F5F5)
    val fixedSubtitleColor = Color(0xFFB0B0B0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = fixedCardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = fixedTitleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = fixedSubtitleColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            )
        }
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    highlight: Boolean = false
) {
    val fixedCardBg = Color(0xFF1E1E1E)
    val fixedTitleColor = Color(0xFFF5F5F5)
    val fixedSubtitleColor = Color(0xFFB0B0B0)

    val animatedBg by animateColorAsState(
        targetValue = if (highlight) Color(0xFFA4A4A4) else fixedCardBg,
        animationSpec = tween(durationMillis = 300),
        label = "bgColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = animatedBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = fixedTitleColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = fixedSubtitleColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fixedSubtitleColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ActitrackerTheme {
        SettingsScreenContent(
            backgroundColorState = Color.White,
            savedContentColor = Color.Black,
            previousBg = Color.White,
            previousText = Color.Black,
            showWarningDrawer = false,
            snackbarMessage = null,
            dimScreen = false,
            highlightDataManagement = false,
            onBackgroundColorChange = {},
            onContentColorChange = {},
            onShowWarning = { _, _ -> },
            onNavigateToLicenses = {},
            onClearSnackbar = {},
            onCreateBackup = { _, _ -> },
            onRestoreBackup = { _, _ -> },
            onClearData = { _ -> },
            onShowSnackbar = {},
            contentColor = Color.Black
        )
    }
}
