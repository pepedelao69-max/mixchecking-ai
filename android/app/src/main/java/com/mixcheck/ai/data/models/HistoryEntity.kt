
package com.mixcheck.ai.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analyses")
data class AnalysisHistoryEntity(
    @PrimaryKey val id: String,
    val projectName: String,
    val fileName: String,
    val date: Long,
    val version: Int,
    val overallScore: Int,
    val lufs: Double,
    val truePeak: Double,
    val topIssue: String,
    val isRegional: Boolean
)
