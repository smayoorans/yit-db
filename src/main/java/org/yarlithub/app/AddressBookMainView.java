package org.yarlithub.app;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import org.yarlithub.addressbook.data.SearchFilter;
import org.yarlithub.addressbook.ui.NavigationTree;
import org.yarlithub.addressbook.ui.SearchView;

public class AddressBookMainView extends VerticalLayout implements ComponentContainer, ItemClickEvent.ItemClickListener {

    public static final Logger logger = LoggerFactory.getLogger(AddressBookMainView.class);

    public static final String[] COL_HEADERS_ENGLISH = new String[]{"First name", "Last name", "Phone Number", "Email", "Profession", "Affiliation"};

    public static final Object[] NATURAL_COLUMNS = new Object[]{"firstName", "lastName", "phoneNumber", "email", "profession", "affiliation"};

    private Grid personTable;
    private JPAContainer<Person> persons;
    private final Button add = new Button("Add Contact");
    private final Button search = new Button("Search");
    private final Button edit = new Button("Edit");
    private final Button delete = new Button("Delete");
    private final Button help = new Button("Help");

    private final HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();

    private final NavigationTree sideNavigationTree = new NavigationTree(this);
    private HelpWindow helpWindow = null;
    private SearchView searchView = null;

    public AddressBookMainView() {
        persons = JPAContainerFactory.make(Person.class, JpaAddressbookUI.PERSISTENCE_UNIT);
        buildMainArea();
    }

    private HorizontalLayout createToolbar() {
        add.setIcon(FontAwesome.PLUS);
        search.setIcon(FontAwesome.SEARCH);
        edit.setIcon(FontAwesome.EDIT);
        help.setIcon(FontAwesome.FILE);
        delete.setIcon(FontAwesome.REMOVE);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.addComponents(add, search, edit, delete, help);
        toolbar.setMargin(true);
        toolbar.setSpacing(true);
        toolbar.setStyleName("toolbar");
        return toolbar;
    }


    private void buildMainArea() {
        setSizeFull();
        addComponent(createToolbar());
        addComponent(horizontalSplit);
        setExpandRatio(horizontalSplit, 1);

        horizontalSplit.setSplitPosition(100, Unit.PERCENTAGE);
        VerticalLayout gridView = new VerticalLayout();
        horizontalSplit.setFirstComponent(gridView);

        personTable = new Grid(persons);
        personTable.setSelectionMode(Grid.SelectionMode.SINGLE);
        personTable.setImmediate(true);
        personTable.setSizeFull();

        personTable.setColumns(NATURAL_COLUMNS);

        edit.setEnabled(false);
        delete.setEnabled(false);

        gridView.addComponent(personTable);
        gridView.setSizeFull();

        personTable.addItemClickListener(event1 -> {
            if (event1.getItem() != null) {
                edit.setEnabled(true);
                delete.setEnabled(true);
            }
            if (event1.isDoubleClick()) {
                personTable.select(event1.getItemId());
                Object selectedRow = personTable.getSelectedRow();
                horizontalSplit.setSplitPosition(420, Unit.PIXELS);
                horizontalSplit.setSecondComponent(gridView);
                horizontalSplit.setFirstComponent(new PersonEditor(personTable.getContainerDataSource().getItem(selectedRow)));
            }
        });

        add.addClickListener(event -> {
            final BeanItem<Person> newPersonItem = new BeanItem<>(new Person());
            PersonEditor personEditor = new PersonEditor(newPersonItem);
            personEditor.addListener((PersonEditor.EditorSavedEvent e) -> {
                persons.addEntity(newPersonItem.getBean());
            });

            horizontalSplit.setSplitPosition(420, Unit.PIXELS);
            horizontalSplit.setFirstComponent(personEditor);
            horizontalSplit.setSecondComponent(gridView);
        });

        edit.addClickListener(event -> {
            Object selectedRow = personTable.getSelectedRow();
            horizontalSplit.setFirstComponent(new PersonEditor(personTable.getContainerDataSource().getItem(selectedRow)));
        });

        delete.addClickListener(event -> persons.removeItem(personTable.getSelectedRow()));

        help.addClickListener(event -> {
            if (helpWindow == null) {
                helpWindow = new HelpWindow();
            }
            UI.getCurrent().addWindow(helpWindow);
        });

        search.addClickListener(event -> {
            if (searchView == null) {
                searchView = new SearchView(this);
            }
            horizontalSplit.setSplitPosition(420, Unit.PIXELS);
            horizontalSplit.setSecondComponent(searchView);
            horizontalSplit.setFirstComponent(sideNavigationTree);
            sideNavigationTree.select(NavigationTree.SEARCH);
        });
    }

    @Override
    public void itemClick(ItemClickEvent event) {
        if (event.getSource() == sideNavigationTree) {
            Object itemId = event.getItemId();
            if (itemId != null) {
                if (NavigationTree.SHOW_ALL.equals(itemId)) {
                    /* Clear all filters from person container */
                    persons.removeAllContainerFilters();
                    horizontalSplit.setSplitPosition(10, Unit.PIXELS);
//                    horizontalSplit.setSecondComponent(searchView);
                    horizontalSplit.setFirstComponent(personTable);
//                    sideNavigationTree.select(NavigationTree.SEARCH);
//                    showListView();
                } else if (NavigationTree.SEARCH.equals(itemId)) {
//                    showSearchView();
                    if (searchView == null) {
                        searchView = new SearchView(this);
                    }
                    horizontalSplit.setSplitPosition(420, Unit.PIXELS);
                    horizontalSplit.setSecondComponent(searchView);
                    horizontalSplit.setFirstComponent(sideNavigationTree);
                    sideNavigationTree.select(NavigationTree.SEARCH);
                } else if (itemId instanceof SearchFilter[]) {
                    search((SearchFilter[]) itemId);
                }
            }
        }
    }
    public void search(SearchFilter... searchFilters) {
        if (searchFilters.length == 0) {
            return;
        }

        /* Clear all filters from person container. */
        persons.removeAllContainerFilters();

        /* Build an array of filters */
        Container.Filter[] filters = new Container.Filter[searchFilters.length];
        int ix = 0;
        for (SearchFilter searchFilter : searchFilters) {
            if (Integer.class.equals(persons.getType(searchFilter.getPropertyId()))) {
                try {
                    filters[ix] = new Compare.Equal(searchFilter.getPropertyId(),
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
        persons.addContainerFilter(new Or(filters));
        horizontalSplit.setFirstComponent(personTable);

        Notification.show(
                "Searched for:<br/> "
                        + searchFilters[0].getPropertyIdDisplayName() + " = *"
                        + searchFilters[0].getTermDisplayName()
                        + "*<br/>Found  item(s).",
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
}
