package com.box.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ExcelTabularWriter implements TabularWriter {
    private final Path path;
    private SXSSFWorkbook workbook;
    private Sheet sheet;
    private int rowIndex = 0;
    private CellStyle headerStyle;
    private CellStyle dateCellStyle;

    public ExcelTabularWriter(Path path) {
        this.path = path;
    }

    @Override
    public void start(String sheetName, String dateFormat) {
        workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        sheet = workbook.createSheet((sheetName == null || sheetName.isBlank()) ? "Report" : sheetName);

        Font bold = workbook.createFont();
        bold.setBold(true);
        headerStyle = workbook.createCellStyle();
        headerStyle.setFont(bold);
        dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(dateFormat));
    }

    @Override
    public void writeHeader(List<String> headers) {
        Row row = sheet.createRow(rowIndex++);
        for (int c = 0; c < headers.size(); c++) {
            Cell cell = row.createCell(c);
            cell.setCellValue(headers.get(c));
            cell.setCellStyle(headerStyle);
        }
    }

    @Override
    public void writeRow(List<?> cells) {
        Row row = sheet.createRow(rowIndex++);
        for (int c = 0; c < cells.size(); c++) {
            Object val = cells.get(c);
            Cell cell = row.createCell(c);
            if (val == null) { cell.setBlank(); continue; }
            if (val instanceof Number n) cell.setCellValue(n.doubleValue());
            else if (val instanceof Boolean b) cell.setCellValue(b);
            else if (val instanceof java.util.Date d) {
                cell.setCellValue(d);
                cell.setCellStyle(dateCellStyle);
            }
            else cell.setCellValue(val.toString());
        }
    }

    @Override
    public void close() throws IOException {
        try (OutputStream os = Files.newOutputStream(path)) {
            workbook.write(os);
        } finally {
            workbook.dispose();
            workbook.close();
        }
    }
}
