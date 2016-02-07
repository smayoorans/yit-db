package org.yarlithub.addressbook;


import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate.RowIdChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import org.yarlithub.addressbook.data.DatabaseHelper;
import org.yarlithub.addressbook.data.SearchFilter;
import org.yarlithub.addressbook.ui.*;
import org.yarlithub.app.HelpWindow;

@SuppressWarnings("serial")
@Title("Yarl IT Hub - Talent Database")
@Theme("mytheme")
@Widgetset("org.yarlithub.MyAppWidgetset")
public class YITApplication extends UI implements ValueChangeListener, ItemClickListener,
        QueryDelegate.RowIdChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(YITApplication.class);

    private final NavigationTree sideNavigationTree = new NavigationTree(null);

    private final Button newContact = new Button("Add Contact");
    private final Button search = new Button("Search");

    private final HorizontalSplitPanel mainSplit = new HorizontalSplitPanel();

    // Lazily created UI references
    private ListView listView = null;
    private SearchView searchView = null;
    private PersonList personList = null;
    private PersonForm personForm = null;

    /* Helper class that creates the tables and SQLContainers. */
    private final DatabaseHelper dbHelp = new DatabaseHelper();

    @Override
    protected void init(VaadinRequest request) {
        logger.info("Initializing Yarl IT Hub's Talent Database Application.");
        buildMainLayout();
        setMainComponent(getListView());
        dbHelp.getPersonContainer().addRowIdChangeListener(this);
    }

    private void buildMainLayout() {

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        layout.addComponent(createToolbar());
        layout.addComponent(mainSplit);
        layout.setExpandRatio(mainSplit, 1);

        mainSplit.setSplitPosition(200, Unit.PIXELS);
        mainSplit.setFirstComponent(sideNavigationTree);

        setContent(layout);
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponent(newContact);
        toolbar.addComponent(search);

        search.addClickListener(event -> {
            showSearchView();
            sideNavigationTree.select(NavigationTree.SEARCH);
        });
        newContact.addClickListener(event -> addNewContact());

        search.setIcon(FontAwesome.SEARCH);
        newContact.setIcon(FontAwesome.PLUS);
        toolbar.setMargin(true);
        toolbar.setSpacing(true);
        toolbar.setStyleName("toolbar");
        return toolbar;
    }

    private void setMainComponent(Component c) {
        mainSplit.setSecondComponent(c);
    }

    /*
     * View getters exist so we can lazily generate the views, resulting in
     * faster application startup time.
     */
    private ListView getListView() {
        if (listView == null) {
            personList = new PersonList(this);
            personForm = new PersonForm(this);
            listView = new ListView(personList, personForm);
        }
        return listView;
    }

    private SearchView getSearchView() {
        if (searchView == null) {
            searchView = new SearchView(null);
        }
        return searchView;
    }

    private void showListView() {
        setMainComponent(getListView());
        personList.fixVisibleAndSelectedItem();
    }

    private void showSearchView() {
        setMainComponent(getSearchView());
        personList.fixVisibleAndSelectedItem();
    }

    public void valueChange(ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == personList) {
            Item item = personList.getItem(personList.getValue());
            if (item != personForm.getItemDataSource()) {
                personForm.setItemDataSource(item);
            }
        }
    }

    public void itemClick(ItemClickEvent event) {
        if (event.getSource() == sideNavigationTree) {
            Object itemId = event.getItemId();
            if (itemId != null) {
                if (NavigationTree.SHOW_ALL.equals(itemId)) {
                    /* Clear all filters from person container */
                    getDbHelp().getPersonContainer()
                            .removeAllContainerFilters();
                    showListView();
                } else if (NavigationTree.SEARCH.equals(itemId)) {
                    showSearchView();
                } else if (itemId instanceof SearchFilter[]) {
                    search((SearchFilter[]) itemId);
                }
            }
        }
    }

    private void addNewContact() {
        showListView();
        sideNavigationTree.select(NavigationTree.SHOW_ALL);
        /* Clear all filters from person container */
        getDbHelp().getPersonContainer().removeAllContainerFilters();
        personForm.addContact();
    }

    public void search(SearchFilter... searchFilters) {
        if (searchFilters.length == 0) {
            return;
        }
        SQLContainer c = getDbHelp().getPersonContainer();

        /* Clear all filters from person container. */
        getDbHelp().getPersonContainer().removeAllContainerFilters();

        /* Build an array of filters */
        Filter[] filters = new Filter[searchFilters.length];
        int ix = 0;
        for (SearchFilter searchFilter : searchFilters) {
            if (Integer.class.equals(c.getType(searchFilter.getPropertyId()))) {
                try {
                    filters[ix] = new Equal(searchFilter.getPropertyId(),
                            Integer.parseInt(searchFilter.getTerm()));
                } catch (NumberFormatException nfe) {
                    Notification.show("Invalid search term!");
                    return;
                }
            } else {
                filters[ix] = new Like((String) searchFilter.getPropertyId(),
                        "%" + searchFilter.getTerm() + "%");
            }
            ix++;
        }
        /* Add the filter(s) to the person container. */
        c.addContainerFilter(new Or(filters));
        showListView();

        Notification.show(
                "Searched for:<br/> "
                        + searchFilters[0].getPropertyIdDisplayName() + " = *"
                        + searchFilters[0].getTermDisplayName()
                        + "*<br/>Found " + c.size() + " item(s).",
                Notification.Type.TRAY_NOTIFICATION);
    }

    public void saveSearch(SearchFilter... searchFilter) {
        sideNavigationTree.addItem(searchFilter);
        sideNavigationTree.setItemCaption(searchFilter, searchFilter[0].getSearchName());
        sideNavigationTree.setParent(searchFilter, NavigationTree.SEARCH);
        // mark the saved search as a leaf (cannot have children)
        sideNavigationTree.setChildrenAllowed(searchFilter, false);
        // make sure "Search" is expanded
        sideNavigationTree.expandItem(NavigationTree.SEARCH);
        // select the saved search
        sideNavigationTree.setValue(searchFilter);
    }

    public DatabaseHelper getDbHelp() {
        return dbHelp;
    }

    public void rowIdChange(RowIdChangeEvent event) {
        /* Select the added item and fix the table scroll position */
        personList.select(event.getNewRowId());
        personList.fixVisibleAndSelectedItem();
    }


}
