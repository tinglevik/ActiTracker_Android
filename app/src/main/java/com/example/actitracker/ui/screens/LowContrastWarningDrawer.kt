package com.example.actitracker.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actitracker.R
import kotlinx.coroutines.delay

private val DRAWER_BG = Color(0xFF1E1E1E)
private val DRAWER_TEXT = Color(0xFFFFFFFF)
private val DRAWER_ACCENT = Color(0xFFFFD54F)
private val DRAWER_DANGER = Color(0xFFEF5350)
private val PEEK_WIDTH = 56.dp
private val FULL_WIDTH = 260.dp

@Composable
fun LowContrastWarningDrawer(
    onRevert: () -> Unit,
    onKeep: () -> Unit,
    onTimeout: () -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var secondsLeft by rememberSaveable { mutableIntStateOf(30) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
        onTimeout()
    }

    val currentWidth by animateDpAsState(
        targetValue = if (isExpanded) FULL_WIDTH else PEEK_WIDTH,
        animationSpec = tween(durationMillis = 250),
        label = "drawer_width"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { isExpanded = false }
            )
        }

        Box(
            modifier = Modifier
                .width(currentWidth)
                .wrapContentHeight() 
                .background(
                    color = DRAWER_BG,
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        when {
                            dragAmount > 20f && isExpanded -> isExpanded = false
                            dragAmount < -20f && !isExpanded -> isExpanded = true
                        }
                    }
                }
                .padding(vertical = 16.dp)
        ) {
            if (isExpanded) {
                ExpandedDrawer(
                    secondsLeft = secondsLeft,
                    onCollapse = { isExpanded = false },
                    onRevert = onRevert,
                    onKeep = onKeep
                )
            } else {
                CollapsedDrawer(
                    secondsLeft = secondsLeft,
                    onExpand = { isExpanded = true }
                )
            }
        }
    }
}

@Composable
private fun CollapsedDrawer(
    secondsLeft: Int,
    onExpand: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(PEEK_WIDTH)
            .clickable { onExpand() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⏱️", fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${secondsLeft}s",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DRAWER_ACCENT,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ExpandedDrawer(
    secondsLeft: Int,
    onCollapse: () -> Unit,
    onRevert: () -> Unit,
    onKeep: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "⏱️", fontSize = 28.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.contrast_drawer_title),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DRAWER_ACCENT,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.contrast_drawer_desc),
            fontSize = 13.sp,
            color = DRAWER_TEXT,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.contrast_reverting_in, secondsLeft),
            fontSize = 12.sp,
            color = DRAWER_ACCENT,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRevert,
            colors = ButtonDefaults.buttonColors(containerColor = DRAWER_DANGER),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.revert_button), fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onKeep,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DRAWER_TEXT),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.keep_changes_button), fontSize = 13.sp)
        }
        TextButton(onClick = onCollapse) {
            Text(stringResource(R.string.minimize_button), fontSize = 12.sp, color = DRAWER_TEXT.copy(alpha = 0.5f))
        }
    }
}
