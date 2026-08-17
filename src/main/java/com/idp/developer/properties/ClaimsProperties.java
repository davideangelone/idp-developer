package com.idp.developer.properties;

import java.util.Map;

import lombok.Data;

@Data
public class ClaimsProperties {
    private Map<String, String> always;
    private Map<String, Map<String, String>> scopes;
}
