package com.example.actitracker.util

import android.os.Parcel
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import kotlinx.parcelize.Parceler

object ColorParceler : Parceler<Color> {
    override fun create(parcel: Parcel): Color {
        return Color(parcel.readLong().toULong())
    }

    override fun Color.write(parcel: Parcel, flags: Int) {
        parcel.writeLong(this.value.toLong())
    }
}

val ColorSaver = Saver<Color?, Any>(
    save = { it?.value?.toLong() ?: "null" },
    restore = { if (it == "null") null else Color((it as Long).toULong()) }
)
