package com.idp.enterpriseidp.ui;

import com.idp.enterpriseidp.properties.AppProperties;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "clients", layout = MainLayout.class)
@PageTitle("OAuth2 Clients | Enterprise IDP")
public class ClientView extends VerticalLayout {

    public ClientView(AppProperties appProperties) {

        setSpacing(true);
        setPadding(true);

        add(new H1("Oauth Client"));

        FormLayout oauth2 = new FormLayout();

        TextArea redirectUris = new TextArea("Redirect URIs");
        redirectUris.setValue(
                String.join(System.lineSeparator(),
                        appProperties.getOauth2().getRedirectUris())
        );
        redirectUris.setReadOnly(true);
        redirectUris.setWidthFull();

        TextArea postLogoutRedirectUris = new TextArea("Post Logout Redirect URIs");
        postLogoutRedirectUris.setValue(
                String.join(System.lineSeparator(),
                        appProperties.getOauth2().getPostLogoutRedirectUris())
        );
        postLogoutRedirectUris.setReadOnly(true);
        postLogoutRedirectUris.setWidthFull();

        oauth2.add(
                readOnlyField(
                        "Client URL",
                        appProperties.getOauth2().getClientUrl()
                ),
                new Span(),
                readOnlyField(
                        "Client ID",
                        appProperties.getOauth2().getClientId()
                ),
                readOnlyField(
                        "Client Secret",
                        appProperties.getOauth2().getClientSecret()
                ),
                redirectUris,
                postLogoutRedirectUris,
                readOnlyField(
                        "Scopes",
                        String.join(", ", appProperties.getOauth2().getScopes())
                )
        );

        add(oauth2);
    }

    private TextField readOnlyField(String label, String value) {
        TextField field = new TextField(label);
        field.setValue(value != null ? value : "");
        field.setReadOnly(true);
        field.setWidthFull();
        return field;
    }
}