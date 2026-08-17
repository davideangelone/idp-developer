package com.idp.enterpriseidp.ui;

import java.util.Map;

import com.idp.enterpriseidp.properties.ConfigProperties;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "claims", layout = MainLayout.class)
@PageTitle("Scopes - Claims | Enterprise IDP")
public class ClaimsView extends AnonymousVerticalLayout {

    public ClaimsView(ConfigProperties configProperties) {

        setSpacing(true);
        setPadding(true);

        add(new H1("Scope / Claims"));

        add(new H2("Always"));

        VerticalLayout alwaysClaims = new VerticalLayout();
        alwaysClaims.add(
                readOnlyField(
                        null,
                        String.join(", ", configProperties.getClaims().getAlways().keySet())
                )
        );

        add(alwaysClaims);

        add(new H2("Scoped"));

        VerticalLayout scopeClaims = new VerticalLayout();

        for (Map.Entry<String, Map<String, String>> scopeEntry : configProperties.getClaims().getScopes().entrySet()) {
            scopeClaims.add(
                    readOnlyField(
                            scopeEntry.getKey(),
                            String.join(", ", scopeEntry.getValue().keySet())
                    )
            );
        }

        add(scopeClaims);
    }
}