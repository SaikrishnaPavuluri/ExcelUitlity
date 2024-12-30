package com.app.controller;

import java.io.InputStream;
import java.util.Iterator;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.ParserConfigurationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FilterLargeExcelFile {

    public List<Map<String, String>> processExcel(String filename) throws Exception {
        // List to hold filtered rows
        List<Map<String, String>> filteredRows = new ArrayList<>();

        // Open the Excel file as a package
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader reader = new XSSFReader(pkg);

        // Get the shared strings table
        SharedStringsTable sst = (SharedStringsTable) reader.getSharedStringsTable();
        XMLReader parser = createSheetParser(sst, filteredRows);

        // Iterate over each sheet in the workbook
        Iterator<InputStream> sheets = reader.getSheetsData();
        while (sheets.hasNext()) {
            InputStream sheet = sheets.next();
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
            sheet.close();
        }
        return filteredRows;
    }

    private XMLReader createSheetParser(SharedStringsTable sst, List<Map<String, String>> filteredRows)
            throws SAXException, ParserConfigurationException {
        // Create an XML parser and set a custom handler
        XMLReader parser = org.apache.poi.util.XMLHelper.newXMLReader();
        ContentHandler handler = new SheetHandler(sst, filteredRows);
        parser.setContentHandler(handler);
        return parser;
    }

    private static class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private List<Map<String, String>> filteredRows;
        private String lastContents;
        private boolean nextIsString;

        // To store the values of the current row
        private Map<String, String> rowData = new LinkedHashMap<>();

        private int currentColumnIndex;
        private List<String> headers = new ArrayList<>();
        private int functionColumnIndex = -1;
        private int clientColumnIndex = -1;
        private int uatColumnIndex = -1;

        public SheetHandler(SharedStringsTable sst, List<Map<String, String>> filteredRows) {
            this.sst = sst;
            this.filteredRows = filteredRows;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("c")) { // "c" represents a cell
                String cellType = attributes.getValue("t"); // Check cell type
                nextIsString = (cellType != null && cellType.equals("s"));

                // Track the current column index based on the cell reference
                String cellRef = attributes.getValue("r");
                currentColumnIndex = getColumnIndex(cellRef);
            }
            lastContents = ""; // Clear previous contents
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = sst.getItemAt(idx).getString(); // Resolve shared string
                nextIsString = false;
            }

            if (qName.equals("v")) { // Cell value
                // If it's the header row, we map the column names
                if (headers.isEmpty()) {
                    headers.add(lastContents); // Store the header names
                } else {
                    // If column indices are not set, map columns
                    if (functionColumnIndex == -1 && lastContents.equals("Function")) {
                        functionColumnIndex = currentColumnIndex;
                    } else if (clientColumnIndex == -1 && lastContents.equals("Client")) {
                        clientColumnIndex = currentColumnIndex;
                    } else if (uatColumnIndex == -1 && lastContents.equals("UAT")) {
                        uatColumnIndex = currentColumnIndex;
                    }

                    // Add values based on column index
                    if (currentColumnIndex == functionColumnIndex) {
                        rowData.put("Function", lastContents);
                    } else if (currentColumnIndex == clientColumnIndex) {
                        rowData.put("Client", lastContents);
                    } else if (currentColumnIndex == uatColumnIndex) {
                        rowData.put("UAT", lastContents);
                    }
                }
            }

            if (qName.equals("row")) { // End of row
                // Apply filter condition: Function == b900000 or y00000, and UAT starts with ba
                String functionValue = rowData.get("Function");
                String uatValue = rowData.get("UAT");

                if (functionValue != null && (functionValue.equals("b900000") || functionValue.equals("y00000"))
                        && uatValue != null && uatValue.startsWith("ba")) {
                    filteredRows.add(new LinkedHashMap<>(rowData)); // Add the row if it meets the filter condition
                }

                // Reset row data for the next row
                rowData.clear();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            lastContents += new String(ch, start, length);
        }

        private int getColumnIndex(String cellRef) {
            // Extract the column part of the cell reference (e.g., "A1" -> "A")
            String columnRef = cellRef.replaceAll("[^A-Z]", "");
            int columnIndex = 0;
            for (int i = 0; i < columnRef.length(); i++) {
                columnIndex = columnIndex * 26 + (columnRef.charAt(i) - 'A' + 1);
            }
            return columnIndex - 1; // Convert to zero-based index
        }
    }

   

    public static void main(String[] args) throws Exception {
        String filePath = "C:\\Users\\pavul\\Downloads\\Book1.xlsx";
        long startTime = System.currentTimeMillis();

        FilterLargeExcelFile filterExcelFile = new FilterLargeExcelFile();
        List<Map<String, String>> result = filterExcelFile.processExcel(filePath);
        Integer i =0;
        // Print the filtered rows with only Function, Client, and UAT columns
        for (Map<String, String> row : result) {
            System.out.println(i+++" "+row);
        }
        long endTime = System.currentTimeMillis();


        long elapsedTime = endTime - startTime;

        System.out.println("Elapsed time in milliseconds: " + elapsedTime);
    }
}

