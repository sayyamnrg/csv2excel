package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.CsvParseResult
import com.example.ui.CsvViewModel
import com.example.ui.components.DataGridTable

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConvertScreen(
  viewModel: CsvViewModel,
  onOpenPasteDialog: () -> Unit,
  onNavigateToSamples: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val parseResult by viewModel.parseResult.collectAsStateWithLifecycle()
  val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
  val isConverting by viewModel.isConverting.collectAsStateWithLifecycle()
  val customSheetName by viewModel.customSheetName.collectAsStateWithLifecycle()
  val customFileName by viewModel.customFileName.collectAsStateWithLifecycle()
  val hasHeaders by viewModel.hasHeaders.collectAsStateWithLifecycle()
  val selectedDelimiter by viewModel.selectedDelimiter.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

  // File Picker Launcher for CSV
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let { viewModel.loadCsvFromUri(context, it) }
  }

  // Create Document Launcher for saving .xlsx
  val saveFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
  ) { uri: Uri? ->
    uri?.let { viewModel.convertAndSaveToUri(context, it) }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Hero Card
    item {
      Spacer(modifier = Modifier.height(4.dp))
      ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(130.dp)
              .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_csv_hero),
              contentDescription = "Spreadsheet Banner",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.25f))
            )
          }

          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(2.dp)
              ) {
                Text(
                  text = "RFC-4180 COMPLIANT",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  ),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
              Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(2.dp)
              ) {
                Text(
                  text = "EXCEL (.XLSX)",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  ),
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "Convert CSV Data to Excel",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Transform any delimiter-separated file into a beautifully formatted Microsoft Excel workbook with typed columns, auto-width headers, and alternating zebra rows.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Import Options
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier
                  .weight(1.3f)
                  .testTag("choose_file_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary
                )
              ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open CSV", maxLines = 1)
              }

              OutlinedButton(
                onClick = onOpenPasteDialog,
                modifier = Modifier
                  .weight(1f)
                  .testTag("paste_csv_button")
              ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Paste", maxLines = 1)
              }

              OutlinedButton(
                onClick = onNavigateToSamples,
                modifier = Modifier
                  .weight(1f)
                  .testTag("samples_button")
              ) {
                Icon(Icons.Default.Dataset, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Samples", maxLines = 1)
              }
            }
          }
        }
      }
    }

    // If parsing or loading
    if (isLoading) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
      }
    }

    // Config & Conversion Options Card
    parseResult?.let { result ->
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  Icons.Default.TableChart,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "File & Formatting Settings",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "${result.totalRows} rows • ${result.totalColumns} cols",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Delimiter chips
            Text(
              text = "Delimiter",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              val isAuto = selectedDelimiter == null
              FilterChip(
                selected = isAuto,
                onClick = { viewModel.selectDelimiter(null) },
                label = { Text("Auto (${getDelimiterName(result.delimiter)})") },
                leadingIcon = if (isAuto) {
                  { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("delimiter_auto_chip")
              )

              listOf(
                ',' to "Comma (,)",
                ';' to "Semicolon (;)",
                '\t' to "Tab (\\t)",
                '|' to "Pipe (|)"
              ).forEach { (char, label) ->
                val isSelected = selectedDelimiter == char
                FilterChip(
                  selected = isSelected,
                  onClick = { viewModel.selectDelimiter(char) },
                  label = { Text(label) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                  ),
                  modifier = Modifier.testTag("delimiter_${char}_chip")
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Header toggle switch
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "First row contains headers",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "Formats the top row with solid emerald styling",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Switch(
                checked = hasHeaders,
                onCheckedChange = { viewModel.toggleHasHeaders(it) },
                modifier = Modifier.testTag("has_headers_switch")
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Output filename and sheet name
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = customFileName,
                onValueChange = { viewModel.setOutputFileName(it) },
                label = { Text("Excel File Name") },
                singleLine = true,
                modifier = Modifier
                  .weight(1.3f)
                  .testTag("output_filename_field"),
                shape = RoundedCornerShape(10.dp)
              )

              OutlinedTextField(
                value = customSheetName,
                onValueChange = { viewModel.setSheetName(it) },
                label = { Text("Sheet Name") },
                singleLine = true,
                modifier = Modifier
                  .weight(0.9f)
                  .testTag("sheet_name_field"),
                shape = RoundedCornerShape(10.dp)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Conversion Actions
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  val proposed = viewModel.getProposedOutputFileName()
                  saveFileLauncher.launch(proposed)
                },
                enabled = !isConverting,
                modifier = Modifier
                  .weight(1f)
                  .height(52.dp)
                  .testTag("save_excel_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
              ) {
                if (isConverting) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                  )
                } else {
                  Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Save .xlsx", fontWeight = FontWeight.SemiBold)
                }
              }

              OutlinedButton(
                onClick = { viewModel.convertAndShare(context) },
                enabled = !isConverting,
                modifier = Modifier
                  .weight(1f)
                  .height(52.dp)
                  .testTag("share_excel_button"),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share .xlsx", fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }

      // Data Preview Section Header & Search
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Live Data Preview",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Showing structured rows and detected column data types",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = { Text("Filter rows by keyword...") },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
          },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              IconButton(onClick = { viewModel.setSearchQuery("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
              }
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("table_search_input"),
          shape = RoundedCornerShape(10.dp)
        )
      }

      // Interactive Data Grid Preview
      item {
        DataGridTable(
          parseResult = result,
          searchQuery = searchQuery,
          modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .testTag("data_grid_table")
        )
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

private fun getDelimiterName(delimiter: Char): String {
  return when (delimiter) {
    ',' -> "Comma"
    ';' -> "Semicolon"
    '\t' -> "Tab"
    '|' -> "Pipe"
    else -> "'$delimiter'"
  }
}
