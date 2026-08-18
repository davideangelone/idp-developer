package com.idp.developer.ui;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.idp.developer.model.OAuth2ClaimDto;
import com.idp.developer.service.OAuth2ClaimService;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "claims", layout = MainLayout.class)
@PageTitle("Scopes - Claims | Developer IDP")
public class ClaimsView extends AnonymousVerticalLayout {

    public ClaimsView(OAuth2ClaimService claimService) {

        setSpacing(true);
        setPadding(true);

        add(new H1("Scope / Claims"));

        add(new H2("Always"));

        VerticalLayout alwaysClaims = new VerticalLayout();

        List<String> alwaysClaimsList = claimService.getAlwaysClaims()
                .stream()
                .map(OAuth2ClaimDto::name)
                .toList();
        alwaysClaims.add(readOnlyField(null, alwaysClaimsList));

        add(alwaysClaims);

        add(new H2("Scoped"));

        VerticalLayout scopeClaims = new VerticalLayout();
        Map<String, List<OAuth2ClaimDto>> scopedClaimsMap = claimService.getScopedClaims()
                                                                                .stream()
                                                                                .collect(Collectors.groupingBy(OAuth2ClaimDto::scope));
        for (Map.Entry<String, List<OAuth2ClaimDto>> entry : scopedClaimsMap.entrySet()) {
            scopeClaims.add(readOnlyField(entry.getKey(), entry.getValue().stream()
                                                                            .map(OAuth2ClaimDto::name)
                                                                            .toList()
                                         )
                            );
        }

        add(scopeClaims);
    }
}