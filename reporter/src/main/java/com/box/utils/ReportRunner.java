package com.box.utils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.box.sdkgen.client.BoxClient;
import com.box.sdkgen.managers.metadatatemplates.GetMetadataTemplateScope;
import com.box.sdkgen.schemas.filefull.FileFull;
import com.box.sdkgen.schemas.filefullorfolderfull.FileFullOrFolderFull;
import com.box.sdkgen.schemas.metadatafull.MetadataFull;
import com.box.sdkgen.schemas.metadataquery.MetadataQuery;
import com.box.sdkgen.schemas.metadataquery.MetadataQuery.MetadataQueryBuilder;
import com.box.sdkgen.schemas.metadataqueryresults.MetadataQueryResults;
import com.box.sdkgen.schemas.metadatatemplate.MetadataTemplate;
import com.box.sdkgen.schemas.metadatatemplate.MetadataTemplateFieldsTypeField;
import com.fasterxml.jackson.databind.JsonNode;

public class ReportRunner {

private ReportConfig reportConfig;
private BoxClient client;
private ReportWriter writer;
private DateTimeFormatter  dateFormatter;
private MetadataTemplate metadataTemplate;
private Map<String,MetadataTemplateFieldsTypeField> metadataFieldTypes = new HashMap<String, MetadataTemplateFieldsTypeField>();

    public ReportRunner(ReportConfig reportConfig, BoxClient client, ReportWriter writer) {
        this.reportConfig = reportConfig;
        this.client = client;
        this.writer = writer;
        this.dateFormatter = DateTimeFormatter.ofPattern(reportConfig.getDateFormat());
        getMetadaTemplate();
    }

    private void getMetadaTemplate() {
        this.metadataTemplate = 
            client.metadataTemplates.getMetadataTemplate(GetMetadataTemplateScope.ENTERPRISE,reportConfig.getTemplate());
        metadataTemplate.getFields().forEach( field -> {
            this.metadataFieldTypes.put(field.getKey(), field.getType().getEnumValue());
        });
    }

    public String[] getFIelds() {
        return reportConfig.getFields();
    }

    public Object getValueOfMetadataField(String fieldName, Object rawValue) {
        MetadataTemplateFieldsTypeField fieldType = this.metadataFieldTypes.get(fieldName);
        if (fieldType != null && rawValue != null) {
            switch (fieldType) {
                case STRING:
                    return rawValue.toString();
                case DATE:
                    if (rawValue instanceof String) {
                        OffsetDateTime dateTime = OffsetDateTime.parse((String) rawValue);
                        return this.dateFormatter.format(dateTime);
                    }
                    return rawValue;
                case FLOAT:
                    if (rawValue instanceof Number) {
                        return ((Number) rawValue).doubleValue();
                    }
                    return rawValue;
                case INTEGER:
                    if (rawValue instanceof Number) {
                        return ((Number) rawValue).intValue();
                    }
                    return rawValue;
                case ENUM:
                    return rawValue.toString();
                case MULTISELECT:
                    return rawValue.toString();
                default:
                    return rawValue;
            }
        }
        return rawValue;
    }

    private Object jsonNodeToObject(JsonNode node) {
        if (null == node) {
            return null;
        } else if (node.isTextual()) {
            String text = node.asText();
            if (text.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)")) {
                OffsetDateTime dateTime = OffsetDateTime.parse(text);
                return this.dateFormatter.format(dateTime);
            } else if (text.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z")) {
                OffsetDateTime dateTime = OffsetDateTime.parse(text);
                return this.dateFormatter.format(dateTime);
            } else {
                return text;
            }
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
        System.out.printf("Running query for report: %s%n", reportConfig.getConfigFile());
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
        builder.limit((long) 1000);
        while (hasMore) {
            MetadataQueryResults results = client.getSearch().searchByMetadataQuery(builder.build());
            if (results.getEntries() != null) {
                List<FileFullOrFolderFull> entries = results.getEntries();
                for (FileFullOrFolderFull entry : entries) {
                    FileFull fileFull = entry.getFileFull();
                    if (null != fileFull) {
                        count++;
                        if ((null != reportConfig.getLimit()) && (count >= reportConfig.getLimit())) {
                            System.out.printf("%nReached the limit of %,d records.  Stopping further processing.%n",reportConfig.getLimit());
                            hasMore = false;
                            break;
                        }
                        if (count % 1000 == 0) {
                            System.out.printf("\rProcessed %,d records so far...", count);
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
                                    entryValues[i + reportConfig.getFileProperties().length] 
                                        = this.getValueOfMetadataField(fieldName, metadataFields.get(fieldName));
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


