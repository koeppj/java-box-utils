package com.box.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import java.io.FileWriter;
import java.io.IOException;

public class ReportWriter {
    private final String outputFileName;
    private final String[] columnHeaders;
    private CSVPrinter csvPrinter;

    public ReportWriter(String outputFileName, String[] columnHeaders) throws IOException {
        this.outputFileName = outputFileName;
        this.columnHeaders = columnHeaders;
        FileWriter writer = new FileWriter(outputFileName);
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
