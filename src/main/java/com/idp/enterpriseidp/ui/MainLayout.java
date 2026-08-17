package com.idp.enterpriseidp.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();

        setDrawerOpened(true);
    }

    private void createHeader() {
        H1 title = new H1("Enterprise IDP");

        title.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("margin", "0");

        Button menuButton = new Button(
                new Icon(VaadinIcon.MENU),
                event -> setDrawerOpened(true)
        );

        HorizontalLayout header = new HorizontalLayout(menuButton, title);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding", "var(--lumo-space-m)");

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav navigation = new SideNav();

        navigation.addItem(
                new SideNavItem(
                        "Dashboard",
                        DashboardView.class,
                        new Icon(VaadinIcon.DASHBOARD))
        );

        navigation.addItem(
                new SideNavItem(
                        "Users",
                        UsersView.class,
                        new Icon(VaadinIcon.USERS))
        );

        navigation.addItem(
                new SideNavItem(
                        "OAuth2 Client",
                        ClientView.class,
                        new Icon(VaadinIcon.KEY))
        );

        navigation.addItem(
                new SideNavItem(
                        "Scopes / Claims",
                        ClaimsView.class,
                        new Icon(VaadinIcon.SITEMAP))
        );

        navigation.addItem(
                new SideNavItem(
                        "Configuration",
                        ConfigurationView.class,
                        new Icon(VaadinIcon.COG))
        );

        addToDrawer(navigation);
    }
}