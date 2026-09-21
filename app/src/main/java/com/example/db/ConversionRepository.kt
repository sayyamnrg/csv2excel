package com.example.db

import kotlinx.coroutines.flow.Flow

class ConversionRepository(private val dao: ConversionDao) {
  val allRecords: Flow<List<ConversionRecord>> = dao.getAllRecords()

  suspend fun insertRecord(record: ConversionRecord): Long = dao.insertRecord(record)

  suspend fun deleteRecord(id: Long) = dao.deleteRecordById(id)

  suspend fun clearHistory() = dao.clearAll()
}
