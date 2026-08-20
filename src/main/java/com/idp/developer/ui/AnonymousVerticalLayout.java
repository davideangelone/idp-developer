package com.idp.developer.ui;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
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

    protected TextField readOnlyField(String label, Collection<String> values) {
        return readOnlyField(label, String.join(", ", values));
    }

    protected TextField getDurationField(String label, Duration duration) {
        TextField field = new TextField(label);
        field.setAllowedCharPattern("[0-9]");

        long seconds = duration != null ? duration.toSeconds() : 0;
        field.setValue(String.valueOf(seconds));

        Span description = new Span(formatDuration(duration));
        description.getStyle()
                .set("background-color", "#7f7f7f")
                .set("color", "white")
                .set("padding", "0.25rem 0.5rem")
                .set("border-radius", "5px")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("white-space", "nowrap")
                .set("opacity", "0.85");

        field.setSuffixComponent(description);
        field.setValueChangeMode(ValueChangeMode.EAGER);

        field.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                long newSeconds = event.getValue().isEmpty() ? 0 : Long.parseLong(event.getValue());
                description.setText(formatDuration(Duration.ofSeconds(newSeconds)));
            }
        });

        return field;
    }

    protected Duration getDurationFieldValue(TextField field) {
        return field.getValue().isEmpty() ? Duration.ZERO : Duration.ofSeconds(Long.parseLong(field.getValue()));
    }

    protected List<String> parseLines(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return value.lines()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private static String formatDuration(Duration duration) {
        if (duration == null) {
            return null;
        }

        StringJoiner result = new StringJoiner(", ");

        addPart(result, duration.toDays(), "giorno", "giorni");
        addPart(result, duration.toHoursPart(), "ora", "ore");
        addPart(result, duration.toMinutesPart(), "minuto", "minuti");
        addPart(result, duration.toSecondsPart(), "secondo", "secondi");

        return result.length() == 0 ? "0 secondi" : result.toString();
    }

    private static void addPart(
            StringJoiner result,
            long value,
            String singular,
            String plural) {

        if (value > 0) {
            result.add(value + " " + (value == 1 ? singular : plural));
        }
    }

    protected Notification getNotificationElement(Span message) {
        Notification notification = new Notification();

        Button close = new Button(new Icon(VaadinIcon.CLOSE));
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        close.getStyle()
                .set("position", "absolute")
                .set("top", "0.15rem")
                .set("right", "0.15rem")
                .set("width", "1.5rem")
                .set("height", "1.5rem")
                .set("padding", "0");

        close.addClickListener(event -> notification.close());

        notification.getElement().getStyle()
                .set("position", "relative")
                .set("padding-right", "2rem");

        notification.add(message, close);
        notification.setDuration(5000);

        notification.open();
        return notification;
    }
}
