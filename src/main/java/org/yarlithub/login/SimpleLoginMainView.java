package org.yarlithub.login;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import org.yarlithub.ui.MainAppView;

public class SimpleLoginMainView extends CustomComponent implements View {

    public static final String NAME = "";

    Label text = new Label();

    Button logout = new Button("Logout", (Button.ClickListener) event -> {

        // "Logout" the user
        getSession().setAttribute("user", null);

        // Refresh this view, should redirect to login view
        getUI().getNavigator().navigateTo(NAME);
    });

    public SimpleLoginMainView() {
        setCompositionRoot(new MainAppView());
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // Get the user name from the session
        String username = String.valueOf(getSession().getAttribute("user"));

        // And show the username
        text.setValue("Hello " + username);
    }
}