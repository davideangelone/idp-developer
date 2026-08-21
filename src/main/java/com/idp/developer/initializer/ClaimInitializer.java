package com.idp.developer.initializer;

import java.util.Map;
import java.util.Set;

import com.idp.developer.entity.OAuth2Claim;

public class ClaimInitializer {

    private ClaimInitializer() {
    }

    public static void init(InitializerBean initializerBean) {

        Map<String, String> claimMappings = initializerBean.getConfigProperties().getClaims().getClaimMappings();
        Set<String> alwaysClaims = initializerBean.getConfigProperties().getClaims().getAlways();

        for (Map.Entry<String, String> claimEntry : claimMappings.entrySet()) {
            String claimName = claimEntry.getKey();
            String userProperty = claimEntry.getValue();

            OAuth2Claim claim = initializerBean.getOAuth2ClaimRepository()
                    .findByName(claimName)
                    .orElseGet(OAuth2Claim::new);

            claim.setAlways(alwaysClaims.contains(claimName));
            claim.setName(claimName);
            claim.setUserProperty(userProperty);

            initializerBean.getOAuth2ClaimRepository().save(claim);
        }
    }
}
