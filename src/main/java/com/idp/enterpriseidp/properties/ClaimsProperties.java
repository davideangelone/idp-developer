package com.idp.enterpriseidp.properties;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ClaimsProperties {

    private Map<String, String> always;
    private List<ScopeClaims> scopes;

    @Data
    public static class ScopeClaims {
        private String scope;
        private Map<String, String> mappings;
    }
}
