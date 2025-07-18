package com.box.utils;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;

import com.box.sdkgen.schemas.metadataquery.MetadataQueryOrderByField;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonFilter("nullablePropertyFilder")
public class ReportConfig {

    @JsonProperty("scope")
    protected String scope;

    @JsonProperty("eid")
    protected String eid;

    @JsonProperty("from")
    protected String from;

    @JsonProperty("ancestor_folder_id")
    protected String ancestorFolderId;

    @JsonProperty("query")
    protected String query;

    @JsonProperty("query_params")
    protected Map<String,Object> queryParams;

    @JsonProperty("file_properties")
    protected String[] fileProperties;

    @JsonProperty("metadata_fields")
    protected String[] fields;

    @JsonProperty("order_by")
    protected List<MetadataQueryOrderByField> orderBy;

    public String getScope() {
        return scope;
    }  
    
    public String getScopeEid() {
        return scope.concat("_").concat(eid);
    }

    public String getFrom() {
        return scope.concat("_").concat(eid).concat(".").concat(from);
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

    public static ReportConfig fromConfigFile(File arg0) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(arg0, ReportConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file: " + arg0, e);
        }
    }


}
