package com.app.controller;
//
//import java.io.ByteArrayOutputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.HttpHeaders;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
public class ExcelController {

    // Endpoint to get unique values for a given column
    @PostMapping("/unique-values")
    public ResponseEntity<?> getUniqueValues(@RequestParam("file") MultipartFile file, 
                                             @RequestParam("column") String columnName) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            // Identify the column index based on header row
            Row headerRow = sheet.getRow(0);
            int columnIndex = -1;
            for (Cell cell : headerRow) {
                if (cell.getStringCellValue().equalsIgnoreCase(columnName)) {
                    columnIndex = cell.getColumnIndex();
                    break;
                }
            }

            if (columnIndex == -1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Column not found.");
            }

            // Collect unique values from the specified column
            Set<String> uniqueValues = new LinkedHashSet<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    Cell cell = row.getCell(columnIndex);
                    if (cell != null) {
                        String cellValue = getCellValue(cell);
                        if (cellValue != null && !cellValue.trim().isEmpty()) {
                            uniqueValues.add(cellValue);
                        }
                    }
                }
            }
            workbook.close();

            return ResponseEntity.ok(uniqueValues);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file.");
        }
    }

    // Endpoint to filter rows based on selected values for a given column
    @PostMapping("/filter-rows")
    public ResponseEntity<?> filterRows(@RequestParam("file") MultipartFile file,
                                        @RequestParam("column") String columnName,
                                        @RequestParam("selectedValues") List<String> selectedValues) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            // Identify the column index based on header row
            Row headerRow = sheet.getRow(0);
            int columnIndex = -1;
            for (Cell cell : headerRow) {
                if (cell.getStringCellValue().equalsIgnoreCase(columnName)) {
                    columnIndex = cell.getColumnIndex();
                    break;
                }
            }

            if (columnIndex == -1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Column not found.");
            }

            // Collect rows that match the selected filter values
            List<Map<String, String>> filteredRows = new ArrayList<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    Cell cell = row.getCell(columnIndex);
                    if (cell != null) {
                        String cellValue = getCellValue(cell);
                        if (selectedValues.contains(cellValue)) {
                            Map<String, String> rowData = new HashMap<>();
                            for (Cell headerCell : headerRow) {
                                rowData.put(headerCell.getStringCellValue(), getCellValue(row.getCell(headerCell.getColumnIndex())));
                            }
                            filteredRows.add(rowData);
                        }
                    }
                }
            }
            workbook.close();

            return ResponseEntity.ok(filteredRows);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file.");
        }
    }

    // Helper method to extract cell value as string based on cell type
    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}






//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.app.pojo.FilterRequest;
//
//@RestController
//@RequestMapping("/api/excel")
//public class ExcelController {
//
//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file) {
//        try {
//            // Step 1: Read the Excel file
//            Workbook workbook = new XSSFWorkbook(file.getInputStream());
//            Sheet sheet = workbook.getSheetAt(0); // Assuming first sheet
//
//            // Step 2: Extract unique values for each column
//            Map<String, Set<String>> uniqueColumnValues = new LinkedHashMap<String, Set<String>>();
//            Row headerRow =  sheet.getRow(0);
//            for (Cell cell : headerRow) {
//                int columnIndex = cell.getColumnIndex();
//                String columnName = cell.getStringCellValue();
//
//                Set<String> uniqueValues = new LinkedHashSet<String>();
//                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
//                    Row row =  sheet.getRow(rowIndex);
//                    Cell columnCell = row.getCell(columnIndex);
//                    if (columnCell != null) {
//                        uniqueValues.add(columnCell.toString());
//                    }
//                }
//                uniqueColumnValues.put(columnName, uniqueValues);
//            }
//            workbook.close();
//
//            // Return the unique values as JSON
//            return ResponseEntity.ok(uniqueColumnValues);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file");
//        }
//    }
//
//    @PostMapping("/filter")
//    public ResponseEntity<?> filterData(@RequestParam("file") MultipartFile file, 
//                                        @RequestBody FilterRequest filterRequest) {   try {
//            Workbook workbook = new XSSFWorkbook(file.getInputStream());
//            Sheet sheet = workbook.getSheetAt(0);
//
//            // Step 1: Extract rows matching filter
//            List<Map<String, String>> filteredRows = new ArrayList<>();
//            Row headerRow =  sheet.getRow(0);
//            Map<String, Integer> columnIndices = new HashMap<>();
//
//            // Map column names to indices
//            for (Cell cell : headerRow) {
//                columnIndices.put(cell.getStringCellValue(), cell.getColumnIndex());
//            }
//
//            for (int rowIndex = 1; rowIndex <= ((org.apache.poi.ss.usermodel.Sheet) sheet).getLastRowNum(); rowIndex++) {
//                Row row = ((org.apache.poi.ss.usermodel.Sheet) sheet).getRow(rowIndex);
//                boolean match = true;
//                Map<String, String> rowData = new HashMap<String, String>();
//
//                for (String column : filterRequest.getFilters().keySet()) {
//                    int columnIndex = columnIndices.get(column);
//                    String value = row.getCell(columnIndex).toString();
//                    rowData.put(column, value);
//
//                    if (!filterRequest.getFilters().get(column).contains(value)) {
//                        match = false;
//                        break;
//                    }
//                }
//
//                if (match) {
//                    filteredRows.add(rowData);
//                }
//            }
//            workbook.close();
//
//            // Step 2: Group by selected columns
//            Map<Object, List<Map<String, String>>> groupedData = filteredRows.stream()
//                .collect(Collectors.groupingBy(row ->
//                        filterRequest.getGroupByColumns().stream()
//                            .map(row::get)
//                            .collect(Collectors.joining("-"))
//                ));
//
//            return ResponseEntity.ok(groupedData);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file");
//        }
//    }
//
//    @PostMapping("/generate")
//    public ResponseEntity<?> generateExcel(@RequestBody List<Map<String, String>> groupedData) {
//        try {
//            Workbook workbook = new XSSFWorkbook();
//            Sheet sheet = (Sheet) workbook.createSheet("Grouped Data");
//
//            int rowNum = 0;
//
//            // Write header
//            Row headerRow = sheet.createRow(rowNum++);
//            if (!groupedData.isEmpty()) {
//                List<String> columns = new ArrayList<String>(groupedData.get(0).keySet());
//                for (int i = 0; i < columns.size(); i++) {
//                    headerRow.createCell(i).setCellValue(columns.get(i));
//                }
//            }
//
//            // Write data
//            for (Map<String, String> rowData : groupedData) {
//                Row row = sheet.createRow(rowNum++);
//                int colNum = 0;
//                for (String value : rowData.values()) {
//                    row.createCell(colNum++).setCellValue(value);
//                }
//            }
//
//            // Write to file
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            workbook.write(outputStream);
//            workbook.close();
//
//            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grouped-data.xlsx");
//
//            return ResponseEntity.ok()
//                .headers(headers)
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(resource);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
//}
//
