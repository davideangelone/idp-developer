package com.idp.enterpriseidp.ui;

import com.idp.enterpriseidp.properties.ConfigProperties;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Enterprise IDP")
public class DashboardView extends VerticalLayout {

    private static final String OPENID_CONFIGURATION_URL = "/.well-known/openid-configuration";
    public DashboardView(ConfigProperties configProperties) {

        String openIdConfigurationUrl =
                configProperties.getAuthorizationServer().getIssuerUrl() + OPENID_CONFIGURATION_URL;
        Anchor discoveryLink = new Anchor(
                openIdConfigurationUrl,
                "OpenID Connect Discovery"
        );
        discoveryLink.setTarget("_blank");

        add(
                new H1("Dashboard"),
                new H3("Authorization Server"),
                new Paragraph("Status: RUNNING"),
                discoveryLink
        );
    }
}