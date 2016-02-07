package org.yarlithub.addressbook.ui;


import com.vaadin.ui.Tree;
import org.yarlithub.addressbook.YITApplication;
import org.yarlithub.app.AddressBookMainView;

@SuppressWarnings("serial")
public class NavigationTree extends Tree {
    public static final Object SHOW_ALL = "Show all";
    public static final Object SEARCH = "Search";

    public NavigationTree(AddressBookMainView app) {
        addItem(SHOW_ALL);
        addItem(SEARCH);

        setChildrenAllowed(SHOW_ALL, false);

        /*
         * We want items to be selectable but do not want the user to be able to
         * de-select an item.
         */
        setSelectable(true);
        setNullSelectionAllowed(false);

        // Make application handle item click events
        addItemClickListener(app);
    }
}
