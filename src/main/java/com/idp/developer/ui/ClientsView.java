package com.idp.developer.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.idp.developer.model.AuthorizationServerDto;
import com.idp.developer.model.OAuth2ClientDto;
import com.idp.developer.model.OAuth2ClientUpdateDto;
import com.idp.developer.service.AuthorizationServerService;
import com.idp.developer.service.OAuth2ClientService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
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

    private final Map<Long, Div> clientRows = new HashMap<>();
    private final Map<Long, String> selectedClients = new HashMap<>();

    public ClientsView(AuthorizationServerService authorizationServerService, OAuth2ClientService oAuth2ClientService) {

        setSpacing(true);
        setPadding(true);

        add(new H1("OAuth2 Clients"));

        AuthorizationServerDto authorizationServer = authorizationServerService.getAuthorizationServer();

        Button deleteButton = new Button("Elimina selezionati");
        deleteButton.addClickListener(event -> showDeleteConfirmation(
                deleteButton,
                oAuth2ClientService
        ));
        deleteButton.setEnabled(false);

        for (OAuth2ClientDto oAuth2ClientDto : oAuth2ClientService.getAllOAuth2Clients()) {
            add(createClientRow(authorizationServer, oAuth2ClientService, oAuth2ClientDto, deleteButton));
        }

        add(deleteButton);
    }

    private void showDeleteConfirmation(
            Button deleteButton,
            OAuth2ClientService oAuth2ClientService) {

        ConfirmDialog dialog = getConfirmDialog();

        dialog.addConfirmListener(event -> {
            try {
                Set<Long> clientsIds = Set.copyOf(selectedClients.keySet());
                Collection<String> clientNames = List.copyOf(selectedClients.values());

                oAuth2ClientService.deleteOAuth2Client(clientsIds);
                for (Long id : selectedClients.keySet()) {
                    Div row = clientRows.remove(id);
                    if (row != null) {
                        remove(row);
                    }
                }

                selectedClients.clear();
                deleteButton.setEnabled(false);

                if (clientNames.size() > 1) {
                    log.info("Client {} eliminati", clientNames);
                } else {
                    log.info("Client {} eliminato", clientNames);
                }

                showClientsDeletedNotification(clientNames);
            } catch (Exception e) {
                log.error("Errore durante l'eliminazione dei client", e);
                showClientsDeleteErrorNotification();
            }
        });

        dialog.open();
    }

    private ConfirmDialog getConfirmDialog() {
        int count = selectedClients.size();

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Elimina client");

        String message = (count == 1)
                ? "Sei sicuro di voler eliminare il client selezionato?"
                : "Sei sicuro di voler eliminare i " + count + " client selezionati?";

        dialog.setText(message);

        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");

        dialog.setConfirmText("Elimina");
        dialog.setConfirmButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName() + " " + ButtonVariant.LUMO_PRIMARY.getVariantName());
        return dialog;
    }

    private Div createClientRow(AuthorizationServerDto authorizationServer,
                                OAuth2ClientService oAuth2ClientService,
                                OAuth2ClientDto client,
                                Button deleteButton) {

        Checkbox selected = new Checkbox();
        selected.getElement().setAttribute("aria-label", "Seleziona client " + client.clientId());
        selected.getStyle().set("margin-top", "1.05rem");
        selected.addValueChangeListener(event -> {
            if (Boolean.TRUE.equals(event.getValue())) {
                selectedClients.put(client.id(), client.clientId());
            } else {
                selectedClients.remove(client.id());
            }

            deleteButton.setEnabled(!selectedClients.isEmpty());
        });

        Details details = createDetails(authorizationServer, oAuth2ClientService, client);
        details.setWidthFull();

        Div row = new Div(selected, details);
        row.getStyle().set("display", "flex")
                .set("align-items", "flex-start")
                .set("width", "100%");

        clientRows.put(client.id(), row);

        return row;
    }

    private Details createDetails(AuthorizationServerDto authorizationServer,
                                  OAuth2ClientService oAuth2ClientService,
                                  OAuth2ClientDto oAuth2ClientDto) {

        Span title = new Span(oAuth2ClientDto.clientId());
        title.getStyle().setFontWeight("bold");

        Span subtitle = new Span(oAuth2ClientDto.description());
        subtitle.getStyle()
                .set("font-style", "italic")
                .setFontSize("var(--lumo-font-size-s)");

        Div summary = new Div();
        summary.add(title, subtitle);
        summary.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "0");

        return new Details(
                summary, createClientPanel(authorizationServer, oAuth2ClientService, oAuth2ClientDto, subtitle)
        );
    }

    private FormLayout createClientPanel(AuthorizationServerDto authorizationServerDto,
                                         OAuth2ClientService oAuth2ClientService,
                                         OAuth2ClientDto oAuth2ClientDto,
                                         Span subtitle) {

        FormLayout layout = new FormLayout();

        TextField clientId = readOnlyField("Client ID", oAuth2ClientDto.clientId());

        PasswordField clientSecret = new PasswordField("Client Secret");
        clientSecret.setValue(oAuth2ClientDto.clientSecret());

        TextField clientUrl = new TextField("Client URL");
        clientUrl.setValue(oAuth2ClientDto.clientUrl());

        TextField description = new TextField("Description");
        description.setValue(oAuth2ClientDto.description());
        description.setValueChangeMode(ValueChangeMode.EAGER);
        description.addValueChangeListener(event ->
                subtitle.setText(event.getValue())
        );
        layout.setColspan(description, 2);

        MultiSelectComboBox<String> authenticationMethods = new MultiSelectComboBox<>("Client Authentication Methods");
        authenticationMethods.setItems(authorizationServerDto.supportedAuthenticationMethods());
        authenticationMethods.setValue(new HashSet<>(oAuth2ClientDto.clientAuthenticationMethods()));

        TextArea redirectUris = new TextArea("Redirect URIs");
        redirectUris.setValue(String.join(System.lineSeparator(), oAuth2ClientDto.redirectUris()));

        TextArea postLogoutRedirectUris = new TextArea("Post Logout Redirect URIs");
        postLogoutRedirectUris.setValue(String.join(System.lineSeparator(), oAuth2ClientDto.postLogoutRedirectUris()));

        MultiSelectComboBox<String> scopes = new MultiSelectComboBox<>("Scopes");
        scopes.setItems(authorizationServerDto.supportedScopes());
        scopes.setValue(new HashSet<>(oAuth2ClientDto.scopes()));

        MultiSelectComboBox<String> authorizationGrantTypes = new MultiSelectComboBox<>("Authorization Grant Types");
        authorizationGrantTypes.setItems(authorizationServerDto.supportedGrantTypes());
        authorizationGrantTypes.setValue(new HashSet<>(oAuth2ClientDto.authorizationGrantTypes()));

        Checkbox authorizationConsent = new Checkbox("Authorization Consent");
        authorizationConsent.setValue(oAuth2ClientDto.authorizationConsent());

        Checkbox requireProofKey = new Checkbox("Require Proof Key");
        requireProofKey.setValue(oAuth2ClientDto.requireProofKey());

        Checkbox reuseRefreshTokens = new Checkbox("Reuse Refresh Tokens");
        reuseRefreshTokens.setValue(oAuth2ClientDto.reuseRefreshTokens());

        TextField accessTokenTtl = getDurationField("Access Token TTL (secondi)", oAuth2ClientDto.accessTokenTtl());
        TextField refreshTokenTtl = getDurationField("Refresh Token TTL (secondi)", oAuth2ClientDto.refreshTokenTtl());
        TextField authorizationCodeTtl = getDurationField("Authorization Code TTL (secondi)", oAuth2ClientDto.authorizationCodeTtl());

        Button saveButton = new Button("Salva");
        saveButton.addClickListener(event -> {

            try {
                OAuth2ClientUpdateDto updateDto = new OAuth2ClientUpdateDto(
                        oAuth2ClientDto.id(),
                        oAuth2ClientDto.clientId(),
                        clientSecret.getValue(),
                        description.getValue(),
                        clientUrl.getValue(),
                        parseLines(redirectUris.getValue()),
                        parseLines(postLogoutRedirectUris.getValue()),
                        scopes.getValue(),
                        authorizationGrantTypes.getValue(),
                        authenticationMethods.getValue(),
                        authorizationConsent.getValue(),
                        requireProofKey.getValue(),
                        getDurationFieldValue(accessTokenTtl),
                        getDurationFieldValue(refreshTokenTtl),
                        getDurationFieldValue(authorizationCodeTtl),
                        reuseRefreshTokens.getValue()
                );

                oAuth2ClientService.updateOAuth2Client(updateDto);

                showClientUpdatedNotification(oAuth2ClientDto.clientId());
                log.info("Client [{}] aggiornato", oAuth2ClientDto.clientId());
            } catch (Exception e) {
                log.error("Errore durante l'aggiornamento del client {}", oAuth2ClientDto.clientId(), e);
                showClientUpdateErrorNotification(oAuth2ClientDto.clientId());
            }
        });


        layout.add(
                clientId,
                clientSecret,
                description,
                clientUrl,
                redirectUris,
                postLogoutRedirectUris,
                new Span(),
                scopes,
                authenticationMethods,
                authorizationGrantTypes,
                new Span(),
                authorizationConsent,
                requireProofKey,
                reuseRefreshTokens,
                new Span(),
                accessTokenTtl,
                refreshTokenTtl,
                authorizationCodeTtl,
                new Span(),
                saveButton
        );

        return layout;
    }

    private void showClientsDeletedNotification(Collection<String> clientsIds) {
        Span message = new Span();
        message.add("Client ");

        Span clientName = new Span(String.join(", ", clientsIds));
        clientName.getStyle().setFontWeight("bold");

        message.add(clientName);
        if (clientsIds.size() > 1) {
            message.add(" eliminati");
        } else {
            message.add(" eliminato");
        }

        openNotificationElement(message);
    }

    private void showClientsDeleteErrorNotification() {
        Span message = new Span("Errore durante l'eliminazione dei client");
        openNotificationElement(message).open();
    }

    private void showClientUpdatedNotification(String name) {
        Span message = new Span();
        message.add("Client ");

        Span clientName = new Span(name);
        clientName.getStyle().setFontWeight("bold");

        message.add(clientName);
        message.add(" aggiornato");

        openNotificationElement(message).open();
    }

    private void showClientUpdateErrorNotification(String name) {
        Span message = new Span();
        message.add("Errore durante l'aggiornamento del client ");

        Span clientName = new Span(name);
        clientName.getStyle().setFontWeight("bold");

        message.add(clientName);

        openNotificationElement(message).open();
    }

}