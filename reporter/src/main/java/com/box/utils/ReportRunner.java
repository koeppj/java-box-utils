package com.box.utils;

import java.util.List;

import com.box.sdkgen.client.BoxClient;
import com.box.sdkgen.schemas.filefull.FileFull;
import com.box.sdkgen.schemas.filefull.FileFullMetadataField;
import com.box.sdkgen.schemas.filefullorfolderfull.FileFullOrFolderFull;
import com.box.sdkgen.schemas.metadatabase.MetadataBase.MetadataBaseBuilder;
import com.box.sdkgen.schemas.metadatafull.MetadataFull;
import com.box.sdkgen.schemas.metadataquery.MetadataQuery;
import com.box.sdkgen.schemas.metadataquery.MetadataQuery.MetadataQueryBuilder;
import com.box.sdkgen.schemas.metadataqueryresults.MetadataQueryResults;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReportRunner {

    private ReportConfig reportConfig;

    public ReportRunner(java.io.File configFile) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            this.reportConfig = objectMapper.readValue(configFile, ReportConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read metadata from config file: " + configFile.getName(), e);
        }
    }

    public String[] getFIelds() {
        return reportConfig.fields;
    }

    public void runQuery(BoxClient client, ReportWriter writer) {
        // Implement the logic to run the query against the Box API
        // This is a placeholder for actual query execution logic
        String newMarker = null;

        //TODO: Add scope and EID values to from and field names

        MetadataQueryBuilder builder = new MetadataQuery.MetadataQueryBuilder(reportConfig.from, reportConfig.ancestorFolderId);
        builder.query(reportConfig.query);
        builder.queryParams(metadataQuery.getQueryParams());
        builder.fields(metadataQuery.getFields());
        if (metadataQuery.getMarker() != null) {
            newMarker = metadataQuery.getMarker();
        }
        MetadataQueryResults results = client.getSearch().searchByMetadataQuery(builder.build());
        if (results.getEntries() != null) {
            List<FileFullOrFolderFull> entries = results.getEntries();
            for (FileFullOrFolderFull entry : entries) {
                FileFull fileFull = entry.getFileFull();
                if (null != fileFull) {
                    FileFullMetadataField metadataFull = fileFull.getMetadata();
                    metadataFull.getExtraData().get("contracts");
                }
            }
        }
        System.out.println("Running query: " + metadataQuery.getQuery());
    }
}


