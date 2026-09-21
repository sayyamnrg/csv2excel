package com.example.generator

import com.example.model.ColumnType
import com.example.model.CsvParseResult
import com.example.parser.CsvParser
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExcelGenerator {

  /**
   * Generates a modern Microsoft Excel OpenXML (.xlsx) file.
   */
  fun generateXlsx(
    parseResult: CsvParseResult,
    outputStream: OutputStream,
    sheetName: String = "Sheet1",
    includeHeaders: Boolean = true,
    applyStyling: Boolean = true
  ) {
    val zos = ZipOutputStream(outputStream)

    try {
      // 1. [Content_Types].xml
      addZipEntry(zos, "[Content_Types].xml", buildContentTypesXml())

      // 2. _rels/.rels
      addZipEntry(zos, "_rels/.rels", buildRootRelsXml())

      // 3. xl/_rels/workbook.xml.rels
      addZipEntry(zos, "xl/_rels/workbook.xml.rels", buildWorkbookRelsXml())

      // 4. xl/workbook.xml
      addZipEntry(zos, "xl/workbook.xml", buildWorkbookXml(sanitizeSheetName(sheetName)))

      // 5. xl/styles.xml
      addZipEntry(zos, "xl/styles.xml", buildStylesXml())

      // 6. xl/worksheets/sheet1.xml
      val worksheetXml = buildWorksheetXml(parseResult, includeHeaders, applyStyling)
      addZipEntry(zos, "xl/worksheets/sheet1.xml", worksheetXml)

      zos.finish()
    } finally {
      zos.close()
    }
  }

  fun generateXlsxToFile(
    parseResult: CsvParseResult,
    outputFile: File,
    sheetName: String = "Data",
    includeHeaders: Boolean = true,
    applyStyling: Boolean = true
  ): File {
    FileOutputStream(outputFile).use { fos ->
      generateXlsx(parseResult, fos, sheetName, includeHeaders, applyStyling)
    }
    return outputFile
  }

  private fun addZipEntry(zos: ZipOutputStream, entryName: String, content: String) {
    val entry = ZipEntry(entryName)
    zos.putNextEntry(entry)
    zos.write(content.toByteArray(StandardCharsets.UTF_8))
    zos.closeEntry()
  }

  private fun buildContentTypesXml(): String = """
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
      <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
      <Default Extension="xml" ContentType="application/xml"/>
      <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
      <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
      <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
    </Types>
  """.trimIndent()

  private fun buildRootRelsXml(): String = """
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
    </Relationships>
  """.trimIndent()

  private fun buildWorkbookRelsXml(): String = """
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
      <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
    </Relationships>
  """.trimIndent()

  private fun buildWorkbookXml(sheetName: String): String = """
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
      <bookViews>
        <workbookView xWindow="0" yWindow="0" windowWidth="24000" windowHeight="12000"/>
      </bookViews>
      <sheets>
        <sheet name="${escapeXml(sheetName)}" sheetId="1" r:id="rId1"/>
      </sheets>
    </workbook>
  """.trimIndent()

  private fun buildStylesXml(): String = """
    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
      <fonts count="2">
        <font>
          <sz val="11"/>
          <color theme="1"/>
          <name val="Calibri"/>
          <family val="2"/>
        </font>
        <font>
          <b/>
          <sz val="11"/>
          <color rgb="FFFFFFFF"/>
          <name val="Calibri"/>
          <family val="2"/>
        </font>
      </fonts>
      <fills count="4">
        <fill><patternFill fillType="none"/></fill>
        <fill><patternFill fillType="gray125"/></fill>
        <fill>
          <patternFill fillType="solid">
            <fgColor rgb="FF137333"/>
          </patternFill>
        </fill>
        <fill>
          <patternFill fillType="solid">
            <fgColor rgb="FFF1F8F3"/>
          </patternFill>
        </fill>
      </fills>
      <borders count="2">
        <border>
          <left/><right/><top/><bottom/><diagonal/>
        </border>
        <border>
          <left style="thin"><color rgb="FFD4DDD6"/></left>
          <right style="thin"><color rgb="FFD4DDD6"/></right>
          <top style="thin"><color rgb="FFD4DDD6"/></top>
          <bottom style="thin"><color rgb="FFD4DDD6"/></bottom>
          <diagonal/>
        </border>
      </borders>
      <cellStyleXfs count="1">
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
      </cellStyleXfs>
      <cellXfs count="4">
        <!-- 0: Standard normal text -->
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
        <!-- 1: Styled Header (Bold, White font, Green background, border, center-left align) -->
        <xf numFmtId="0" fontId="1" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
          <alignment vertical="center" wrapText="1"/>
        </xf>
        <!-- 2: Regular bordered data cell -->
        <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1" applyAlignment="1">
          <alignment vertical="center"/>
        </xf>
        <!-- 3: Alternating row subtle zebra cell -->
        <xf numFmtId="0" fontId="0" fillId="3" borderId="1" xfId="0" applyFill="1" applyBorder="1" applyAlignment="1">
          <alignment vertical="center"/>
        </xf>
      </cellXfs>
    </styleSheet>
  """.trimIndent()

  private fun buildWorksheetXml(
    data: CsvParseResult,
    includeHeaders: Boolean,
    applyStyling: Boolean
  ): String {
    val totalCols = if (data.headers.isNotEmpty()) data.headers.size else data.totalColumns
    val colWidths = IntArray(totalCols) { 10 }

    // Measure maximum string length for auto column widths
    if (includeHeaders) {
      data.headers.forEachIndexed { idx, header ->
        if (idx < colWidths.size) {
          colWidths[idx] = maxOf(colWidths[idx], header.length + 4)
        }
      }
    }

    data.rows.take(200).forEach { row ->
      row.forEachIndexed { idx, cell ->
        if (idx < colWidths.size) {
          colWidths[idx] = maxOf(colWidths[idx], minOf(cell.length + 3, 50))
        }
      }
    }

    val sb = StringBuilder()
    sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
    sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")

    // Column widths
    if (totalCols > 0) {
      sb.append("<cols>")
      for (i in 0 until totalCols) {
        val width = colWidths[i].coerceIn(11, 45)
        sb.append("""<col min="${i + 1}" max="${i + 1}" width="$width" customWidth="1"/>""")
      }
      sb.append("</cols>")
    }

    sb.append("<sheetData>")

    var excelRowIndex = 1

    // Header row
    if (includeHeaders && data.headers.isNotEmpty()) {
      sb.append("""<row r="$excelRowIndex" ht="26" customHeight="1">""")
      data.headers.forEachIndexed { colIndex, headerText ->
        val cellRef = "${getColumnName(colIndex)}$excelRowIndex"
        val styleId = if (applyStyling) "1" else "0"
        sb.append("""<c r="$cellRef" s="$styleId" t="inlineStr"><is><t>${escapeXml(headerText)}</t></is></c>""")
      }
      sb.append("</row>")
      excelRowIndex++
    }

    // Data rows
    data.rows.forEachIndexed { rowIndex, row ->
      val isAlt = (rowIndex % 2 == 1)
      val defaultStyleId = when {
        !applyStyling -> "0"
        isAlt -> "3"
        else -> "2"
      }

      sb.append("""<row r="$excelRowIndex" ht="20">""")
      row.forEachIndexed { colIndex, rawVal ->
        val cellRef = "${getColumnName(colIndex)}$excelRowIndex"
        val trimmed = rawVal.trim()

        if (trimmed.isEmpty()) {
          sb.append("""<c r="$cellRef" s="$defaultStyleId"/>""")
        } else if (data.columnTypes.getOrNull(colIndex) == ColumnType.NUMBER && CsvParser.isNumber(trimmed)) {
          val numVal = trimmed.replace(",", "")
          sb.append("""<c r="$cellRef" s="$defaultStyleId"><v>$numVal</v></c>""")
        } else if (data.columnTypes.getOrNull(colIndex) == ColumnType.BOOLEAN && CsvParser.isBoolean(trimmed)) {
          val boolVal = if (trimmed.lowercase() == "true" || trimmed.lowercase() == "yes") "1" else "0"
          sb.append("""<c r="$cellRef" s="$defaultStyleId" t="b"><v>$boolVal</v></c>""")
        } else {
          sb.append("""<c r="$cellRef" s="$defaultStyleId" t="inlineStr"><is><t>${escapeXml(rawVal)}</t></is></c>""")
        }
      }
      sb.append("</row>")
      excelRowIndex++
    }

    sb.append("</sheetData>")
    sb.append("</worksheet>")

    return sb.toString()
  }

  fun getColumnName(colIndex: Int): String {
    var num = colIndex
    val sb = StringBuilder()
    while (num >= 0) {
      sb.insert(0, ('A' + (num % 26)))
      num = (num / 26) - 1
    }
    return sb.toString()
  }

  private fun sanitizeSheetName(name: String): String {
    val clean = name.replace(Regex("[:\\\\/?*\\[\\]]"), " ").trim()
    return if (clean.isBlank()) "Sheet1" else clean.take(31)
  }

  private fun escapeXml(text: String): String {
    val sb = StringBuilder(text.length + 16)
    for (c in text) {
      when (c) {
        '&' -> sb.append("&amp;")
        '<' -> sb.append("&lt;")
        '>' -> sb.append("&gt;")
        '"' -> sb.append("&quot;")
        '\'' -> sb.append("&apos;")
        else -> {
          // Filter out invalid XML characters
          if (c.code in 0x20..0xD7FF || c == '\t' || c == '\n' || c == '\r' || c.code in 0xE000..0xFFFD) {
            sb.append(c)
          }
        }
      }
    }
    return sb.toString()
  }
}
