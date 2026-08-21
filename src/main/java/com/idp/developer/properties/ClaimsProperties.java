package com.idp.developer.properties;

import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class ClaimsProperties {
    private Set<String> always;
    private Map<String, Set<String>> scopeMappings;
    private Map<String, String> claimMappings;
}
