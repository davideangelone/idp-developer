package com.idp.developer.ui;

import com.idp.developer.model.OAuth2ClientDto;
import com.idp.developer.service.OAuth2ClientService;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "clients", layout = MainLayout.class)
@PageTitle("OAuth2 Clients | Developer IDP")
public class ClientsView extends AnonymousVerticalLayout {

    public ClientsView(OAuth2ClientService oAuth2ClientService) {

        setSpacing(true);
        setPadding(true);

        add(new H1("OAuth2 Clients"));

        oAuth2ClientService
                .getAllOAuth2Clients()
                .forEach(oAuth2ClientDto -> add(createDetails(oAuth2ClientDto)));
    }

    private Details createDetails(OAuth2ClientDto oAuth2ClientDto) {
        Span summary = new Span(oAuth2ClientDto.name());
        summary.getStyle().set("font-weight", "bold");

        return new Details(summary, createClientPanel(oAuth2ClientDto));
    }

    private FormLayout createClientPanel(OAuth2ClientDto oAuth2ClientDto) {

        FormLayout layout = new FormLayout();

        layout.add(
                readOnlyField("Client URL", oAuth2ClientDto.clientUrl()),
                new Span(),
                readOnlyField("Client ID", oAuth2ClientDto.clientId()),
                readOnlyField("Client Secret", oAuth2ClientDto.clientSecret()),
                readOnlyTextArea("Redirect URIs", oAuth2ClientDto.redirectUris()),
                readOnlyTextArea("Post Logout Redirect URIs", oAuth2ClientDto.postLogoutRedirectUris()),
                readOnlyField("Scopes", oAuth2ClientDto.scopes()),
                readOnlyField("Authorization Grant Types", oAuth2ClientDto.grantTypes())
        );

        return layout;
    }

}