package com.idp.developer.ui;

import java.time.Duration;

import com.idp.developer.properties.ConfigProperties;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "configuration", layout = MainLayout.class)
@PageTitle("Configuration | Developer IDP")
public class ConfigurationView extends AnonymousVerticalLayout {

    public ConfigurationView(ConfigProperties configProperties) {

        setSpacing(true);
        setPadding(true);

        add(new H1("Configuration"));
        add(new H2("Authorization Server"));

        FormLayout authorizationServer = new FormLayout();
        authorizationServer.add(
                readOnlyField(
                        "Issuer",
                        configProperties.getAuthorizationServer().getIssuerUrl()
                ),
                readOnlyField(
                        "Authorization Consent",
                        String.valueOf(configProperties.getAuthorizationServer().isAuthorizationConsent())
                ),
                readOnlyField(
                        "Access Token TTL",
                        formatDuration(configProperties.getAuthorizationServer().getAccessTokenTtl())
                ),
                readOnlyField(
                        "Refresh Token TTL",
                        formatDuration(configProperties.getAuthorizationServer().getRefreshTokenTtl())
                ),
                readOnlyField(
                        "Authorization Code TTL",
                        formatDuration(configProperties.getAuthorizationServer().getAuthorizationCodeTtl())
                ),
                readOnlyField(
                        "Reuse Refresh Tokens",
                        String.valueOf(configProperties.getAuthorizationServer().isReuseRefreshTokens())
                ),
                readOnlyField(
                        "Supported scopes",
                        String.join(", ", configProperties.getAuthorizationServer().getSupportedScopes())
                ),
                readOnlyField(
                        "Login page",
                        configProperties.getAuthorizationServer().isFreeLogin() ? "Custom" : "Default"
                )
        );

        add(authorizationServer);
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