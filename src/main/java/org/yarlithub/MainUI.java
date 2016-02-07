package org.yarlithub;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import org.yarlithub.model.Contact;
import org.yarlithub.ui.ContactForm;

/**
 * Main Root UI
 */
@Title("Yarl IT Hub - Talent Database")
@Theme("mytheme")
@Widgetset("org.madrona.MyAppWidgetset")
public class MainUI extends UI {

    public static final Logger logger = LoggerFactory.getLogger(MainUI.class);

    public TextField filter = new TextField();
    public Grid contactList = new Grid();
    public Button newContact = new Button("New Contact");

    public ContactForm contactForm = new ContactForm();

    public ContactService service = ContactService.createDemoService();


    @Override
    protected void init(VaadinRequest vaadinRequest) {
        logger.info("Initializing main UI component");
        configureComponents();

        buildMainLayout();
    }


    private void configureComponents() {

        newContact.addClickListener(e -> contactForm.edit(new Contact()));

        filter.setInputPrompt("Filter contacts...");
        filter.addTextChangeListener(e -> refreshContacts(e.getText()));

        contactList.setContainerDataSource(new BeanItemContainer<>(Contact.class));
        contactList.setColumnOrder("firstName", "lastName", "email");
        contactList.removeColumn("id");
        contactList.removeColumn("birthDate");
        contactList.removeColumn("phone");
        contactList.setSelectionMode(Grid.SelectionMode.SINGLE);
        contactList.addSelectionListener(e -> contactForm.edit((Contact) contactList.getSelectedRow()));
        refreshContacts();
    }

    private void buildMainLayout() {

        HorizontalLayout actions = new HorizontalLayout(filter, newContact);
        actions.setWidth("100%");
        filter.setWidth("100%");
        actions.setExpandRatio(filter, 1);

        HorizontalLayout logo = new HorizontalLayout();
//        logo.setMargin(true);
//        logo.setSpacing(true);
//        logo.setStyleName("toolbar");
        logo.setWidth("100%");

        //Logo
        Embedded em = new Embedded("", new ThemeResource("images/logo.png"));
        logo.addComponent(em);
        logo.setComponentAlignment(em, Alignment.TOP_CENTER);
        logo.setExpandRatio(em, 1);

        VerticalLayout left = new VerticalLayout(logo, actions, contactList);
        left.setSizeFull();
        contactList.setSizeFull();
        left.setExpandRatio(contactList, 1);

        HorizontalLayout mainLayout = new HorizontalLayout(left, contactForm);
        mainLayout.setSizeFull();
        mainLayout.setExpandRatio(left, 1);

        // Split and allow resizing
        setContent(mainLayout);
    }


    public void refreshContacts() {
        refreshContacts(filter.getValue());
    }

    private void refreshContacts(String stringFilter) {
        contactList.setContainerDataSource(new BeanItemContainer<>(Contact.class, service.findAll(stringFilter)));
        contactForm.setVisible(false);
    }

/*    @WebServlet(urlPatterns = "*//*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = JpaAddressbookUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }*/
}
