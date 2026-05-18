package com.example.actitracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.actitracker.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onBack: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_licenses), color = contentColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button),
                            tint = contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        val licenses = listOf(
            Triple(
                stringResource(R.string.license_material_icons),
                stringResource(R.string.license_apache_title),
                stringResource(R.string.license_apache_header) + "\n\n" + stringResource(R.string.license_apache_text)
            ),
            Triple(
                stringResource(R.string.license_lucide_title),
                stringResource(R.string.license_isc_title),
                stringResource(R.string.license_isc_text)
            ),
            Triple(
                stringResource(R.string.license_phosphor_tabler_title),
                stringResource(R.string.license_mit_title),
                stringResource(R.string.license_mit_text)
            )
        )

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            licenses.forEach { (name, licenseName, text) ->
                Column {
                    LicenseItem(
                        name = name,
                        license = licenseName,
                        contentColor = contentColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = text,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier
                            .background(contentColor.copy(alpha = 0.05f))
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun LicenseItem(name: String, license: String, contentColor: Color) {
    Column {
        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        Text(
            text = license,
            fontSize = 14.sp,
            color = contentColor.copy(alpha = 0.7f)
        )
    }
}
