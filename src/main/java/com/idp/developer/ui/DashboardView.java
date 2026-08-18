package com.idp.developer.ui;

import com.idp.developer.service.AuthorizationServerService;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Developer IDP")
public class DashboardView extends AnonymousVerticalLayout {

    private static final String OPENID_CONFIGURATION_URL = "/.well-known/openid-configuration";

    public DashboardView(AuthorizationServerService authorizationServerService) {

        String openIdConfigurationUrl = authorizationServerService.getAuthorizationServer().issuerUrl() + OPENID_CONFIGURATION_URL;
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