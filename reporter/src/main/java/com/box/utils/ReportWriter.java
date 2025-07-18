package com.box.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReportWriter {
    private CSVPrinter csvPrinter;

    public ReportWriter(File outputFile, String[] columnHeaders) throws IOException {
        FileWriter writer = new FileWriter(outputFile);
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(columnHeaders)
                .get();
        csvPrinter = new CSVPrinter(writer, format);
    }

    public void writeRecord(Object[] record) throws IOException {
        csvPrinter.printRecord(record);
    }

    public void close() throws IOException {
        csvPrinter.close();
    }
}
