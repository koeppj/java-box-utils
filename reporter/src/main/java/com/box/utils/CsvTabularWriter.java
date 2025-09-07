package com.box.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvTabularWriter implements TabularWriter {
    private final Path path;
    private BufferedWriter writer;
    private CSVPrinter printer;

    public CsvTabularWriter(Path path) {
        this.path = path;
    }

    @Override
    public void start(String ignored, String ignored2) throws IOException {
        writer = Files.newBufferedWriter(path);
        printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
    }

    @Override
    public void writeHeader(List<String> headers) throws IOException {
        printer.printRecord(headers);
    }

    @Override
    public void writeRow(List<?> cells) throws IOException {
        printer.printRecord(cells);
    }

    @Override
    public void close() throws IOException {
        if (printer != null) printer.close(true);
        if (writer != null) writer.close();
    }
}
