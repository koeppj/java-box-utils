package com.box.utils;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;

import com.box.sdkgen.schemas.metadataquery.MetadataQueryOrderByField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReportConfig {
    private String scope = "enterprise";

    private String eid;

    private String configFile;

    @JsonProperty(value = "template", required = true)
    private String template;

    @JsonProperty("ancestor_folder_id")
    private String ancestorFolderId = "0";

    @JsonProperty("query")
    private String query;

    @JsonProperty("query_params")
    private Map<String,Object> queryParams;

    @JsonProperty("file_properties")
    private String[] fileProperties = {"id","name"};

    @JsonProperty(value = "metadata_fields", required = true)
    private String[] fields;

    @JsonProperty("order_by")
    private List<MetadataQueryOrderByField> orderBy;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("date_format")
    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ssXXX";

    public String getDateFormat() {
        return dateFormat;
    }

    public String getScope() {
        return scope;
    }  
    
    public String getEid() {
        return eid;
    }

    public String getTemplate() {
        return template;
    }

    public String getAncestorFolderId() {
        return ancestorFolderId;
    }

    public String getQuery() {
        return query;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public String[] getFileProperties() {
        return fileProperties;
    }

    public String[] getFields() {
        return fields;
    }

    public List<MetadataQueryOrderByField> getOrderBy() {
        return orderBy;
    }

    public String getScopeEid() {
        return scope.concat("_").concat(eid);
    }

    public String getFrom() {
        return getScopeEid().concat(".").concat(template);
    }

    public Integer getLimit() {
        return limit;
    }

    public List<String> getAllFields() {
        List<String> result = new java.util.ArrayList<>();
        if (fileProperties != null) {
            for (String prop : fileProperties) {
                result.add(prop);
            }
        }
        if (fields != null) {
            String prefix = "metadata." + getFrom() + ".";
            for (String field : fields) {
                result.add(prefix + field);
            }
        }
        return result;
    }

    public static ReportConfig fromConfigFile(File arg0) throws StreamReadException, DatabindException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        ReportConfig reportConfig = mapper.readValue(arg0, ReportConfig.class);
        reportConfig.configFile = arg0.getAbsolutePath();
        reportConfig.validate();
        return reportConfig;
    }

    public void validate() throws IllegalArgumentException {
        if (template == null || template.isEmpty()) {
            throw new IllegalArgumentException("Missing required property: template");
        }
        if (fields == null || fields.length == 0) {
            throw new IllegalArgumentException("Missing required property: metadata_fields");
        }
    }

    public ReportConfig withEid(String eid) {
        this.eid = eid;
        return this;
    }

    public String getConfigFile() {
        return configFile;
    }

}
