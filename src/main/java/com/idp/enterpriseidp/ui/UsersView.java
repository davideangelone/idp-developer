package com.idp.enterpriseidp.ui;

import com.idp.enterpriseidp.properties.ConfigProperties;
import com.idp.enterpriseidp.properties.UserProperties;
import com.idp.enterpriseidp.utils.UserUtils;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Users | Enterprise IDP")
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