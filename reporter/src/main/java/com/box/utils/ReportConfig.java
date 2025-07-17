package com.box.utils;

import java.util.List;

import com.box.sdkgen.schemas.metadataquery.MetadataQueryOrderByField;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFilter("nullablePropertyFilder")
public class ReportConfig {

    @JsonProperty("scope")
    protected String scope;

    @JsonProperty("eid")
    protected String eid;

    @JsonProperty("from")
    protected String from;

    @JsonProperty("ancestorFolderId")
    protected String ancestorFolderId;

    @JsonProperty("query")
    protected String query;

    @JsonProperty("queryParams")
    protected String queryParams;

    @JsonProperty("fileProperties")
    protected String fileProperties;

    @JsonProperty("metadataFields")
    protected String[] fields;

    @JsonProperty("orderBy")
    protected List<MetadataQueryOrderByField> orderBy;
}
