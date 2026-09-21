package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.ConversionSuccessDialog
import com.example.ui.components.PasteCsvDialog
import com.example.ui.screens.ConvertScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SamplesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvToExcelApp(
  viewModel: CsvViewModel = viewModel()
) {
  val context = LocalContext.current
  var selectedTab by rememberSaveable { mutableIntStateOf(0) }
  var showPasteDialog by remember { mutableStateOf(false) }
  var showInfoDialog by remember { mutableStateOf(false) }
  var conversionSuccessInfo by remember { mutableStateOf<ConversionSuccess?>(null) }

  val snackbarHostState = remember { SnackbarHostState() }
  val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

  // Collect conversion success events
  LaunchedEffect(Unit) {
    viewModel.conversionSuccess.collect { success ->
      conversionSuccessInfo = success
    }
  }

  // Show snackbar on errors
  LaunchedEffect(errorMessage) {
    errorMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearError()
    }
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
              painter = painterResource(id = R.drawable.img_app_icon),
              contentDescription = "App Icon",
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "CSV to Excel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Converter & Spreadsheet Viewer",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = { showInfoDialog = true },
            modifier = Modifier.testTag("app_info_button")
          ) {
            Icon(
              Icons.Default.Info,
              contentDescription = "About",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        NavigationBarItem(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          icon = {
            Icon(
              if (selectedTab == 0) Icons.Filled.TableChart else Icons.Outlined.TableChart,
              contentDescription = "Convert"
            )
          },
          label = { Text("Convert") },
          modifier = Modifier.testTag("nav_tab_convert")
        )

        NavigationBarItem(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          icon = {
            Icon(
              if (selectedTab == 1) Icons.Filled.History else Icons.Outlined.History,
              contentDescription = "History"
            )
          },
          label = { Text("History") },
          modifier = Modifier.testTag("nav_tab_history")
        )

        NavigationBarItem(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          icon = {
            Icon(
              if (selectedTab == 2) Icons.Filled.Dataset else Icons.Outlined.Dataset,
              contentDescription = "Samples"
            )
          },
          label = { Text("Samples") },
          modifier = Modifier.testTag("nav_tab_samples")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (selectedTab) {
        0 -> ConvertScreen(
          viewModel = viewModel,
          onOpenPasteDialog = { showPasteDialog = true },
          onNavigateToSamples = { selectedTab = 2 }
        )
        1 -> HistoryScreen(
          viewModel = viewModel
        )
        2 -> SamplesScreen(
          onSelectSample = { sample ->
            viewModel.loadSample(sample)
            selectedTab = 0
          }
        )
      }
    }
  }

  // Paste CSV Dialog
  if (showPasteDialog) {
    PasteCsvDialog(
      onDismiss = { showPasteDialog = false },
      onConfirm = { text, fileName ->
        viewModel.loadCsvFromText(text, fileName)
        showPasteDialog = false
      }
    )
  }

  // Success Confirmation Dialog
  conversionSuccessInfo?.let { success ->
    ConversionSuccessDialog(
      success = success,
      onDismiss = { conversionSuccessInfo = null },
      onShare = {
        conversionSuccessInfo = null
        success.localFile?.let { file ->
          viewModel.shareFile(context, file, success.fileName)
        }
      },
      onOpen = {
        conversionSuccessInfo = null
        success.localFile?.let { file ->
          viewModel.openFile(context, file)
        }
      }
    )
  }

  // Information & Help Dialog
  if (showInfoDialog) {
    AlertDialog(
      onDismissRequest = { showInfoDialog = false },
      title = {
        Text(
          text = "CSV to Excel Converter",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column {
          Text(
            text = "Features & Specifications:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "• RFC-4180 Compliant: Handles quoted fields, embedded line breaks, and escaped double quotes seamlessly.\n" +
              "• OpenXML (.xlsx): Exports genuine modern Excel files compatible with Microsoft 365, Google Sheets, LibreOffice, and Apple Numbers.\n" +
              "• Auto Data Typing: Automatically detects numbers, dates, booleans, and text strings for mathematical formulas.\n" +
              "• Delimiter Flexibility: Auto-detects or switches between comma, semicolon, tab, and pipe delimiters.\n" +
              "• Pure Android: Zero bulky external dependencies for blazing-fast conversions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { showInfoDialog = false }) {
          Text("Got it")
        }
      }
    )
  }
}
