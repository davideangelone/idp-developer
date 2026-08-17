package com.idp.enterpriseidp.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
public class AnonymousVerticalLayout extends VerticalLayout {

    protected TextField readOnlyField(String label, String value) {
        TextField field = new TextField(label);
        field.setValue(value != null ? value : "");
        field.setReadOnly(true);
        field.setWidthFull();
        return field;
    }
}
