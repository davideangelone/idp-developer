package com.idp.developer.ui;

import java.time.Duration;

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
                readOnlyField("Authorization Grant Types", oAuth2ClientDto.authorizationGrantTypes()),
                readOnlyField("Authorization Consent", String.valueOf(oAuth2ClientDto.authorizationConsent())),
                readOnlyField("Require Proof Key", String.valueOf(oAuth2ClientDto.requireProofKey())),
                readOnlyField("Access Token TTL", formatDuration(oAuth2ClientDto.accessTokenTtl())),
                readOnlyField("Refresh Token TTL", formatDuration(oAuth2ClientDto.refreshTokenTtl())),
                readOnlyField("Authorization Code TTL", formatDuration(oAuth2ClientDto.authorizationCodeTtl())),
                readOnlyField("Reuse Refresh Tokens", String.valueOf(oAuth2ClientDto.reuseRefreshTokens()))
        );

        return layout;
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "";
        }

        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(days == 1 ? " giorno" : " giorni");
        }

        if (hours > 0) {
            appendSeparator(result);
            result.append(hours).append(hours == 1 ? " ora" : " ore");
        }

        if (minutes > 0) {
            appendSeparator(result);
            result.append(minutes).append(minutes == 1 ? " minuto" : " minuti");
        }

        if (seconds > 0) {
            appendSeparator(result);
            result.append(seconds).append(seconds == 1 ? " secondo" : " secondi");
        }

        return !result.isEmpty() ? result.toString() : "0 secondi";
    }

    private void appendSeparator(StringBuilder builder) {
        if (!builder.isEmpty()) {
            builder.append(", ");
        }
    }

}