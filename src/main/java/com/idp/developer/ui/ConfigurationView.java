package com.idp.developer.ui;

import com.idp.developer.service.AuthorizationServerService;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "configuration", layout = MainLayout.class)
@PageTitle("Configuration | Developer IDP")
public class ConfigurationView extends AnonymousVerticalLayout {

    public ConfigurationView(AuthorizationServerService authorizationServerService) {

        setSpacing(true);
        setPadding(true);

        add(new H1("Configuration"));
        add(new H2("Authorization Server"));

        FormLayout authorizationServer = new FormLayout();
        authorizationServer.add(
                readOnlyField("Issuer", authorizationServerService.getAuthorizationServer().issuerUrl()),
                readOnlyField("Supported authentication methods", authorizationServerService.getAuthorizationServer().supportedAuthenticationMethods()),
                readOnlyField("Supported scopes", authorizationServerService.getAuthorizationServer().supportedScopes()),
                readOnlyField("Supported grant types", authorizationServerService.getAuthorizationServer().supportedGrantTypes()),
                readOnlyField("Login", authorizationServerService.getAuthorizationServer().freeLogin() ? "Free" : "Default")
        );

        add(authorizationServer);
    }

}