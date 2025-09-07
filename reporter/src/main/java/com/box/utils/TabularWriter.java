package com.box.utils;

import java.io.IOException;

public interface TabularWriter extends AutoCloseable {
  void start(String sheetOrName, String dateFormat) throws IOException;
  void writeHeader(java.util.List<String> headers) throws IOException;
  void writeRow(java.util.List<?> cells) throws IOException;
  @Override void close() throws IOException;
}
