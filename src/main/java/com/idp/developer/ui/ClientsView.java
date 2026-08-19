package com.idp.developer.ui;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.idp.developer.model.OAuth2ClientDto;
import com.idp.developer.model.OAuth2ClientUpdateDto;
import com.idp.developer.service.OAuth2ClientService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

@Route(value = "clients", layout = MainLayout.class)
@PageTitle("OAuth2 Clients | Developer IDP")
@Slf4j
public class ClientsView extends AnonymousVerticalLayout {

    public ClientsView(OAuth2ClientService oAuth2ClientService) {

        setSpacing(true);
        setPadding(true);

        add(new H1("OAuth2 Clients"));

        oAuth2ClientService
                .getAllOAuth2Clients()
                .forEach(oAuth2ClientDto -> add(createDetails(oAuth2ClientService, oAuth2ClientDto)));
    }

    private Details createDetails(OAuth2ClientService oAuth2ClientService, OAuth2ClientDto oAuth2ClientDto) {
        Span summary = new Span(oAuth2ClientDto.name());
        summary.getStyle().setFontWeight("bold");

        return new Details(summary, createClientPanel(oAuth2ClientService, oAuth2ClientDto));
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

    private FormLayout createClientPanel(OAuth2ClientService oAuth2ClientService, OAuth2ClientDto oAuth2ClientDto) {

        FormLayout layout = new FormLayout();

        TextField clientUrl = new TextField("Client URL");
        clientUrl.setValue(oAuth2ClientDto.clientUrl());

        TextField clientId = new TextField("Client ID");
        clientId.setValue(oAuth2ClientDto.clientId());

        PasswordField clientSecret = new PasswordField("Client Secret");
        clientSecret.setValue(oAuth2ClientDto.clientSecret());

        MultiSelectComboBox<String> authenticationMethods = new MultiSelectComboBox<>("Client Authentication Methods");
        authenticationMethods.setItems(oAuth2ClientDto.clientAuthenticationMethods());

        authenticationMethods.setValue(new HashSet<>(oAuth2ClientDto.clientAuthenticationMethods()));

        TextArea redirectUris = new TextArea("Redirect URIs");
        redirectUris.setValue(String.join(System.lineSeparator(), oAuth2ClientDto.redirectUris()));

        TextArea postLogoutRedirectUris = new TextArea("Post Logout Redirect URIs");
        postLogoutRedirectUris.setValue(String.join(System.lineSeparator(), oAuth2ClientDto.postLogoutRedirectUris()));

        Checkbox authorizationConsent = new Checkbox("Authorization Consent");
        authorizationConsent.setValue(oAuth2ClientDto.authorizationConsent());

        Checkbox requireProofKey = new Checkbox("Require Proof Key");
        requireProofKey.setValue(oAuth2ClientDto.requireProofKey());

        Checkbox reuseRefreshTokens = new Checkbox("Reuse Refresh Tokens");
        reuseRefreshTokens.setValue(oAuth2ClientDto.reuseRefreshTokens());

        TextField accessTokenTtl = durationField("Access Token TTL", oAuth2ClientDto.accessTokenTtl());
        TextField refreshTokenTtl = durationField("Refresh Token TTL", oAuth2ClientDto.refreshTokenTtl());
        TextField authorizationCodeTtl = durationField("Authorization Code TTL", oAuth2ClientDto.authorizationCodeTtl());

        Button save = new Button("Salva");
        save.addClickListener(event -> {

            try {
                OAuth2ClientUpdateDto updateDto = new OAuth2ClientUpdateDto(
                        oAuth2ClientDto.id(),
                        oAuth2ClientDto.name(),
                        oAuth2ClientDto.clientId(),
                        clientUrl.getValue(),
                        clientSecret.getValue(),
                        parseLines(redirectUris.getValue()),
                        parseLines(postLogoutRedirectUris.getValue()),
                        oAuth2ClientDto.scopes(),
                        oAuth2ClientDto.authorizationGrantTypes(),
                        oAuth2ClientDto.clientAuthenticationMethods(),
                        authorizationConsent.getValue(),
                        requireProofKey.getValue(),
                        Duration.ofSeconds(Long.parseLong(accessTokenTtl.getValue())),
                        Duration.ofSeconds(Long.parseLong(refreshTokenTtl.getValue())),
                        Duration.ofSeconds(Long.parseLong(authorizationCodeTtl.getValue())),
                        reuseRefreshTokens.getValue()
                );

                oAuth2ClientService.updateOAuth2Client(updateDto);

                showClientUpdatedNotification(oAuth2ClientDto.name());
                log.info("Client {} aggiornato", oAuth2ClientDto.name());
            } catch (Exception e) {
                log.error("Errore durante l'aggiornamento del client {}", oAuth2ClientDto.name(), e);
                showClientUpdateErrorNotification(oAuth2ClientDto.name());
            }
        });


        layout.add(
                clientUrl,
                clientId,
                clientSecret,
                redirectUris,
                postLogoutRedirectUris,
                authorizationConsent,
                requireProofKey,
                reuseRefreshTokens,
                accessTokenTtl,
                refreshTokenTtl,
                authorizationCodeTtl,
                new Span(),
                save
        );

        return layout;
    }

    private TextField durationField(String label, Duration duration) {
        TextField field = new TextField(label);
        field.setAllowedCharPattern("[0-9]");

        long seconds = duration != null ? duration.toSeconds() : 0;
        field.setValue(String.valueOf(seconds));

        Span description = new Span(formatDuration(duration));
        description.getStyle()
                .set("background-color", "#7f7f7f")
                .set("color", "white")
                .set("padding", "0.25rem 0.5rem")
                .set("border-radius", "5px")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("white-space", "nowrap")
                .set("opacity", "0.85");

        field.setSuffixComponent(description);
        field.setValueChangeMode(ValueChangeMode.EAGER);

        field.addValueChangeListener(event -> {
            if (!event.isFromClient() || event.getValue().isBlank()) {
                return;
            }
            try {
                long newSeconds = Long.parseLong(event.getValue());

                if (newSeconds < 0) {
                    description.setText("valore non valido");
                    return;
                }

                description.setText(
                        formatDuration(Duration.ofSeconds(newSeconds))
                );

            } catch (NumberFormatException e) {
                description.setText("valore non valido");
            }
        });

        return field;
    }

    private List<String> parseLines(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private Notification getNotificationElement(Span message) {
        Notification notification = new Notification();
        notification.add(message);

        Button close = new Button(new Icon(VaadinIcon.CLOSE));
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        close.getStyle()
                .set("position", "absolute")
                .set("top", "0.15rem")
                .set("right", "0.15rem")
                .set("width", "1.5rem")
                .set("height", "1.5rem")
                .set("padding", "0");

        close.addClickListener(event -> notification.close());

        notification.getElement().getStyle()
                .set("position", "relative")
                .set("padding-right", "2rem");

        notification.add(message, close);
        notification.setDuration(5000);

        notification.open();
        return notification;
    }

    private void showClientUpdatedNotification(String name) {
        Span message = new Span();
        message.add("Client ");

        Span clientName = new Span(name);
        clientName.getStyle().setFontWeight("bold");

        message.add(clientName);
        message.add(" aggiornato");

        getNotificationElement(message).open();
    }

    private void showClientUpdateErrorNotification(String name) {
        Span message = new Span();
        message.add("Errore durante l'aggiornamento del client ");

        Span clientName = new Span(name);
        clientName.getStyle().setFontWeight("bold");

        message.add(clientName);

        getNotificationElement(message).open();
    }

}