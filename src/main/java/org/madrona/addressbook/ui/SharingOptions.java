package org.madrona.addressbook.ui;

import com.vaadin.ui.*;

@SuppressWarnings("serial")
public class SharingOptions extends Window {
    public SharingOptions() {
        /*
         * Make the window modal, which will disable all other components while
         * it is visible
         */
        setModal(true);

        /* Make the sub window 50% the size of the browser window */
        setWidth("50%");
        /*
         * Center the window both horizontally and vertically in the browser
         * window
         */
        center();


        setCaption("Sharing options");
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(new Label(
                "With these setting you can modify contact sharing "
                        + "options. (non-functional, example of modal dialog)"));
        layout.addComponent(new CheckBox("Gmail"));
        layout.addComponent(new CheckBox(".Mac"));
        layout.setMargin(true);
        layout.setSpacing(true);
        Button close = new Button("OK");
        close.addClickListener(event -> SharingOptions.this.close());
        layout.addComponent(close);
        setContent(layout);
    }
}
