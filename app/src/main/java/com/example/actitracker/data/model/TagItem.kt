package com.example.actitracker.data.model

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.example.actitracker.util.ColorParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<Color, ColorParceler>
data class TagItem(
    val id: Long,
    val name: String,
    val color: Color,
    val sortOrder: Int = 0
) : Parcelable
