package com.idp.enterpriseidp.ui;

import com.idp.enterpriseidp.model.UserDto;
import com.idp.enterpriseidp.service.UserService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Users | Enterprise IDP")
public class UsersView extends AnonymousVerticalLayout {

    public UsersView(UserService userService) {

        Grid<UserDto> grid = new Grid<>();

        grid.addColumn(UserDto::getFullName).setHeader("User");
        grid.addColumn(UserDto::username).setHeader("Username");
        grid.addColumn(UserDto::password).setHeader("Password");
        grid.addColumn(UserDto::roles).setHeader("Roles");
        grid.addColumn(UserDto::groups).setHeader("Groups");
        grid.addColumn(UserDto::email).setHeader("Email");

        grid.setItems(userService.getAllUsers());

        add(
                new H1("Users"),
                grid
        );

        setSizeFull();
    }
}