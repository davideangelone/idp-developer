package com.idp.enterpriseidp.ui;

import com.idp.enterpriseidp.properties.AppProperties;
import com.idp.enterpriseidp.properties.UserProperties;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Users | Enterprise IDP")
public class UsersView extends VerticalLayout {

    public UsersView(AppProperties appProperties) {

        Grid<UserProperties> grid = new Grid<>();

        grid.addColumn(user -> user.getFirstName() + " " + user.getLastName()).setHeader("User");
        grid.addColumn(UserProperties::getUsername).setHeader("Username");
        grid.addColumn(UserProperties::getPassword).setHeader("Password");
        grid.addColumn(UserProperties::getRoles).setHeader("Roles");
        grid.addColumn(UserProperties::getGroups).setHeader("Groups");
        grid.addColumn(UserProperties::getEmail).setHeader("Email");

        grid.setItems(appProperties.getUsers());

        add(
                new H1("Users"),
                grid
        );

        setSizeFull();
    }
}