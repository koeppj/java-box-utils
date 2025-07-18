package com.box.utils;

import java.io.IOException;
import java.lang.reflect.Array;
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
        return reportConfig.fields;
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
        // Implement the logic to run the query against the Box API
        // This is a placeholder for actual query execution logic
        String newMarker = null;

        MetadataQueryBuilder builder = new MetadataQuery.MetadataQueryBuilder(reportConfig.getFrom(), reportConfig.ancestorFolderId);
        builder.query(reportConfig.query);
        builder.queryParams(reportConfig.queryParams);
        builder.fields(reportConfig.getAllFields());
        MetadataQueryResults results = client.getSearch().searchByMetadataQuery(builder.build());
        if (results.getEntries() != null) {
            List<FileFullOrFolderFull> entries = results.getEntries();
            for (FileFullOrFolderFull entry : entries) {
                FileFull fileFull = entry.getFileFull();
                if (null != fileFull) {
                    Object[] entryValues = new Array[reportConfig.getAllFields().size()];
                    // Initialize the entryValues array with nulls
                    for (int i = 0; i < entryValues.length; i++) {
                        entryValues[i] = null;
                    }
                    // first fill in the file properties
                    for (int i = 0; i < reportConfig.fileProperties.length; i++) {
                        String propName = reportConfig.fileProperties[i];
                        Object propValue = jsonNodeToObject(fileFull.getRawData().get(propName));
                        entryValues[i] = propValue;
                    }
                    // now fill in the metadata fields
                    Map<String,MetadataFull> metadataForScope = fileFull.getMetadata().getExtraData().get(reportConfig.getScopeEid());
                    MetadataFull metadata = metadataForScope.get(reportConfig.from);
                    if (metadata != null) {
                        for (int i = 0; i < reportConfig.fields.length; i++) {
                            String fieldName = reportConfig.fields[i];
                            Map<String,Object> metadataFields = metadata.getExtraData();
                            entryValues[i + reportConfig.fileProperties.length] = metadataFields.get(fieldName);
                        }
                    }
                    writer.writeRecord(entryValues);
                } else {
                    // Handle folder entries if needed
                    // For now, we are only processing file entries
                }
            }
        }
    }
}


