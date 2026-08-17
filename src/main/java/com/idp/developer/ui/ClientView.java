package com.idp.developer.ui;

import com.idp.developer.properties.ConfigProperties;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "clients", layout = MainLayout.class)
@PageTitle("OAuth2 Clients | Developer IDP")
public class ClientView extends AnonymousVerticalLayout {

    public ClientView(ConfigProperties configProperties) {

        setSpacing(true);
        setPadding(true);

        add(new H1("Oauth Client"));

        FormLayout oauth2 = new FormLayout();

        TextArea redirectUris = new TextArea("Redirect URIs");
        redirectUris.setValue(
                String.join(System.lineSeparator(), configProperties.getOauth2Client().getRedirectUris())
        );
        redirectUris.setReadOnly(true);
        redirectUris.setWidthFull();

        TextArea postLogoutRedirectUris = new TextArea("Post Logout Redirect URIs");
        postLogoutRedirectUris.setValue(
                String.join(System.lineSeparator(), configProperties.getOauth2Client().getPostLogoutRedirectUris())
        );
        postLogoutRedirectUris.setReadOnly(true);
        postLogoutRedirectUris.setWidthFull();

        oauth2.add(
                readOnlyField(
                        "Client URL",
                        configProperties.getOauth2Client().getClientUrl()
                ),
                new Span(),
                readOnlyField(
                        "Client ID",
                        configProperties.getOauth2Client().getClientId()
                ),
                readOnlyField(
                        "Client Secret",
                        configProperties.getOauth2Client().getClientSecret()
                ),
                redirectUris,
                postLogoutRedirectUris,
                readOnlyField(
                        "Scopes",
                        String.join(", ", configProperties.getOauth2Client().getScopes())
                ),
                readOnlyField(
                        "Authorization Grant Types",
                        String.join(", ", configProperties.getOauth2Client().getAuthorizationGrantTypes())
                )
        );

        add(oauth2);
    }
}