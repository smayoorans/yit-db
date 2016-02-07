package org.yarlithub.addressbook.ui;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class HelpWindow extends Window {
    private static final String HELP_HTML_SNIPPET = "This is "
            + "an application built during <strong><a href=\""
            + "http://dev.vaadin.com/\">Vaadin</a></strong> "
            + "tutorial. Hopefully it doesn't need any real help."
            + "<br/><br/>This version is modified to demonstrate the"
            + " usage of SQLContainer. In this version the data is"
            + " fetched from database using two separate SQLContainers"
            + " which both use a TableQuery to generate the necessary"
            + " SQL commands.";

    public HelpWindow() {
        setCaption("Address Book help");
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(new Label(HELP_HTML_SNIPPET, ContentMode.HTML));
        setContent(layout);
    }
}
