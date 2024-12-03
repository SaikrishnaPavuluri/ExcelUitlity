package com.app.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class ExcelFilterController {

	@PostMapping("/upload")
	public ResponseEntity<List<Map<String, String>>> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("filters") String filtersJson) throws Exception {

		// Parse filters from the request body
		Map<String, List<String>> filters = parseFilters(filtersJson);

		// Read Excel file and filter data
		List<Map<String, String>> filteredData = filterExcelData(file, filters);

		return new ResponseEntity<>(filteredData, HttpStatus.OK);
	}

	private Map<String, List<String>> parseFilters(String filtersJson) {
	
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.readValue(filtersJson, new TypeReference<Map<String, List<String>>>() {
			});
		} catch (IOException e) {
			throw new RuntimeException("Error parsing filter JSON", e);
		}
	}

	private List<Map<String, String>> filterExcelData(MultipartFile file, Map<String, List<String>> filters)
			throws Exception {
		List<Map<String, String>> filteredData = new ArrayList<>();

		// Read the Excel file
		try (InputStream is = file.getInputStream()) {
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			Sheet sheet = workbook.getSheetAt(0);

			// Process headers
			Row headerRow = sheet.getRow(0);
			Map<Integer, String> columnMapping = new HashMap<>();
			for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
				columnMapping.put(i, headerRow.getCell(i).getStringCellValue());
			}
//
			// Loop through the data rows
			for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
				Row row = sheet.getRow(rowNum);
				boolean matchesFilters = true;
				Map<String, String> rowData = new HashMap<>();

				// Loop through columns and apply filters
				for (int colNum = 0; colNum < headerRow.getPhysicalNumberOfCells(); colNum++) {
					String columnName = columnMapping.get(colNum);
					String cellValue = row.getCell(colNum) != null ? row.getCell(colNum).toString() : "";

					// Add cell value to row data map
					rowData.put(columnName, cellValue);

					// Apply filters if criteria exists
					if (filters.containsKey(columnName) && !filters.get(columnName).contains(cellValue)) {
						matchesFilters = false;
					}
				}

				if (matchesFilters) {
					filteredData.add(rowData);
				}
			}
		}

		return filteredData;
	}

	@PostMapping("/generate")
	public String generateFilteredFile(@RequestBody Map<String, List<Map<String, Object>>> data) throws Exception {
		// Extract filteredData from the received JSON
		List<Map<String, Object>> filteredData = data.get("filteredData");

		// Create a new workbook and sheet
		String originalFileName = "filteredData";
		System.out.println(filteredData);
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Filtered Data");

		// Create header row
		if (!filteredData.isEmpty()) {
			Row headerRow = sheet.createRow(0);
			Map<String, Object> firstRowData = filteredData.get(0);
			int colIndex = 0;

			// Add headers based on the first row's keys
			for (String key : firstRowData.keySet()) {
				Cell headerCell = headerRow.createCell(colIndex++);
				headerCell.setCellValue(key);
			}
		}

		// Add data to the sheet (starting from row 1, as row 0 is the header)
		int rowIndex = 1;
		for (Map<String, Object> rowData : filteredData) {
			Row row = sheet.createRow(rowIndex++);
			int colIndex = 0;
			for (Object value : rowData.values()) {
				Cell cell = row.createCell(colIndex++);
				cell.setCellValue(value != null ? value.toString() : "");
			}
		}

		// Define the output directory and file name with timestamp
		long timestamp = System.currentTimeMillis();
		String filteredFileName = "filtered_" + originalFileName + timestamp + ".xlsx";
		String downloadsDir = "C:\\Users\\pavul\\OneDrive\\Documents\\New folder (11)";
		String outputPath = downloadsDir + File.separator + filteredFileName;

		// Ensure the directory exists
		File directory = new File(downloadsDir);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		// Write the workbook to the file
		try (FileOutputStream fos = new FileOutputStream(outputPath)) {
			workbook.write(fos);
		}

		workbook.close();
		System.out.println("File generated at: " + outputPath);
		return outputPath; // Return the file path if needed
	}

}
