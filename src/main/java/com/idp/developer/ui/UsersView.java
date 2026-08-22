package com.idp.developer.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.idp.developer.model.AuthorizationServerDto;
import com.idp.developer.model.UserDto;
import com.idp.developer.model.UserUpdateDto;
import com.idp.developer.service.AuthorizationServerService;
import com.idp.developer.service.UserService;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Users | Developer IDP")
@Slf4j
public class UsersView extends AnonymousVerticalLayout {

    private final Map<Long, Div> userRows = new HashMap<>();
    private final Map<Long, String> selectedUsers = new HashMap<>();
    private final Div usersContainer = new Div();

    public UsersView(AuthorizationServerService authorizationServerService, UserService userService) {

        setSpacing(true);
        setPadding(true);

        add(new H1("Users"));

        AuthorizationServerDto authorizationServer = authorizationServerService.getAuthorizationServer();

        Button deleteButton = new Button("Elimina selezionati");
        deleteButton.addClickListener(event ->
                showDeleteConfirmation(deleteButton, userService));
        deleteButton.setEnabled(false);

        Button createButton = new Button("Nuovo utente");
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(event -> showCreateUserDialog(
                authorizationServer,
                userService,
                deleteButton
        ));

        usersContainer.setWidthFull();

        for (UserDto user : userService.getAllUsers()) {
            usersContainer.add(createUserRow(authorizationServer, userService, user, deleteButton));
        }

        add(usersContainer);

        Div actions = new Div(createButton, deleteButton);
        actions.getStyle()
                .setDisplay(Style.Display.FLEX)
                .setGap("var(--lumo-space-s)");

        add(actions);
    }

    private void showCreateUserDialog(AuthorizationServerDto authorizationServer, UserService userService, Button deleteButton) {

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nuovo utente");
        dialog.setWidth("500px");
        dialog.setMaxWidth("90vw");

        TextField username = new TextField("Username");
        username.getStyle().set("overflow", "hidden");
        username.setWidthFull();

        Button cancel = new Button("Annulla", event -> dialog.close());

        Button create = new Button("Crea", event -> {
            try {
                String usernameValue = username.getValue().trim();

                if (usernameValue.isBlank()) {
                    username.setInvalid(true);
                    username.setErrorMessage("Username è obbligatorio");
                    return;
                }

                if (userService.existsByUsername(usernameValue)) {
                    username.setInvalid(true);
                    username.setErrorMessage("Username già esistente");
                    return;
                }

                UserDto user = userService.createUser(usernameValue);
                log.info("Utente {} creato con successo", usernameValue);

                Div row = createUserRow(authorizationServer, userService, user, deleteButton);
                usersContainer.add(row);

                dialog.close();

                openUserCreatedNotification(usernameValue);

            } catch (Exception e) {
                log.error("Errore durante la creazione dell'utente {}", username.getValue(), e);
                username.setInvalid(true);
                username.setErrorMessage("Utente già esistente");
            }
        });

        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(username);
        dialog.getFooter().add(cancel, create);

        dialog.open();
    }

    private Div createUserRow(AuthorizationServerDto authorizationServer, UserService userService, UserDto user, Button deleteButton) {

        Checkbox selected = new Checkbox();
        selected.getElement()
                .setAttribute("aria-label", "Seleziona user " + user.username());
        selected.getStyle().setMarginTop("1.05rem");

        selected.addValueChangeListener(event -> {
            if (Boolean.TRUE.equals(event.getValue())) {
                selectedUsers.put(user.id(), user.username());
            } else {
                selectedUsers.remove(user.id());
            }

            deleteButton.setEnabled(!selectedUsers.isEmpty());
        });

        Details details = createDetails(authorizationServer, userService, user);
        details.setWidthFull();

        Div row = new Div(selected, details);
        row.getStyle()
                .setDisplay(Style.Display.FLEX)
                .setAlignItems(Style.AlignItems.FLEX_START)
                .setWidth("100%");

        userRows.put(user.id(), row);

        return row;
    }

    private Details createDetails(AuthorizationServerDto authorizationServer, UserService userService, UserDto user) {

        Span title = new Span(getFullname(user.firstName(), user.lastName(), user.username()));
        title.getStyle().setFontWeight("bold");

        Span subtitle = new Span(user.username());
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
                summary,
                createUserPanel(authorizationServer, userService, user, title)
        );
    }

    private FormLayout createUserPanel(AuthorizationServerDto authorizationServerDto, UserService userService, UserDto user, Span title) {

        FormLayout layout = new FormLayout();

        TextField username = readOnlyField("Username", user.username());

        Checkbox enabled = new Checkbox("Enabled");
        enabled.setValue(user.enabled());

        TextField firstName = new TextField("First Name");
        firstName.setValue(user.firstName());
        firstName.setValueChangeMode(ValueChangeMode.EAGER);

        TextField lastName = new TextField("Last Name");
        lastName.setValue(user.lastName());
        lastName.setValueChangeMode(ValueChangeMode.EAGER);

        firstName.addValueChangeListener(event ->
                title.setText(getFullname(event.getValue(), lastName.getValue(), username.getValue()))
        );
        lastName.addValueChangeListener(event ->
                title.setText(getFullname(firstName.getValue(), event.getValue(), username.getValue()))
        );

        PasswordField password = new PasswordField("Password");
        password.setValue(user.password());

        TextField email = new TextField("Email");
        email.setValue(user.email());

        Checkbox emailVerified = new Checkbox("Email Verified");
        emailVerified.setValue(user.emailVerified());

        TextField address = new TextField("Address");
        address.setValue(user.address());

        TextField phoneNumner = new TextField("Phone Number");
        phoneNumner.setValue(user.phoneNumber());

        MultiSelectComboBox<String> roles = new MultiSelectComboBox<>("Roles");
        roles.setItems(authorizationServerDto.supportedRoles());
        roles.setValue(user.roles());

        MultiSelectComboBox<String> groups = new MultiSelectComboBox<>("Groups");
        groups.setItems(authorizationServerDto.supportedGroups());
        groups.setValue(user.groups());

        Checkbox accountNonExpired = new Checkbox("Account Non Expired");
        accountNonExpired.setValue(user.accountNonExpired());

        Checkbox accountNonLocked = new Checkbox("Account Non Locked");
        accountNonLocked.setValue(user.accountNonLocked());

        Checkbox credentialsNonExpired = new Checkbox("Credentials Non Expired");
        credentialsNonExpired.setValue(user.credentialsNonExpired());

        TextField cretedAt = readOnlyField("Created at", formatInstant(user.createdAt()));
        TextField updatedAt = readOnlyField("Updated at", formatInstant(user.updatedAt()));

        Button saveButton = new Button("Salva");
        saveButton.addClickListener(event -> {

            try {
                UserUpdateDto updateDto = new UserUpdateDto(
                        user.id(),
                        firstName.getValue(),
                        lastName.getValue(),
                        password.getValue(),
                        email.getValue(),
                        emailVerified.getValue(),
                        address.getValue(),
                        phoneNumner.getValue(),
                        roles.getValue(),
                        groups.getValue(),
                        enabled.getValue(),
                        accountNonExpired.getValue(),
                        accountNonLocked.getValue(),
                        credentialsNonExpired.getValue()
                );

                userService.updateUser(updateDto);

                openUsersUpdatedNotification(user.username());
                log.info("Utente [{}] aggiornato", user.username());
            } catch (Exception e) {
                log.error("Errore durante l'aggiornamento dell'utente {}", user.username(), e);
                openUsersUpdateErrorNotification(user.username());
            }
        });

        layout.add(
                username,
                enabled,
                firstName,
                lastName,
                password,
                new Span(),
                email,
                emailVerified,
                address,
                phoneNumner,
                roles,
                groups,
                accountNonExpired,
                accountNonLocked,
                credentialsNonExpired,
                new Span(),
                cretedAt,
                updatedAt,
                saveButton
        );

        return layout;
    }

    private String getFullname(String firstName, String lastName, String username) {
        String fullname = (firstName.trim() + " " + lastName.trim()).trim();
        return fullname.isEmpty() ? username : fullname;
    }

    private Span getUserSpan() {
        return new Span("Utente ");
    }

    private void openUserCreatedNotification(String username) {
        Span message = getUserSpan();
        message.add(getUserName(username));
        message.add(" creato");
        openNotificationElement(message);
    }

    private void openUsersDeletedNotification(List<String> usernames) {
        Span message = getUserSpan();
        message.add(getUserName(String.join(", ", usernames)));
        message.add(usernames.size() > 1 ? " eliminati" : " eliminato");
        openNotificationElement(message);
    }

    private void openUsersDeleteErrorNotification() {
        Span message = new Span("Errore durante l'eliminazione degli utenti");
        openNotificationElement(message);
    }

    private void openUsersUpdatedNotification(String name) {
        Span message = getUserSpan();
        message.add(getUserName(name));
        message.add(" aggiornato");
        openNotificationElement(message);
    }

    private void openUsersUpdateErrorNotification(String name) {
        Span message = new Span();
        message.add("Errore durante l'aggiornamento dell'utente ");
        message.add(getUserName(name));
        openNotificationElement(message);
    }

    private Span getUserName(String name) {
        Span username = new Span(name);
        username.getStyle().setFontWeight("bold");
        return username;
    }

    private void showDeleteConfirmation(
            Button deleteButton,
            UserService userService) {

        ConfirmDialog dialog = createConfirmDialog();

        dialog.addConfirmListener(event -> {

            try {
                Set<Long> userIds = new HashSet<>(selectedUsers.keySet());
                List<String> usernames = List.copyOf(selectedUsers.values());

                userService.deleteUsers(userIds);

                for (Long id : userIds) {
                    Div row = userRows.remove(id);

                    if (row != null) {
                        usersContainer.remove(row);
                    }
                }

                selectedUsers.clear();
                deleteButton.setEnabled(false);

                log.info("{} {} {}", ((usernames.size() > 1) ? "Utenti" : "Utente"), usernames, (usernames.size() > 1) ? "eliminati" : "eliminato");
                openUsersDeletedNotification(usernames);
            } catch (Exception e) {
                log.error("Errore durante l'eliminazione degli utenti", e);
                openUsersDeleteErrorNotification();
            }
        });

        dialog.open();
    }

    private @NonNull ConfirmDialog createConfirmDialog() {
        ConfirmDialog dialog = new ConfirmDialog();

        int count = selectedUsers.size();

        dialog.setHeader("Elimina user");

        dialog.setText(
                count == 1
                        ? "Sei sicuro di voler eliminare lo user selezionato?"
                        : "Sei sicuro di voler eliminare i " + count + " user selezionati?"
        );

        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");

        dialog.setConfirmText("Elimina");
        dialog.setConfirmButtonTheme(ButtonVariant.LUMO_ERROR.getVariantName() + " " + ButtonVariant.LUMO_PRIMARY.getVariantName());
        return dialog;
    }
}