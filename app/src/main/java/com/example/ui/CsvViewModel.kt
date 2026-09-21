package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.db.AppDatabase
import com.example.db.ConversionRecord
import com.example.db.ConversionRepository
import com.example.generator.ExcelGenerator
import com.example.model.CsvParseResult
import com.example.parser.CsvParser
import com.example.samples.SampleCsv
import com.example.samples.SampleDatasets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class ConversionSuccess(
  val fileName: String,
  val rowCount: Int,
  val columnCount: Int,
  val fileSizeBytes: Long,
  val localFile: File? = null
)

class CsvViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: ConversionRepository

  init {
    val database = AppDatabase.getDatabase(application)
    repository = ConversionRepository(database.conversionDao())
  }

  val historyRecords: StateFlow<List<ConversionRecord>> = repository.allRecords
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  private val _parseResult = MutableStateFlow<CsvParseResult?>(null)
  val parseResult: StateFlow<CsvParseResult?> = _parseResult.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _isConverting = MutableStateFlow(false)
  val isConverting: StateFlow<Boolean> = _isConverting.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _conversionSuccess = MutableSharedFlow<ConversionSuccess>()
  val conversionSuccess: SharedFlow<ConversionSuccess> = _conversionSuccess.asSharedFlow()

  private val _customSheetName = MutableStateFlow("Data")
  val customSheetName: StateFlow<String> = _customSheetName.asStateFlow()

  private val _customFileName = MutableStateFlow("")
  val customFileName: StateFlow<String> = _customFileName.asStateFlow()

  private val _hasHeaders = MutableStateFlow(true)
  val hasHeaders: StateFlow<Boolean> = _hasHeaders.asStateFlow()

  private val _selectedDelimiter = MutableStateFlow<Char?>(null) // null = auto
  val selectedDelimiter: StateFlow<Char?> = _selectedDelimiter.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private var rawCachedContent: String = ""
  private var currentSourceType: String = "sample"

  init {
    // Automatically load the first sample on first launch so user sees the app populated and ready!
    loadSample(SampleDatasets.SAMPLES.first())
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setSheetName(name: String) {
    _customSheetName.value = name
  }

  fun setOutputFileName(name: String) {
    _customFileName.value = name
  }

  fun toggleHasHeaders(enabled: Boolean) {
    _hasHeaders.value = enabled
    reparseCurrentContent()
  }

  fun selectDelimiter(delim: Char?) {
    _selectedDelimiter.value = delim
    reparseCurrentContent()
  }

  fun clearError() {
    _errorMessage.value = null
  }

  fun loadSample(sample: SampleCsv) {
    viewModelScope.launch {
      _isLoading.value = true
      currentSourceType = "sample"
      rawCachedContent = sample.csvContent
      _selectedDelimiter.value = sample.defaultDelimiter
      _hasHeaders.value = true

      val baseName = sample.fileName.removeSuffix(".csv")
      _customFileName.value = "${baseName}.xlsx"
      _customSheetName.value = sample.title.take(20).replace(" ", "_")

      parseContentInternal(
        content = sample.csvContent,
        fileName = sample.fileName,
        delimiter = sample.defaultDelimiter,
        hasHeaders = true
      )
      _isLoading.value = false
    }
  }

  fun loadCsvFromText(text: String, customName: String = "pasted_data.csv") {
    if (text.isBlank()) {
      _errorMessage.value = "CSV text is empty. Please enter or paste CSV content."
      return
    }
    viewModelScope.launch {
      _isLoading.value = true
      currentSourceType = "pasted"
      rawCachedContent = text

      val baseName = customName.removeSuffix(".csv").ifBlank { "converted_data" }
      _customFileName.value = "${baseName}.xlsx"
      _customSheetName.value = "Data"

      parseContentInternal(
        content = text,
        fileName = customName,
        delimiter = _selectedDelimiter.value,
        hasHeaders = _hasHeaders.value
      )
      _isLoading.value = false
    }
  }

  fun loadCsvFromUri(context: Context, uri: Uri) {
    viewModelScope.launch {
      _isLoading.value = true
      currentSourceType = "file"
      try {
        val fileName = getFileName(context, uri) ?: "imported_data.csv"
        val content = withContext(Dispatchers.IO) {
          context.contentResolver.openInputStream(uri)?.use { stream ->
            CsvParser.readStream(stream)
          } ?: ""
        }

        if (content.isBlank()) {
          _errorMessage.value = "Selected file is empty."
          _isLoading.value = false
          return@launch
        }

        rawCachedContent = content
        val baseName = fileName.substringBeforeLast(".")
        _customFileName.value = "${baseName}.xlsx"
        _customSheetName.value = "Data"

        parseContentInternal(
          content = content,
          fileName = fileName,
          delimiter = _selectedDelimiter.value,
          hasHeaders = _hasHeaders.value
        )
      } catch (e: Exception) {
        _errorMessage.value = "Error reading file: ${e.localizedMessage ?: "Unknown error"}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  private fun reparseCurrentContent() {
    if (rawCachedContent.isNotBlank()) {
      val currentName = _parseResult.value?.fileName ?: "data.csv"
      parseContentInternal(
        content = rawCachedContent,
        fileName = currentName,
        delimiter = _selectedDelimiter.value,
        hasHeaders = _hasHeaders.value
      )
    }
  }

  private fun parseContentInternal(
    content: String,
    fileName: String,
    delimiter: Char?,
    hasHeaders: Boolean
  ) {
    try {
      val result = CsvParser.parse(
        content = content,
        fileName = fileName,
        customDelimiter = delimiter,
        hasHeaders = hasHeaders
      )
      _parseResult.value = result
    } catch (e: Exception) {
      _errorMessage.value = "Failed to parse CSV: ${e.localizedMessage}"
    }
  }

  fun getProposedOutputFileName(): String {
    val input = _customFileName.value.trim()
    return if (input.endsWith(".xlsx", ignoreCase = true)) {
      input
    } else if (input.isNotBlank()) {
      "$input.xlsx"
    } else {
      val base = _parseResult.value?.fileName?.substringBeforeLast(".") ?: "converted"
      "$base.xlsx"
    }
  }

  /**
   * Generates Excel (.xlsx) file and writes directly into user selected file via CreateDocument Uri
   */
  fun convertAndSaveToUri(context: Context, destinationUri: Uri) {
    val result = _parseResult.value ?: return
    viewModelScope.launch {
      _isConverting.value = true
      try {
        val outFileName = getProposedOutputFileName()
        val sheetName = _customSheetName.value.ifBlank { "Data" }

        var writtenBytes = 0L
        withContext(Dispatchers.IO) {
          context.contentResolver.openOutputStream(destinationUri)?.use { os ->
            // Use Counting OutputStream or temp buffer
            val tempFile = File(context.cacheDir, "temp_export.xlsx")
            ExcelGenerator.generateXlsxToFile(
              parseResult = result,
              outputFile = tempFile,
              sheetName = sheetName,
              includeHeaders = _hasHeaders.value,
              applyStyling = true
            )
            writtenBytes = tempFile.length()
            tempFile.inputStream().use { input ->
              input.copyTo(os)
            }
            tempFile.delete()
          }
        }

        // Cache persistent copy for history sharing
        val exportsDir = File(context.filesDir, "exports").apply { mkdirs() }
        val savedCopy = File(exportsDir, "${System.currentTimeMillis()}_$outFileName")
        withContext(Dispatchers.IO) {
          ExcelGenerator.generateXlsxToFile(
            parseResult = result,
            outputFile = savedCopy,
            sheetName = sheetName,
            includeHeaders = _hasHeaders.value,
            applyStyling = true
          )
        }

        repository.insertRecord(
          ConversionRecord(
            fileName = outFileName,
            sourceName = result.fileName,
            rowCount = result.totalRows,
            columnCount = result.totalColumns,
            fileSizeBytes = writtenBytes,
            delimiterUsed = result.delimiter.toString(),
            localFilePath = savedCopy.absolutePath
          )
        )

        _conversionSuccess.emit(
          ConversionSuccess(
            fileName = outFileName,
            rowCount = result.totalRows,
            columnCount = result.totalColumns,
            fileSizeBytes = writtenBytes,
            localFile = savedCopy
          )
        )
      } catch (e: Exception) {
        _errorMessage.value = "Failed to export Excel file: ${e.localizedMessage ?: "Unknown error"}"
      } finally {
        _isConverting.value = false
      }
    }
  }

  /**
   * Generates Excel file in internal storage and initiates Android Share Sheet
   */
  fun convertAndShare(context: Context) {
    val result = _parseResult.value ?: return
    viewModelScope.launch {
      _isConverting.value = true
      try {
        val outFileName = getProposedOutputFileName()
        val sheetName = _customSheetName.value.ifBlank { "Data" }

        val exportsDir = File(context.filesDir, "exports").apply { mkdirs() }
        val outputFile = File(exportsDir, "${System.currentTimeMillis()}_$outFileName")

        withContext(Dispatchers.IO) {
          ExcelGenerator.generateXlsxToFile(
            parseResult = result,
            outputFile = outputFile,
            sheetName = sheetName,
            includeHeaders = _hasHeaders.value,
            applyStyling = true
          )
        }

        repository.insertRecord(
          ConversionRecord(
            fileName = outFileName,
            sourceName = result.fileName,
            rowCount = result.totalRows,
            columnCount = result.totalColumns,
            fileSizeBytes = outputFile.length(),
            delimiterUsed = result.delimiter.toString(),
            localFilePath = outputFile.absolutePath
          )
        )

        shareFile(context, outputFile, outFileName)

        _conversionSuccess.emit(
          ConversionSuccess(
            fileName = outFileName,
            rowCount = result.totalRows,
            columnCount = result.totalColumns,
            fileSizeBytes = outputFile.length(),
            localFile = outputFile
          )
        )
      } catch (e: Exception) {
        _errorMessage.value = "Failed to share Excel file: ${e.localizedMessage ?: "Unknown error"}"
      } finally {
        _isConverting.value = false
      }
    }
  }

  fun shareFile(context: Context, file: File, displayName: String) {
    try {
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )

      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, displayName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = Intent.createChooser(intent, "Share $displayName")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      _errorMessage.value = "Could not open share menu: ${e.localizedMessage}"
    }
  }

  fun openFile(context: Context, file: File) {
    try {
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )

      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(Intent.createChooser(intent, "Open with..."))
    } catch (e: Exception) {
      _errorMessage.value = "No compatible spreadsheet app installed to preview directly."
    }
  }

  fun deleteHistoryRecord(id: Long, localPath: String?) {
    viewModelScope.launch {
      localPath?.let {
        try {
          File(it).delete()
        } catch (_: Exception) {}
      }
      repository.deleteRecord(id)
    }
  }

  fun clearAllHistory() {
    viewModelScope.launch {
      repository.clearHistory()
    }
  }

  private fun getFileName(context: Context, uri: Uri): String? {
    if (uri.scheme == "content") {
      context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1) {
            return cursor.getString(nameIndex)
          }
        }
      }
    }
    return uri.lastPathSegment
  }
}
