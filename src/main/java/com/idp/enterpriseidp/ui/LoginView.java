package com.idp.enterpriseidp.ui;

import com.idp.enterpriseidp.model.UserDto;
import com.idp.enterpriseidp.service.UserService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;

@Route("login")
public class LoginView extends AnonymousVerticalLayout {

    public LoginView(
            UserService userService,
            HttpServletRequest request) {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        add(new H2("Enterprise IDP"));
        add(new H3("Selezionare utente"));

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        Element form = new Element("form")
                .setAttribute("method", "post")
                .setAttribute("action", "/login");

        form.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px")
                .set("width", "350px");

        Element select = new Element("select")
                .setAttribute("id", "user");

        select.getStyle()
                .set("padding", "10px")
                .set("font-size", "16px");

        for (UserDto userDto : userService.getAllUsers()) {
            Element option = new Element("option")
                    .setAttribute("value", userDto.username())
                    .setAttribute("data-password", userDto.password())
                    .setText(userDto.username() + " (" + userDto.getFullName() + ")");
            select.appendChild(option);
        }

        Element username = getHiddenTypeElement()
                .setAttribute("name", "username")
                .setAttribute("id", "username");

        Element password = getHiddenTypeElement()
                .setAttribute("name", "password")
                .setAttribute("id", "password");

        form.appendChild(select);
        form.appendChild(username);
        form.appendChild(password);

        if (csrfToken != null) {
            Element csrf = getHiddenTypeElement()
                    .setAttribute("name", csrfToken.getParameterName())
                    .setAttribute("value", csrfToken.getToken());
            form.appendChild(csrf);
        }

        Element button = new Element("button")
                .setAttribute("type", "submit")
                .setText("Accedi");

        button.getStyle()
                .set("padding", "0.5rem 1rem")
                .set("font-size", "1.25rem")
                .set("line-height", "1.5")
                .set("border", "none")
                .set("border-radius", "0.1rem")
                .set("width", "100%")
                .set("cursor", "pointer")
                .set("color", "#fff")
                .set("background-color", "#06f");

        form.appendChild(button);

        select.executeJs("""
                const updateCredentials = () => {
                    const option =
                        this.options[this.selectedIndex];
                
                    document.getElementById('username').value =
                        option.value;
                
                    document.getElementById('password').value =
                        option.dataset.password;
                };
                
                this.addEventListener(
                    'change',
                    updateCredentials
                );
                
                updateCredentials();
                """);

        getElement().appendChild(form);
    }

    private Element getHiddenTypeElement() {
        return new Element("input").setAttribute("type", "hidden");
    }
}