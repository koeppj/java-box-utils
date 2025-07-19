package com.box.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.box.sdkgen.client.BoxClient;
import com.box.sdkgen.schemas.filefull.FileFull;
import com.box.sdkgen.schemas.filefullorfolderfull.FileFullOrFolderFull;
import com.box.sdkgen.schemas.metadatafull.MetadataFull;
import com.box.sdkgen.schemas.metadataquery.MetadataQuery;
import com.box.sdkgen.schemas.metadataquery.MetadataQuery.MetadataQueryBuilder;
import com.box.sdkgen.schemas.metadataqueryresults.MetadataQueryResults;
import com.fasterxml.jackson.databind.JsonNode;

public class ReportRunner {

private ReportConfig reportConfig;
private BoxClient client;
private ReportWriter writer;

    public ReportRunner(ReportConfig reportConfig, BoxClient client, ReportWriter writer) {
        this.reportConfig = reportConfig;
        this.client = client;
        this.writer = writer;
    }

    public String[] getFIelds() {
        return reportConfig.getFields();
    }

    private Object jsonNodeToObject(JsonNode node) {
        if (null == node) {
            return null;
        } else if (node.isTextual()) {
            return node.asText();
        } else if (node.isNumber()) {
            return node.asDouble();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isArray()) {
            Object[] array = new Object[node.size()];
            for (int i = 0; i < node.size(); i++) {
                array[i] = jsonNodeToObject(node.get(i));
            }
            return array;
        } else if (node.isObject()) {
            return node.toString(); // or convert to a Map
        }
        return null;
    }

    public void runQuery() throws IOException {
        System.out.println("Running query for report: ");
        // Implement the logic to run the query against the Box API
        // This is a placeholder for actual query execution logic
        String newMarker = null;

        MetadataQueryBuilder builder = new MetadataQuery.MetadataQueryBuilder(reportConfig.getFrom(), reportConfig.getAncestorFolderId());
        if (reportConfig.getQuery() != null && !reportConfig.getQuery().isEmpty()) {
            builder.query(reportConfig.getQuery());
        } 
        if (reportConfig.getQueryParams() != null && !reportConfig.getQueryParams().isEmpty()) {
            builder.queryParams(reportConfig.getQueryParams());
        }
        if (reportConfig.getOrderBy() != null && !reportConfig.getOrderBy().isEmpty()) {
            builder.orderBy(reportConfig.getOrderBy());
        }
        builder.fields(reportConfig.getAllFields());
        boolean hasMore = true;
        int count = 0;
        while (hasMore) {
            MetadataQueryResults results = client.getSearch().searchByMetadataQuery(builder.build());
            if (results.getEntries() != null) {
                List<FileFullOrFolderFull> entries = results.getEntries();
                for (FileFullOrFolderFull entry : entries) {
                    FileFull fileFull = entry.getFileFull();
                    if (null != fileFull) {
                        count++;
                        if ((null != reportConfig.getLimit()) && (count >= reportConfig.getLimit())) {
                            System.out.println("Reached the limit of " + reportConfig.getLimit() + " records. Stopping further processing.");
                            hasMore = false;
                            break;
                        }
                        if (count % 1000 == 0) {
                            System.out.println("Processed " + count + " records so far...");
                        }
                        Object[] entryValues = new Object[reportConfig.getAllFields().size()];
                        // Initialize the entryValues array with nulls
                        for (int i = 0; i < entryValues.length; i++) {
                            entryValues[i] = null;
                        }
                        // first fill in the file properties
                        for (int i = 0; i < reportConfig.getFileProperties().length; i++) {
                            String propName = reportConfig.getFileProperties()[i];
                            Object propValue = jsonNodeToObject(fileFull.getRawData().get(propName));
                            entryValues[i] = propValue;
                        }
                        // now fill in the metadata fields
                        Map<String,MetadataFull> metadataForScope = fileFull.getMetadata().getExtraData().get(reportConfig.getScopeEid());
                        MetadataFull metadata = metadataForScope.get(reportConfig.getTemplate());
                        if (metadata != null) {
                            for (int i = 0; i < reportConfig.getFields().length; i++) {
                                String fieldName = reportConfig.getFields()[i];
                                Map<String,Object> metadataFields = metadata.getExtraData();
                                if (null != metadataFields) {
                                    entryValues[i + reportConfig.getFileProperties().length] = metadataFields.get(fieldName);
                                }
                                else {
                                    entryValues[i + reportConfig.getFileProperties().length] = null;
                                }
                            }
                        }
                        writer.writeRecord(entryValues);
                    } else {
                        // Handle folder entries if needed
                        // For now, we are only processing file entries
                    }
                }
                if (results.getNextMarker() != null) {
                    newMarker = results.getNextMarker();
                    builder.marker(newMarker);
                } else {
                    hasMore = false;
                }
            }
            else {
                hasMore = false;
            }
        }
        writer.close();
        System.out.println("Query completed. Total records processed: " + count);
    }
}


