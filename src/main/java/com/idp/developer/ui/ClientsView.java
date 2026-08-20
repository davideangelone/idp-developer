package com.idp.developer.ui;

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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

@Route(value = "clients", layout = MainLayout.class)
@PageTitle("OAuth2 Clients | Developer IDP")
@Slf4j
public class ClientsView extends AnonymousVerticalLayout {

    private final Map<Long, Div> clientRows = new HashMap<>();
    private final Map<Long, String> selectedClients = new HashMap<>();
    private final Div clientsContainer = new Div();

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

        Button createButton = new Button("Nuovo client");
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(event -> showCreateClientDialog(
                authorizationServer,
                oAuth2ClientService,
                deleteButton
        ));

        clientsContainer.setWidthFull();

        for (OAuth2ClientDto oAuth2ClientDto : oAuth2ClientService.getAllOAuth2Clients()) {
            clientsContainer.add(createClientRow(authorizationServer, oAuth2ClientService, oAuth2ClientDto, deleteButton));
        }

        add(clientsContainer);

        Div actions = new Div(createButton, deleteButton);
        actions.getStyle()
                .setDisplay(Style.Display.FLEX)
                .setGap("var(--lumo-space-s)");

        add(actions);
    }

    private void showCreateClientDialog(AuthorizationServerDto authorizationServer, OAuth2ClientService service, Button deleteButton) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nuovo client");
        dialog.setWidth("500px");
        dialog.setMaxWidth("90vw");

        TextField clientId = new TextField("Client ID");
        clientId.getStyle().set("overflow", "hidden");
        clientId.setWidthFull();

        Button cancel = new Button("Annulla", event -> dialog.close());

        Button create = new Button("Crea", event -> {
            try {
                String clientIdValue = clientId.getValue().trim();

                if (clientIdValue.isBlank()) {
                    clientId.setInvalid(true);
                    clientId.setErrorMessage("Il Client ID è obbligatorio");
                    return;
                }

                if (service.existsByClientId(clientIdValue)) {
                    clientId.setInvalid(true);
                    clientId.setErrorMessage("Il Client ID esiste già");
                    return;
                }

                OAuth2ClientDto client = service.createOAuth2Client(clientIdValue);
                log.info("Client {} creato con successo", clientIdValue);

                Div row = createClientRow(authorizationServer, service, client, deleteButton);
                clientsContainer.add(row);

                dialog.close();

                openClientCreatedNotification(clientIdValue);

            } catch (Exception e) {
                log.error("Errore durante la creazione del client {}", clientId.getValue(), e);
                clientId.setInvalid(true);
                clientId.setErrorMessage("Client ID già esistente");
            }
        });

        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(clientId);
        dialog.getFooter().add(cancel, create);

        dialog.open();
    }

    private void showDeleteConfirmation(
            Button deleteButton,
            OAuth2ClientService oAuth2ClientService) {

        ConfirmDialog dialog = createConfirmDialog();

        dialog.addConfirmListener(event -> {
            try {
                Set<Long> clientIds = Set.copyOf(selectedClients.keySet());
                List<String> clientNames = List.copyOf(selectedClients.values());

                oAuth2ClientService.deleteOAuth2Client(clientIds);
                for (Long id : clientIds) {
                    Div row = clientRows.remove(id);
                    if (row != null) {
                        clientsContainer.remove(row);
                    }
                }

                selectedClients.clear();
                deleteButton.setEnabled(false);

                log.info("Client {} {}", clientNames, (clientNames.size() > 1) ? "eliminati" : "eliminato");
                openClientsDeletedNotification(clientNames);
            } catch (Exception e) {
                log.error("Errore durante l'eliminazione dei client", e);
                openClientsDeleteErrorNotification();
            }
        });

        dialog.open();
    }

    private ConfirmDialog createConfirmDialog() {
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
        selected.getStyle().setMarginTop("1.05rem");
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
        row.getStyle().setDisplay(Style.Display.FLEX)
                      .setAlignItems(Style.AlignItems.FLEX_START)
                      .setWidth("100%");

        clientRows.put(client.id(), row);

        return row;
    }

    private Details createDetails(AuthorizationServerDto authorizationServer,
                                  OAuth2ClientService oAuth2ClientService,
                                  OAuth2ClientDto oAuth2ClientDto) {

        Span title = new Span(oAuth2ClientDto.clientId());
        title.getStyle().setFontWeight("bold");

        Span subtitle = new Span(getDescription(oAuth2ClientDto.description()));
        subtitle.getStyle()
                .set("font-style", "italic")
                .setFontSize("var(--lumo-font-size-s)");

        Div summary = new Div();
        summary.add(title, subtitle);
        summary.getStyle()
                .setDisplay(Style.Display.FLEX)
                .setFlexDirection(Style.FlexDirection.COLUMN)
                .setGap("0");

        return new Details(
                summary, createClientPanel(authorizationServer, oAuth2ClientService, oAuth2ClientDto, subtitle)
        );
    }

    private String getDescription(String descrizione) {
        return (null == descrizione || descrizione.isBlank())
                ? "Nessuna descrizione"
                : descrizione;
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
                subtitle.setText(getDescription(event.getValue()))
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

                openClientUpdatedNotification(oAuth2ClientDto.clientId());
                log.info("Client [{}] aggiornato", oAuth2ClientDto.clientId());
            } catch (Exception e) {
                log.error("Errore durante l'aggiornamento del client {}", oAuth2ClientDto.clientId(), e);
                openClientUpdateErrorNotification(oAuth2ClientDto.clientId());
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

    private Span getClientSpan() {
        return new Span("Client ");
    }

    private void openClientCreatedNotification(String clientName) {
        Span message = getClientSpan();
        message.add(getClientName(clientName));
        message.add(" creato");
        openNotificationElement(message);
    }

    private void openClientsDeletedNotification(List<String> clientNames) {
        Span message = getClientSpan();
        message.add(getClientName(String.join(", ", clientNames)));
        message.add(clientNames.size() > 1 ? " eliminati" : " eliminato");
        openNotificationElement(message);
    }

    private void openClientsDeleteErrorNotification() {
        Span message = new Span("Errore durante l'eliminazione dei client");
        openNotificationElement(message);
    }

    private void openClientUpdatedNotification(String name) {
        Span message = getClientSpan();
        message.add(getClientName(name));
        message.add(" aggiornato");
        openNotificationElement(message);
    }

    private void openClientUpdateErrorNotification(String name) {
        Span message = new Span();
        message.add("Errore durante l'aggiornamento del client ");
        message.add(getClientName(name));
        openNotificationElement(message);
    }

    private Span getClientName(String name) {
        Span clientName = new Span(name);
        clientName.getStyle().setFontWeight("bold");
        return clientName;
    }

}