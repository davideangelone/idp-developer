package com.idp.developer.ui;

import com.idp.developer.model.AuthorizationServerDto;
import com.idp.developer.service.AuthorizationServerService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

@Route(value = "configuration", layout = MainLayout.class)
@PageTitle("Configuration | Developer IDP")
@Slf4j
public class ConfigurationView extends AnonymousVerticalLayout {

    private static final String LOGIN_FREE = "Free";
    private static final String LOGIN_DEFAULT = "Default";

    public ConfigurationView(AuthorizationServerService authorizationServerService) {

        setSpacing(true);
        setPadding(true);

        add(new H1("Configuration"));

        AuthorizationServerDto authorizationServer = authorizationServerService.getAuthorizationServer();

        FormLayout configurationForm = new FormLayout();

        RadioButtonGroup<Boolean> freeLogin = new RadioButtonGroup<>("Login");
        freeLogin.setItems(true, false);
        freeLogin.setItemLabelGenerator(value -> Boolean.TRUE.equals(value) ? LOGIN_FREE : LOGIN_DEFAULT);
        freeLogin.setValue(authorizationServer.freeLogin());

        configurationForm.add(
                readOnlyField("Issuer", authorizationServer.issuerUrl()),
                readOnlyField("Supported authentication methods", authorizationServer.supportedAuthenticationMethods()),
                readOnlyField("Supported scopes", authorizationServer.supportedScopes()),
                readOnlyField("Supported grant types", authorizationServer.supportedGrantTypes()),
                readOnlyField("Supported roles", authorizationServer.supportedRoles()),
                readOnlyField("Supported groups", authorizationServer.supportedGroups()),
                freeLogin
        );

        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        saveButton.addClickListener(event -> {
            try {
                boolean freeLoginValue = freeLogin.getValue();

                authorizationServerService.updateFreeLogin(freeLoginValue);

                openConfigurationUpdatedNotification();

                log.info("Configurazione Login aggiornata. Free login: {}", freeLoginValue);

            } catch (Exception e) {
                log.error("Errore durante l'aggiornamento della configurazione", e);
                openConfigurationUpdateErrorNotification();
            }
        });

        VerticalLayout form = new VerticalLayout();
        form.add(configurationForm, saveButton);

        add(form);
    }

    private void openConfigurationUpdatedNotification() {
        openNotificationElement(new Span("Configurazione aggiornata"));
    }

    private void openConfigurationUpdateErrorNotification() {
        openNotificationElement(new Span("Errore durante l'aggiornamento della configurazione"));
    }

}