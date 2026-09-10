
package com.mixcheck.ai.data.repository

import android.content.Context
import com.mixcheck.ai.audio.models.AnalysisResult
import com.mixcheck.ai.data.local.AppDatabase
import com.mixcheck.ai.data.models.AnalysisHistoryEntity
import java.util.UUID

class HistoryRepository(context: Context) {
    private val dao = AppDatabase.get(context).analysisDao()

    suspend fun save(projectName: String, result: AnalysisResult, version: Int): String {
        val id = UUID.randomUUID().toString()
        val entity = AnalysisHistoryEntity(
            id = id,
            projectName = projectName,
            fileName = result.fileInfo.fileName,
            date = System.currentTimeMillis(),
            version = version,
            overallScore = result.scores.overall,
            lufs = result.loudness.integratedLufs,
            truePeak = result.loudness.truePeakDbTp,
            topIssue = result.issuesTop5.firstOrNull()?.title ?: "Sin problemas",
            isRegional = true
        )
        dao.insert(entity)
        return id
    }

    suspend fun getAll() = dao.getAll()
    suspend fun getByProject(name: String) = dao.getByProject(name)
}
