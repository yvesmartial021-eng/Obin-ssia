package com.example.obinssia.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.obinssia.data.model.SavedDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<SavedDocumentEntity>>

    @Query("SELECT * FROM documents WHERE userId = :userId ORDER BY timestamp DESC")
    fun getDocumentsForUser(userId: String): Flow<List<SavedDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: SavedDocumentEntity)

    @Delete
    suspend fun deleteDocument(doc: SavedDocumentEntity)
}
