package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColumnType
import com.example.model.CsvParseResult

@Composable
fun DataGridTable(
  parseResult: CsvParseResult,
  searchQuery: String = "",
  modifier: Modifier = Modifier
) {
  val horizontalScrollState = rememberScrollState()

  val filteredRows = if (searchQuery.isBlank()) {
    parseResult.rows
  } else {
    parseResult.rows.filter { row ->
      row.any { cell -> cell.contains(searchQuery, ignoreCase = true) }
    }
  }

  val columnCount = if (parseResult.headers.isNotEmpty()) {
    parseResult.headers.size
  } else {
    parseResult.totalColumns
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
    shape = RoundedCornerShape(12.dp),
    tonalElevation = 1.dp
  ) {
    if (columnCount == 0 || (parseResult.rows.isEmpty() && parseResult.headers.isEmpty())) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "No data to display in table",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      Column(modifier = Modifier.fillMaxSize()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState)
        ) {
          Column {
            // Header Row
            Row(
              modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .testTag("table_header_row"),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Row Index Header
              Box(
                modifier = Modifier
                  .width(48.dp)
                  .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "#",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimary
                )
              }

              // Column Headers
              parseResult.headers.forEachIndexed { colIdx, headerText ->
                val colType = parseResult.columnTypes.getOrNull(colIdx) ?: ColumnType.TEXT
                Column(
                  modifier = Modifier
                    .widthIn(min = 130.dp, max = 220.dp)
                    .border(0.5.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                  Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = colType.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.primaryContainer
                  )
                }
              }
            }

            // Data Rows in LazyColumn
            LazyColumn(modifier = Modifier.fillMaxSize()) {
              itemsIndexed(filteredRows) { rowIndex, rowData ->
                val isEven = rowIndex % 2 == 0
                val rowBackground = if (isEven) {
                  MaterialTheme.colorScheme.surface
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                }

                Row(
                  modifier = Modifier
                    .background(rowBackground)
                    .testTag("table_row_$rowIndex"),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Row Index Cell
                  Box(
                    modifier = Modifier
                      .width(48.dp)
                      .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                      .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = "${rowIndex + 1}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                      fontFamily = FontFamily.Monospace
                    )
                  }

                  // Data Cells
                  for (colIdx in 0 until columnCount) {
                    val cellText = rowData.getOrNull(colIdx) ?: ""
                    val colType = parseResult.columnTypes.getOrNull(colIdx) ?: ColumnType.TEXT
                    val isNum = colType == ColumnType.NUMBER

                    Box(
                      modifier = Modifier
                        .widthIn(min = 130.dp, max = 220.dp)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                      Text(
                        text = cellText.ifEmpty { "—" },
                        style = MaterialTheme.typography.bodySmall.copy(
                          fontFamily = if (isNum) FontFamily.Monospace else FontFamily.Default
                        ),
                        color = if (cellText.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = if (isNum) TextAlign.End else TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
