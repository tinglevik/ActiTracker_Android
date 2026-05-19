package com.example.actitracker.ui.screens

import com.example.actitracker.R

enum class ReportPeriod(val displayNameRes: Int) {
    TODAY(R.string.nav_today),
    LAST_7_DAYS(R.string.period_7_days),
    LAST_30_DAYS(R.string.period_30_days),
    LAST_YEAR(R.string.period_year),
    CUSTOM_RANGE(R.string.period_custom)
}