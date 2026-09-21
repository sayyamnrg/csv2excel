package com.example

import com.example.generator.ExcelGenerator
import com.example.model.ColumnType
import com.example.parser.CsvParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class CsvExcelUnitTest {

  @Test
  fun testCsvParserStandard() {
    val csv = """
      Name,Age,Score
      Alice,25,92.5
      Bob,30,88.0
    """.trimIndent()

    val result = CsvParser.parse(csv, fileName = "test.csv")
    assertEquals(3, result.headers.size)
    assertEquals("Name", result.headers[0])
    assertEquals("Age", result.headers[1])
    assertEquals("Score", result.headers[2])
    assertEquals(2, result.rows.size)
    assertEquals("Alice", result.rows[0][0])
    assertEquals("25", result.rows[0][1])
    assertEquals(ColumnType.NUMBER, result.columnTypes[1])
    assertEquals(ColumnType.NUMBER, result.columnTypes[2])
  }

  @Test
  fun testCsvParserQuotesAndCommas() {
    val csv = """"Product Name","Location","Price"
"Super Widget, v2.0","New York, NY",199.99
"Standard ""Classic"" Gizmo","San Francisco, CA",49.50"""

    val result = CsvParser.parse(csv, fileName = "quotes.csv")
    assertEquals(2, result.rows.size)
    assertEquals("Super Widget, v2.0", result.rows[0][0])
    assertEquals("New York, NY", result.rows[0][1])
    assertEquals("""Standard "Classic" Gizmo""", result.rows[1][0])
  }

  @Test
  fun testDelimiterDetection() {
    val commaSample = "ID,Name,Department\n1,Alice,Sales\n2,Bob,Tech"
    assertEquals(',', CsvParser.detectDelimiter(commaSample))

    val semicolonSample = "ID;Name;Department\n1;Alice;Sales\n2;Bob;Tech"
    assertEquals(';', CsvParser.detectDelimiter(semicolonSample))

    val tabSample = "ID\tName\tDepartment\n1\tAlice\tSales\n2\tBob\tTech"
    assertEquals('\t', CsvParser.detectDelimiter(tabSample))
  }

  @Test
  fun testExcelColumnNames() {
    assertEquals("A", ExcelGenerator.getColumnName(0))
    assertEquals("B", ExcelGenerator.getColumnName(1))
    assertEquals("Z", ExcelGenerator.getColumnName(25))
    assertEquals("AA", ExcelGenerator.getColumnName(26))
    assertEquals("AB", ExcelGenerator.getColumnName(27))
  }

  @Test
  fun testExcelGenerationProducesValidZip() {
    val csv = "Item,Qty,Price\nApples,50,1.20\nOranges,30,1.50"
    val parsed = CsvParser.parse(csv, "fruits.csv")

    val baos = ByteArrayOutputStream()
    ExcelGenerator.generateXlsx(parsed, baos, sheetName = "Inventory")
    val bytes = baos.toByteArray()

    assertTrue(bytes.isNotEmpty())

    // Validate that it is a valid ZIP with expected OpenXML entries
    val entries = mutableListOf<String>()
    ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
      var entry = zis.nextEntry
      while (entry != null) {
        entries.add(entry.name)
        zis.closeEntry()
        entry = zis.nextEntry
      }
    }

    assertTrue(entries.contains("[Content_Types].xml"))
    assertTrue(entries.contains("_rels/.rels"))
    assertTrue(entries.contains("xl/_rels/workbook.xml.rels"))
    assertTrue(entries.contains("xl/workbook.xml"))
    assertTrue(entries.contains("xl/styles.xml"))
    assertTrue(entries.contains("xl/worksheets/sheet1.xml"))
  }
}
