package com.idp.developer.ui;

import com.idp.developer.service.AuthorizationServerService;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Developer IDP")
public class DashboardView extends AnonymousVerticalLayout {

    private static final String OPENID_CONFIGURATION_URL = "/.well-known/openid-configuration";

    public DashboardView(AuthorizationServerService authorizationServerService) {

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        String openIdConfigurationUrl = authorizationServerService.getAuthorizationServer().issuerUrl() + OPENID_CONFIGURATION_URL;

        H1 title = new H1("IDP for Developer");
        title.getStyle()
                .setFontSize("3rem")
                .setMargin("0");

        H2 subtitle = new H2("OAuth 2.0 / OpenID Connect Authorization Server");
        subtitle.getStyle()
                .setFontSize("1.25rem")
                .setFontWeight("normal")
                .setMarginTop("0.4rem");

        Anchor discoveryLink = new Anchor(openIdConfigurationUrl, "OpenID Connect Discovery");
        discoveryLink.setTarget("_blank");

        Div content = new Div(title, subtitle, discoveryLink);
        content.getStyle()
                .setDisplay(Style.Display.FLEX)
                .setFlexDirection(Style.FlexDirection.COLUMN)
                .setAlignItems(Style.AlignItems.CENTER)
                .setGap("var(--lumo-space-m)")
                .setTextAlign(Style.TextAlign.CENTER)
                .setMarginTop("-20vh");

        add(content);

        getStyle()
                .setAlignItems(Style.AlignItems.CENTER)
                .setJustifyContent(Style.JustifyContent.CENTER);
    }
}