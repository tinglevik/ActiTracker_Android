package com.example.actitracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.actitracker.R
import com.example.actitracker.ui.components.HueBar
import com.example.actitracker.ui.components.SaturationValuePanel
import com.example.actitracker.ui.theme.ActitrackerTheme

// Helper function: Compose Color → ARGB Int
private fun Color.toArgbInt(): Int = toArgb()

// Helper function: Color → HEX string
private fun colorToHex(color: Color): String {
    val argb = color.toArgbInt()
    return "#%06X".format(argb and 0xFFFFFF)
}

// Helper function: HEX string → Color?
private fun parseHexColor(hex: String): Color? {
    return try {
        val cleaned = if (hex.startsWith("#")) hex else "#$hex"
        if (cleaned.length != 7) return null
        Color(cleaned.toColorInt())
    } catch (_: Exception) {
        null
    }
}

@Composable
fun ColorPickerScreen(
    initialColor: Color,
    onColorConfirmed: (Color) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    contrastWarning: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    previousColor: Color? = null
) {
    val initialHsv = remember(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgbInt(), hsv)
        hsv
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    val selectedColor by remember(hue, saturation, value) {
        mutableStateOf(
            Color(
                android.graphics.Color.HSVToColor(
                    floatArrayOf(
                        hue, saturation, value
                    )
                )
            )
        )
    }

    var hexInput by remember(selectedColor) {
        mutableStateOf(colorToHex(selectedColor))
    }
    var hexError by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dummyFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.material3.LocalContentColor provides contentColor
    ) {
        Column(
            modifier = modifier
                .background(backgroundColor)
                .verticalScroll(scrollState)
                .padding(horizontal = dimensionResource(R.dimen.screen_padding))
        ) {
            Box(
                modifier = Modifier
                    .size(0.dp)
                    .focusRequester(dummyFocusRequester)
                    .focusable()
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.color_picker_title),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(
                            horizontal = dimensionResource(
                                R.dimen.color_picker_title_horizontal_padding
                            )
                        ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )

                IconButton(
                    onClick = onDismiss, modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.cancel_button),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(
                            dimensionResource(R.dimen.color_picker_top_icon_size)
                        )
                    )
                }

                IconButton(
                    onClick = { onColorConfirmed(selectedColor) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = stringResource(R.string.color_picker_confirm),
                        tint = colorResource(R.color.color_confirm_green),
                        modifier = Modifier.size(
                            dimensionResource(R.dimen.color_picker_top_icon_size)
                        )
                    )
                }
            }

            if (contrastWarning != null) {
                Surface(
                    color = contentColor.copy(alpha = 0.08f), shape = RoundedCornerShape(
                        dimensionResource(R.dimen.color_picker_warning_corner)
                    ), modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = dimensionResource(
                                R.dimen.color_picker_warning_bottom_padding
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(
                            dimensionResource(R.dimen.color_picker_warning_padding)
                        ), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info_outline),
                            contentDescription = stringResource(R.string.info_desc),
                            tint = contentColor,
                            modifier = Modifier.size(
                                dimensionResource(R.dimen.color_picker_info_icon_size)
                            )
                        )
                        Spacer(
                            modifier = Modifier.width(
                                dimensionResource(R.dimen.color_picker_warning_spacer)
                            )
                        )
                        Text(
                            text = contrastWarning,
                            color = contentColor,
                            fontSize = with(LocalDensity.current) {
                                dimensionResource(
                                    R.dimen.color_picker_warning_text_size
                                ).toSp()
                            })
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    Text(
                        text = stringResource(R.string.color_picker_current),
                        modifier = Modifier
                            .padding(
                                end = dimensionResource(
                                    R.dimen.color_picker_current_new_padding
                                )
                            )
                            .weight(1f),
                        color = contentColor,
                        textAlign = TextAlign.End
                    )
                    Box(
                        modifier = Modifier
                            .width(
                                dimensionResource(R.dimen.color_picker_divider_width)
                            )
                            .fillMaxHeight()
                            .background(
                                contentColor.copy(alpha = 0.18f)
                            )
                    )
                    Text(
                        text = stringResource(R.string.color_picker_new),
                        modifier = Modifier
                            .padding(
                                start = dimensionResource(
                                    R.dimen.color_picker_current_new_padding
                                )
                            )
                            .weight(1f),
                        color = contentColor,
                        textAlign = TextAlign.Start
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .height(
                                dimensionResource(R.dimen.color_picker_preview_height)
                            )
                            .clip(
                                RoundedCornerShape(
                                    topStart =
                                        dimensionResource(R.dimen.color_picker_preview_corner)
                                )
                            )
                            .background(initialColor)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .height(dimensionResource(R.dimen.color_picker_preview_height))
                            .clip(
                                RoundedCornerShape(
                                    topEnd =
                                        dimensionResource(R.dimen.color_picker_preview_corner)
                                )
                            )
                            .background(selectedColor)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.color_picker_preview_spacer)
                )
            )

            SaturationValuePanel(
                hue = hue,
                saturation = saturation,
                value = value,
                onSaturationValueChanged = { s, v ->
                    saturation = s
                    value = v
                    hexInput = colorToHex(
                        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, s, v)))
                    )
                    hexError = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.color_picker_saturation_panel_height))
                    .clip(
                        RoundedCornerShape(
                            bottomStart = dimensionResource(
                                R.dimen.color_picker_saturation_panel_corner
                            ), bottomEnd = dimensionResource(
                                R.dimen.color_picker_saturation_panel_corner
                            )
                        )
                    ),
                previousColor = previousColor
            )

            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.color_picker_hue_bar_spacer)
                )
            )

            HueBar(
                hue = hue, onHueChanged = { h ->
                    hue = h
                    hexInput = colorToHex(
                        Color(
                            android.graphics.Color.HSVToColor(floatArrayOf(h, saturation, value))
                        )
                    )
                    hexError = false
                },
                previousColor = previousColor
            )

            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.color_picker_hex_spacer)
                )
            )

            OutlinedTextField(
                value = hexInput, onValueChange = { input: String ->
                hexInput = input
                hexError = false

                val parsed = parseHexColor(input)
                if (parsed != null) {
                    val hsv = FloatArray(3)
                    android.graphics.Color.colorToHSV(parsed.toArgbInt(), hsv)
                    hue = hsv[0]
                    saturation = hsv[1]
                    value = hsv[2]
                }
            }, label = {
                Text(
                    stringResource(R.string.color_picker_hex_label),
                    color = contentColor.copy(alpha = 0.7f)
                )
            }, placeholder = {
                Text(
                    stringResource(R.string.color_picker_hex_placeholder),
                    color = contentColor.copy(alpha = 0.5f)
                )
            }, isError = hexError, singleLine = true, keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done
            ), keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    // Shift focus to an invisible element
                    dummyFocusRequester.requestFocus()

                    val parsed = parseHexColor(hexInput)
                    if (parsed == null) {
                        hexError = true
                    }
                }), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = contentColor,
                    unfocusedTextColor = contentColor,
                    focusedBorderColor = contentColor,
                    unfocusedBorderColor = contentColor.copy(alpha = 0.5f),
                    cursorColor = contentColor,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorTextColor = contentColor
                )
            )

            if (hexError) {
                Text(
                    stringResource(R.string.color_picker_hex_error),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = with(LocalDensity.current) {
                        dimensionResource(
                            R.dimen.color_picker_error_text_size
                        ).toSp()
                    },
                    modifier = Modifier.padding(
                        top = dimensionResource(
                            R.dimen.color_picker_error_top_padding
                        )
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(
                    dimensionResource(R.dimen.color_picker_bottom_spacer)
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        dummyFocusRequester.requestFocus()
    }
}

@Preview(showBackground = true)
@Composable
fun ColorPickerScreenPreview() {
    ActitrackerTheme {
        ColorPickerScreen(
            initialColor = Color.Blue,
            onColorConfirmed = {},
            onDismiss = {},
            contrastWarning = stringResource(R.string.color_picker_preview_warning)
        )
    }
}
