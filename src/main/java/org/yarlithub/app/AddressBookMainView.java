/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yarlithub.app;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.data.util.BeanItem;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;

public class AddressBookMainView extends VerticalLayout implements ComponentContainer {

    public static final Logger logger = LoggerFactory.getLogger(AddressBookMainView.class);

    public static final String[] COL_HEADERS_ENGLISH = new String[]{"First name", "Last name", "Phone Number", "Street Address"};

    public static final Object[] NATURAL_COLUMNS = new Object[]{"firstName", "lastName", "phoneNumber", "email", "profession", "affiliation"};

    private Grid personTable;
    private JPAContainer<Person> persons;
    private final Button add = new Button("Add Contact");
    private final Button search = new Button("Search");
    private final Button edit = new Button("Edit");
    private final Button delete = new Button("Delete");
    private final Button help = new Button("Help");

    private final HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();

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
        VerticalLayout verticalLayout = new VerticalLayout();
        horizontalSplit.setFirstComponent(verticalLayout);

        personTable = new Grid(persons);
        personTable.setSelectionMode(Grid.SelectionMode.SINGLE);
        personTable.setImmediate(true);
        personTable.setSizeFull();

        personTable.setColumns(NATURAL_COLUMNS);

        edit.setEnabled(false);
        delete.setEnabled(false);

        verticalLayout.addComponent(personTable);
        verticalLayout.setSizeFull();

        personTable.addItemClickListener(event1 -> {
            if (event1.getItem() != null) {
                edit.setEnabled(true);
                delete.setEnabled(true);
            }
            if (event1.isDoubleClick()) {
                personTable.select(event1.getItemId());
                Object selectedRow = personTable.getSelectedRow();
                horizontalSplit.setSplitPosition(420, Unit.PIXELS);
                horizontalSplit.setSecondComponent(verticalLayout);
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
            horizontalSplit.setSecondComponent(verticalLayout);
        });

        edit.addClickListener(event -> {
            Object selectedRow = personTable.getSelectedRow();
            horizontalSplit.setFirstComponent(new PersonEditor(personTable.getContainerDataSource().getItem(selectedRow)));
        });

        delete.addClickListener(event -> persons.removeItem(personTable.getSelectedRow()));
    }
}
