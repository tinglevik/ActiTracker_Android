package com.example.actitracker.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GoalItem(
    val id: Long,
    val name: String,
    val targetSeconds: Long,
    val period: String
) : Parcelable
