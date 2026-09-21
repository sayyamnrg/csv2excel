package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversion_records")
data class ConversionRecord(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val fileName: String,
  val sourceName: String,
  val rowCount: Int,
  val columnCount: Int,
  val fileSizeBytes: Long,
  val delimiterUsed: String,
  val timestamp: Long = System.currentTimeMillis(),
  val localFilePath: String? = null
)
