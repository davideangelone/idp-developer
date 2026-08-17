package com.idp.developer.properties;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public class JwtProperties {
    private Resource keyStore;
    private String keyStoreType;
    private String keyStorePassword;
    private String keyAlias;
    private String keyPassword;
}
