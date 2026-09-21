package com.example.model

enum class ColumnType(val label: String) {
  TEXT("Text"),
  NUMBER("Number"),
  BOOLEAN("Boolean"),
  DATE("Date")
}

data class ColumnInfo(
  val name: String,
  val type: ColumnType,
  val index: Int
)

data class CsvParseResult(
  val fileName: String,
  val delimiter: Char,
  val hasHeaders: Boolean,
  val headers: List<String>,
  val rows: List<List<String>>,
  val totalRows: Int,
  val totalColumns: Int,
  val columnTypes: List<ColumnType>,
  val rawTextPreview: String = "",
  val fileSizeFormatted: String = ""
)
