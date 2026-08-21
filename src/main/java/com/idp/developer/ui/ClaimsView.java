package com.idp.developer.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.idp.developer.model.OAuth2ClaimDto;
import com.idp.developer.model.OAuth2ScopeDto;
import com.idp.developer.service.OAuth2ClaimService;
import com.idp.developer.service.OAuth2ScopeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

@Route(value = "claims", layout = MainLayout.class)
@PageTitle("Scopes - Claims | Developer IDP")
@Slf4j
public class ClaimsView extends AnonymousVerticalLayout {

    public ClaimsView(OAuth2ClaimService claimService, OAuth2ScopeService scopeService) {

        setSpacing(true);
        setPadding(true);

        add(new H1("Scope / Claims"));

        add(new H2("Always"));

        List<String> alwaysClaims = claimService.getAlwaysClaims()
                .stream()
                .map(OAuth2ClaimDto::name)
                .toList();

        add(readOnlyField(null, alwaysClaims));

        add(new H2("Scoped"));

        List<String> scopes = scopeService.getAllScopes()
                .stream()
                .map(OAuth2ScopeDto::name)
                .toList();

        List<String> availableClaims = claimService.getAllClaims()
                .stream()
                .map(OAuth2ClaimDto::name)
                .filter(item -> !item.isEmpty())
                .toList();

        Map<String, List<OAuth2ClaimDto>> scopedClaimsMap = claimService.getScopedClaimsMap();


        Map<String, MultiSelectComboBox<String>> selections = new HashMap<>();

        Grid<String> grid = new Grid<>();
        grid.addColumn(scope -> scope)
                .setHeader("Scope")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addComponentColumn(scope -> {

                    MultiSelectComboBox<String> claims = new MultiSelectComboBox<>();
                    claims.setItems(availableClaims);
                    claims.setValue(
                            scopedClaimsMap.getOrDefault(scope, List.of())
                                    .stream()
                                    .map(OAuth2ClaimDto::name)
                                    .filter(item -> !item.isEmpty())
                                    .collect(Collectors.toSet())
                    );
                    claims.setWidthFull();

                    selections.put(scope, claims);
                    return claims;

                }).setHeader("Claims")
                .setFlexGrow(1);

        grid.setItems(scopes);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();

        add(grid);

        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        saveButton.addClickListener(event -> {

            Map<String, Set<String>> mappings = selections.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> Set.copyOf(entry.getValue().getValue())
                    ));

            try {
                claimService.updateScopedClaims(mappings);
                openClaimsUpdatedNotification();
            } catch (Exception e) {
                log.error("Errore durante l'aggiornamento della mappatura Scope / Claims", e);
                openClaimsUpdateErrorNotification();
            }
        });

        add(saveButton);
    }

    private void openClaimsUpdatedNotification() {
        Span message = new Span("Mappatura Scope / Claims aggiornata");
        openNotificationElement(message);
    }

    private void openClaimsUpdateErrorNotification() {
        Span message = new Span("Errore durante l'aggiornamento della mappatura Scope / Claims");
        openNotificationElement(message);
    }
}