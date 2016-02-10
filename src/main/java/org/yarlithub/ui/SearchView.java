package org.yarlithub.ui;


import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.yarlithub.domain.SearchFilter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class SearchView extends Panel {

    private TextField searchTextField;
    private NativeSelect fieldToSearch;
    private CheckBox saveSearch;
    private TextField searchName;
    private MainAppView app;

    public SearchView(final MainAppView app) {
        this.app = app;
        addStyleName("view");

        setCaption("Search contacts");
        setSizeFull();


        /* Use a FormLayout as main layout for this Panel */
        FormLayout formLayout = new FormLayout();
        setContent(formLayout);

        formLayout.setMargin(true);
        formLayout.setSpacing(true);

        /* Create UI components */
        searchTextField = new TextField("Search term");
        fieldToSearch = new NativeSelect("Field to search");
        saveSearch = new CheckBox("Save search");
        searchName = new TextField("Search name");
        searchName.setVisible(false);
        Button searchButton = new Button("Search");
        searchButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        searchButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        /* Initialize fieldToSearch */
        for (int i = 0; i < MainAppView.NATURAL_COLUMNS.length; i++) {
            fieldToSearch.addItem(MainAppView.NATURAL_COLUMNS[i]);
            fieldToSearch.setItemCaption(MainAppView.NATURAL_COLUMNS[i], MainAppView.COL_HEADERS_ENGLISH[i]);
        }
        fieldToSearch.setValue("firstName");
        fieldToSearch.setNullSelectionAllowed(false);

        /* Pre-select first field */
        fieldToSearch.select(fieldToSearch.getItemIds().iterator().next());

        /* Initialize save checkbox */
        saveSearch.setValue(false);
        saveSearch.setImmediate(true);

        saveSearch.addValueChangeListener(event -> {
            searchName.setVisible(saveSearch.getValue());
        });
        saveSearch.addValueChangeListener(event -> searchName.setVisible(saveSearch.getValue()));

        searchButton.addClickListener(event -> performSearch());

        /* Add all the created components to the form */
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);

        layout.addComponent(searchTextField);
        layout.addComponent(fieldToSearch);
        layout.addComponent(saveSearch);
        layout.addComponent(searchName);
        layout.addComponent(searchButton);

        setContent(layout);

        /* Focus the search term field. */
        searchTextField.focus();
    }

    private void performSearch() {
        String searchTerm = searchTextField.getValue();
        if (searchTerm == null || searchTerm.equals("")) {
            Notification.show("Search term cannot be empty!", Notification.Type.WARNING_MESSAGE);
            return;
        }
        List<SearchFilter> searchFilters = new ArrayList<>();

        if (fieldToSearch.getValue() != null) {
            /* If this is NOT a City search, one filter is enough. */
            searchFilters.add(new SearchFilter(fieldToSearch.getValue(),
                    searchTerm, (String) searchName.getValue(), fieldToSearch
                    .getItemCaption(fieldToSearch.getValue()),
                    searchTerm));
        } else {
            /*
             * If the search does not find any matched in the cities container,
             * we show a notification at this point. It would not make sense to
             * continue since obviously there will be no results from the person
             * container.
             */
            if (searchFilters.isEmpty()) {
                Notification.show(
                        "No matches found for \'" + searchTerm + "\' in " + fieldToSearch.getItemCaption(fieldToSearch
                                .getValue()));
            }
        }

        /* If Save is checked, save the search through the main app. */
        if (saveSearch.getValue()) {
            if (searchName.getValue() == null || searchName.getValue().equals("")) {
                Notification.show("Please enter a name for your search!", Notification.Type.WARNING_MESSAGE);
                return;
            }
            SearchFilter[] sf = {};
            app.saveSearch(searchFilters.toArray(sf));
        }
        SearchFilter[] sf = {};
        app.search(searchFilters.toArray(sf));

        /*
         * Clear the save name and check box to prevent multiple unintentional
         * saves of the same search.
         */
        clearSaving();
    }

    private void clearSaving() {
        searchName.setValue("");
        saveSearch.setValue(false);
    }
}
