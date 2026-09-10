
package com.mixcheck.ai.data.local

import androidx.room.*
import com.mixcheck.ai.data.models.AnalysisHistoryEntity

@Dao
interface AnalysisDao {
    @Query("SELECT * FROM analyses ORDER BY date DESC")
    suspend fun getAll(): List<AnalysisHistoryEntity>

    @Query("SELECT * FROM analyses WHERE projectName = :project ORDER BY version DESC")
    suspend fun getByProject(project: String): List<AnalysisHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AnalysisHistoryEntity)

    @Query("DELETE FROM analyses WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM analyses WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AnalysisHistoryEntity?
}
