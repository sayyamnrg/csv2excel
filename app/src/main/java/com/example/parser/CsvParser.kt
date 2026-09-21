package com.example.parser

import com.example.model.ColumnType
import com.example.model.CsvParseResult
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object CsvParser {

  private val DATE_PATTERN_1 = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$") // 2024-01-31
  private val DATE_PATTERN_2 = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$") // 01/31/2024
  private val DATE_PATTERN_3 = Pattern.compile("^\\d{4}/\\d{2}/\\d{2}$") // 2024/01/31

  /**
   * Auto-detect the most likely delimiter from sample text.
   * Checks comma, semicolon, tab, and pipe.
   */
  fun detectDelimiter(sample: String): Char {
    val lines = sample.lines().filter { it.isNotBlank() }.take(10)
    if (lines.isEmpty()) return ','

    val candidateDelimiters = listOf(',', ';', '\t', '|')
    var bestDelimiter = ','
    var maxScore = -1.0

    for (candidate in candidateDelimiters) {
      val counts = lines.map { countDelimiterOutsideQuotes(it, candidate) }
      val firstCount = counts.firstOrNull() ?: 0
      if (firstCount > 0) {
        val allSame = counts.all { it == firstCount }
        val consistencyScore = if (allSame) 2.0 else 1.0
        val totalCount = counts.sum()
        val score = totalCount * consistencyScore
        if (score > maxScore) {
          maxScore = score
          bestDelimiter = candidate
        }
      }
    }

    return bestDelimiter
  }

  private fun countDelimiterOutsideQuotes(line: String, delimiter: Char): Int {
    var count = 0
    var inQuotes = false
    var i = 0
    while (i < line.length) {
      val c = line[i]
      if (c == '"') {
        if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
          i++ // skip escaped quote
        } else {
          inQuotes = !inQuotes
        }
      } else if (c == delimiter && !inQuotes) {
        count++
      }
      i++
    }
    return count
  }

  /**
   * Parse CSV stream or string using RFC 4180 standard rules.
   */
  fun parse(
    content: String,
    fileName: String = "data.csv",
    customDelimiter: Char? = null,
    hasHeaders: Boolean = true,
    trimValues: Boolean = true
  ): CsvParseResult {
    val delimiter = customDelimiter ?: detectDelimiter(content)
    val records = mutableListOf<List<String>>()
    val currentRecord = mutableListOf<String>()
    val currentField = java.lang.StringBuilder()
    var inQuotes = false
    var i = 0
    val len = content.length

    while (i < len) {
      val c = content[i]

      when {
        c == '"' -> {
          if (inQuotes && i + 1 < len && content[i + 1] == '"') {
            // Escaped quote: "" -> "
            currentField.append('"')
            i++
          } else {
            inQuotes = !inQuotes
          }
        }
        c == delimiter && !inQuotes -> {
          var field = currentField.toString()
          if (trimValues) field = field.trim()
          currentRecord.add(field)
          currentField.setLength(0)
        }
        (c == '\r' || c == '\n') && !inQuotes -> {
          if (c == '\r' && i + 1 < len && content[i + 1] == '\n') {
            i++
          }
          var field = currentField.toString()
          if (trimValues) field = field.trim()
          currentRecord.add(field)
          currentField.setLength(0)

          // Only add non-empty record unless it's an intended empty line
          if (currentRecord.size > 1 || currentRecord.firstOrNull()?.isNotEmpty() == true) {
            records.add(currentRecord.toList())
          }
          currentRecord.clear()
        }
        else -> {
          currentField.append(c)
        }
      }
      i++
    }

    // Flush last field if any remaining
    if (currentField.isNotEmpty() || inQuotes || currentRecord.isNotEmpty()) {
      var field = currentField.toString()
      if (trimValues) field = field.trim()
      currentRecord.add(field)
      if (currentRecord.size > 1 || currentRecord.firstOrNull()?.isNotEmpty() == true) {
        records.add(currentRecord.toList())
      }
    }

    if (records.isEmpty()) {
      return CsvParseResult(
        fileName = fileName,
        delimiter = delimiter,
        hasHeaders = hasHeaders,
        headers = emptyList(),
        rows = emptyList(),
        totalRows = 0,
        totalColumns = 0,
        columnTypes = emptyList(),
        rawTextPreview = content.take(500)
      )
    }

    val maxCols = records.maxOfOrNull { it.size } ?: 0

    // Normalize all rows to maxCols
    val normalizedRecords = records.map { row ->
      if (row.size < maxCols) {
        row + List(maxCols - row.size) { "" }
      } else {
        row
      }
    }

    val headers: List<String>
    val dataRows: List<List<String>>

    if (hasHeaders && normalizedRecords.isNotEmpty()) {
      headers = normalizedRecords.first().mapIndexed { index, name ->
        if (name.isBlank()) "Column ${index + 1}" else name
      }
      dataRows = normalizedRecords.drop(1)
    } else {
      headers = List(maxCols) { "Column ${it + 1}" }
      dataRows = normalizedRecords
    }

    val columnTypes = inferColumnTypes(dataRows, maxCols)

    return CsvParseResult(
      fileName = fileName,
      delimiter = delimiter,
      hasHeaders = hasHeaders,
      headers = headers,
      rows = dataRows,
      totalRows = dataRows.size,
      totalColumns = maxCols,
      columnTypes = columnTypes,
      rawTextPreview = content.take(500),
      fileSizeFormatted = formatBytes(content.toByteArray(StandardCharsets.UTF_8).size.toLong())
    )
  }

  fun readStream(inputStream: InputStream): String {
    val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
    return reader.use { it.readText() }
  }

  private fun inferColumnTypes(rows: List<List<String>>, columnCount: Int): List<ColumnType> {
    if (rows.isEmpty() || columnCount == 0) {
      return List(columnCount) { ColumnType.TEXT }
    }

    return (0 until columnCount).map { colIndex ->
      val nonBlankValues = rows.mapNotNull { row ->
        row.getOrNull(colIndex)?.trim()?.takeIf { it.isNotEmpty() }
      }.take(50) // sample first 50 rows for inference

      if (nonBlankValues.isEmpty()) {
        ColumnType.TEXT
      } else if (nonBlankValues.all { isNumber(it) }) {
        ColumnType.NUMBER
      } else if (nonBlankValues.all { isBoolean(it) }) {
        ColumnType.BOOLEAN
      } else if (nonBlankValues.all { isDate(it) }) {
        ColumnType.DATE
      } else {
        ColumnType.TEXT
      }
    }
  }

  fun isNumber(value: String): Boolean {
    // Standard integer or float, may contain optional sign or thousand commas
    val clean = value.replace(",", "").trim()
    return clean.toDoubleOrNull() != null
  }

  fun isBoolean(value: String): Boolean {
    val lower = value.lowercase().trim()
    return lower == "true" || lower == "false" || lower == "yes" || lower == "no"
  }

  fun isDate(value: String): Boolean {
    val trimmed = value.trim()
    return DATE_PATTERN_1.matcher(trimmed).matches() ||
      DATE_PATTERN_2.matcher(trimmed).matches() ||
      DATE_PATTERN_3.matcher(trimmed).matches()
  }

  fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format(java.util.Locale.US, "%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
  }
}
