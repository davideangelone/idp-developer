package com.idp.developer.ui;

import java.util.Map;

import com.idp.developer.properties.ConfigProperties;
import com.idp.developer.properties.OAuth2ClientProperties;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "clients", layout = MainLayout.class)
@PageTitle("OAuth2 Clients | Developer IDP")
public class ClientsView extends AnonymousVerticalLayout {

    public ClientsView(ConfigProperties configProperties) {

        setSpacing(true);
        setPadding(true);

        add(new H1("OAuth2 Clients"));

        for (Map.Entry<String, OAuth2ClientProperties> entry : configProperties.getOauth2Clients().entrySet()) {
            add(createDetails(entry.getKey(), entry.getValue()));
        }
    }

    private Details createDetails(String name, OAuth2ClientProperties client) {
        Span summary = new Span(name);
        summary.getStyle().set("font-weight", "bold");

        return new Details(summary, createClientPanel(client));
    }

    private FormLayout createClientPanel(OAuth2ClientProperties client) {

        FormLayout layout = new FormLayout();

        layout.add(
                readOnlyField("Client URL", client.getClientUrl()),
                new Span(),
                readOnlyField("Client ID", client.getClientId()),
                readOnlyField("Client Secret", client.getClientSecret()),
                readOnlyTextArea("Redirect URIs", client.getRedirectUris()),
                readOnlyTextArea("Post Logout Redirect URIs", client.getPostLogoutRedirectUris()),
                readOnlyField("Scopes", client.getScopes()),
                readOnlyField("Authorization Grant Types", client.getAuthorizationGrantTypes())
        );

        return layout;
    }

}