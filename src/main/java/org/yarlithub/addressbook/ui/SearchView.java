package org.yarlithub.addressbook.ui;

import com.vaadin.data.util.sqlcontainer.SQLContainer;

import com.vaadin.ui.*;
import org.yarlithub.addressbook.YITApplication;
import org.yarlithub.addressbook.data.DatabaseHelper;
import org.yarlithub.addressbook.data.SearchFilter;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class SearchView extends Panel {

    private TextField tf;
    private NativeSelect fieldToSearch;
    private CheckBox saveSearch;
    private TextField searchName;
    private YITApplication app;

    public SearchView(final YITApplication app) {
        this.app = app;
        addStyleName("view");

        setCaption("Search contacts");
        setSizeFull();

        /* Use a FormLayout as main layout for this Panel */
        FormLayout formLayout = new FormLayout();
        setContent(formLayout);

        /* Create UI components */
        tf = new TextField("Search term");
        fieldToSearch = new NativeSelect("Field to search");
        saveSearch = new CheckBox("Save search");
        searchName = new TextField("Search name");
        searchName.setVisible(false);
        Button search = new Button("Search");

        /* Initialize fieldToSearch */
        for (int i = 0; i < DatabaseHelper.NATURAL_COL_ORDER.length; i++) {
            fieldToSearch.addItem(DatabaseHelper.NATURAL_COL_ORDER[i]);
            fieldToSearch.setItemCaption(DatabaseHelper.NATURAL_COL_ORDER[i],
                    DatabaseHelper.COL_HEADERS_ENGLISH[i]);
        }
        fieldToSearch.setValue("lastname");
        fieldToSearch.setNullSelectionAllowed(false);

        /* Pre-select first field */
        fieldToSearch.select(fieldToSearch.getItemIds().iterator().next());

        /* Initialize save checkbox */
        saveSearch.setValue(false);
        saveSearch.setImmediate(true);

        saveSearch.addValueChangeListener(event -> {
            searchName.setVisible(saveSearch.getValue());
        });
    /*    saveSearch.addListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {

            }
        });*/

        search.addClickListener(event -> performSearch());

        /* Add all the created components to the form */
        VerticalLayout layout = new VerticalLayout();

        layout.addComponent(tf);
        layout.addComponent(fieldToSearch);
        layout.addComponent(saveSearch);
        layout.addComponent(searchName);
        layout.addComponent(search);

        setContent(layout);


        /* Focus the search term field. */
        tf.focus();
    }

    private void performSearch() {
        String searchTerm = (String) tf.getValue();
        if (searchTerm == null || searchTerm.equals("")) {
            Notification.show("Search term cannot be empty!", Notification.Type.WARNING_MESSAGE);
            return;
        }
        List<SearchFilter> searchFilters = new ArrayList<>();

        if (!"cityId".equals(fieldToSearch.getValue())) {
            /* If this is NOT a City search, one filter is enough. */
            searchFilters.add(new SearchFilter(fieldToSearch.getValue(),
                    searchTerm, (String) searchName.getValue(), fieldToSearch
                            .getItemCaption(fieldToSearch.getValue()),
                    searchTerm));
        } else {
            SQLContainer cc = app.getDbHelp().getCityContainer();
            /*
             * If the city column is searched, the filtering becomes a bit more
             * complicated: We need to first find all the search hits from the
             * city container, create search filters for all their ID's and
             * finally use these filters to search the person container (which
             * only holds a foreign key reference to an id of a city).
             */
            cc.addContainerFilter("name", searchTerm, true, false);
            for (Object cityItemId : cc.getItemIds()) {
                searchFilters.add(new SearchFilter("cityId",
                        cc.getItem(cityItemId).getItemProperty("id").getValue()
                                .toString(), (String) searchName.getValue(),
                        fieldToSearch.getItemCaption(fieldToSearch.getValue()),
                        searchTerm));
            }
            cc.removeAllContainerFilters();
            /*
             * If the search does not find any matched in the cities container,
             * we show a notification at this point. It would not make sense to
             * continue since obviously there will be no results from the person
             * container.
             */
            if (searchFilters.isEmpty()) {
                Notification.show(
                        "No matches found for \'"
                                + searchTerm
                                + "\' in "
                                + fieldToSearch.getItemCaption(fieldToSearch
                                .getValue()));
            }
        }

        /* If Save is checked, save the search through the main app. */
        if (saveSearch.getValue()) {
            if (searchName.getValue() == null
                    || searchName.getValue().equals("")) {
                Notification.show(
                        "Please enter a name for your search!",
                        Notification.Type.WARNING_MESSAGE);
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
