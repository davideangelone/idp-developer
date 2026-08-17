package com.idp.developer.ui;

import com.idp.developer.properties.ConfigProperties;
import com.idp.developer.properties.UserProperties;
import com.idp.developer.utils.UserUtils;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Users | Developer IDP")
public class UsersView extends AnonymousVerticalLayout {

    public UsersView(ConfigProperties configProperties) {

        Grid<UserProperties> grid = new Grid<>();

        grid.addColumn(UserUtils::getFullName).setHeader("User");
        grid.addColumn(UserProperties::getUsername).setHeader("Username");
        grid.addColumn(UserProperties::getPassword).setHeader("Password");
        grid.addColumn(UserProperties::getRoles).setHeader("Roles");
        grid.addColumn(UserProperties::getGroups).setHeader("Groups");
        grid.addColumn(UserProperties::getEmail).setHeader("Email");

        grid.setItems(configProperties.getUsers());

        add(
                new H1("Users"),
                grid
        );

        setSizeFull();
    }
}