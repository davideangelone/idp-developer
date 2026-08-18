package com.idp.developer.ui;

import java.time.Duration;

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
                readOnlyField("Authorization Consent", String.valueOf(authorizationServerService.getAuthorizationServer().authorizationConsent())),
                readOnlyField("Access Token TTL", formatDuration(authorizationServerService.getAuthorizationServer().accessTokenTtl())),
                readOnlyField("Refresh Token TTL", formatDuration(authorizationServerService.getAuthorizationServer().refreshTokenTtl())),
                readOnlyField("Authorization Code TTL", formatDuration(authorizationServerService.getAuthorizationServer().authorizationCodeTtl())),
                readOnlyField("Reuse Refresh Tokens", String.valueOf(authorizationServerService.getAuthorizationServer().reuseRefreshTokens())),
                readOnlyField("Supported scopes", authorizationServerService.getAuthorizationServer().supportedScopes()),
                readOnlyField("Supported grant types", authorizationServerService.getAuthorizationServer().supportedGrantTypes()),
                readOnlyField("Login", authorizationServerService.getAuthorizationServer().freeLogin() ? "Free" : "Default")
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