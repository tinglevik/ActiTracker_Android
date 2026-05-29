package com.example.actitracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actitracker.R
import com.example.actitracker.data.search.IconSearchRepository
import com.example.actitracker.ui.components.AppIcon
import com.example.actitracker.ui.components.IconInfo
import com.example.actitracker.ui.components.IconMapper
import com.example.actitracker.ui.theme.ActitrackerTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerScreen(
    initialIconName: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val dummyFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val searchRepository = remember { IconSearchRepository(context) }

    var allIcons by remember { mutableStateOf<List<IconInfo>>(emptyList()) }
    var semanticResults by remember { mutableStateOf<List<IconInfo>>(emptyList()) }

    /**
     * Initialize search index and load all icons as default
     */
    LaunchedEffect(Unit) {
        searchRepository.initialize()
        // Load all icons and group them by logical category immediately
        val all = searchRepository.getAllIcons().mapNotNull { result ->
            IconMapper.getIconInfo(result.iconId)
        }
        allIcons = all
    }

    /**
     * Incremental search with debounce
     */
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(300) // Debounce 300ms
            val results = searchRepository.search(searchQuery).mapNotNull { result ->
                IconMapper.getIconInfo(result.iconId)
            }
            semanticResults = results
        } else {
            semanticResults = emptyList()
        }
    }

    val filteredIcons = remember(searchQuery, semanticResults, allIcons) {
        if (searchQuery.isBlank()) {
            allIcons
        } else {
            semanticResults
        }
    }

    val groupedIcons = remember(filteredIcons) {
        /**
         *Categories in the desired order from IconMapper
         */
        val order = listOf(
            "General",
            "Activities",
            "Work & Study",
            "Leisure & Travel",
            "Nature",
            "Food & Drinks",
            "Other Symbols"
        )

        filteredIcons.groupBy { it.category }
            .toList()
            .sortedBy { (category, _) ->
                val index = order.indexOf(category)
                /**
                 * If category is not in our list, put it at the end.
                 */
                if (index != -1) index else order.size
            }
    }

    /**
     * Force focus onto a dummy element immediately to prevent keyboard from popping up
     */
    LaunchedEffect(Unit) {
        dummyFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                interactionSource = null,
                indication = null
            ) { focusManager.clearFocus() }
    ) {
        /**
         * Dummy element to hold initial focus
         */
        Box(
            modifier = Modifier
                .size(0.dp)
                .focusRequester(dummyFocusRequester)
                .focusable()
        )

        TopAppBar(
            title = { Text(stringResource(R.string.select_icon_title), color = contentColor) },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button),
                        tint = contentColor
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
        )

        /**
         * Search Bar
         */
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    stringResource(R.string.search_icons_hint),
                    color = contentColor.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = contentColor
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear_search),
                            tint = contentColor
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = contentColor,
                unfocusedTextColor = contentColor,
                focusedBorderColor = contentColor,
                unfocusedBorderColor = contentColor.copy(alpha = 0.3f),
                cursorColor = contentColor
            )
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 44.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            groupedIcons.forEach { (category, iconsInGroup) ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val categoryRes = IconMapper.getCategoryRes(category)
                    val categoryName = if (categoryRes != null) {
                        stringResource(categoryRes)
                    } else {
                        category // Fallback for dynamic categories like "Lucide"
                    }
                    Text(
                        text = categoryName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
                    )
                }

                items(iconsInGroup) { iconInfo ->
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                if (iconInfo.name == initialIconName) contentColor.copy(
                                    alpha = 0.2f
                                ) else Color.Transparent
                            )
                            .clickable { onIconSelected(iconInfo.name) },
                        contentAlignment = Alignment.Center
                    ) {
                        AppIcon(
                            iconName = iconInfo.name,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IconPickerScreenPreview() {
    ActitrackerTheme {
        IconPickerScreen(
            initialIconName = "Star",
            onIconSelected = {},
            onDismiss = {}
        )
    }
}
