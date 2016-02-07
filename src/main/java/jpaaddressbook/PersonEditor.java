package jpaaddressbook;

import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.ui.*;
import jpaaddressbook.domain.Person;

import java.io.Serializable;
import java.lang.reflect.Method;

@SuppressWarnings("serial")
public class PersonEditor extends VerticalLayout {

    public static final Logger logger = LoggerFactory.getLogger(PersonEditor.class);
    private final Item personItem;
    private FieldGroup binder;
    private FormLayout form;


    public PersonEditor(Item personItem) {
//        setModal(true);
        setWidth("400px");
        this.personItem = personItem;
        form = new FormLayout();
        form.setMargin(true);
        form.setSpacing(true);
        binder = new FieldGroup(personItem);
        binder.setBuffered(true);
        binder.setItemDataSource(personItem);

        Button saveButton = new Button("Save", event -> {
            try {
                binder.commit();
                fireEvent(new EditorSavedEvent(form, personItem));
            } catch (FieldGroup.CommitException e) {
                e.printStackTrace();
            }
        });

        Button cancelButton = new Button("Cancel", event -> {
            binder.discard();
            HorizontalSplitPanel parent = (HorizontalSplitPanel) this.getParent();
            parent.setSplitPosition(0, Unit.PERCENTAGE);
        });

        setCaption(buildCaption());


        TextField firstName = new TextField("First Name");
        firstName.setNullRepresentation("");
        TextField lastName = new TextField("Last Name");
        lastName.setNullRepresentation("");
        TextField phoneNumber = new TextField("Phone Number");
        phoneNumber.setNullRepresentation("");
        TextArea address = new TextArea("Address");
        address.setNullRepresentation("");
        TextField email = new TextField("Email");
        email.setNullRepresentation("");
        TextField profession = new TextField("Profession");
        profession.setNullRepresentation("");
        TextField affiliation = new TextField("Affiliation");
        affiliation.setNullRepresentation("");
        TextArea description = new TextArea("Description");
        description.setNullRepresentation("");

        binder.bind(firstName, "firstName");
        binder.bind(lastName, "lastName");
        binder.bind(phoneNumber, "phoneNumber");
        binder.bind(address, "address");
        binder.bind(email, "email");
        binder.bind(profession, "profession");
        binder.bind(affiliation, "affiliation");
        binder.bind(description, "description");

        firstName.addValidator(new BeanValidator(Person.class, "firstName"));
        lastName.addValidator(new BeanValidator(Person.class, "lastName"));
        email.addValidator(new BeanValidator(Person.class, "email"));

        form.addComponents(firstName, lastName, phoneNumber, email, profession, address, affiliation, description);

        HorizontalLayout footer = new HorizontalLayout(saveButton, cancelButton);
        footer.setSpacing(true);
        footer.setMargin(true);
        form.addComponent(footer);
        UI.getCurrent().setFocusedComponent(firstName);

        addComponent(form);
    }

    /**
     * @return the caption of the editor window
     */
    private String buildCaption() {
        return String.format("%s %s",
                personItem.getItemProperty("firstName").getValue(),
                personItem.getItemProperty("lastName").getValue());
    }

    // Adding new Listener method
    public void addListener(EditorSavedListener listener) {
        try {
            Method method = EditorSavedListener.class.getDeclaredMethod("editorSaved",
                    new Class[]{EditorSavedEvent.class});
            addListener(EditorSavedEvent.class, listener, method);
        } catch (final NoSuchMethodException e) {
            // This should never happen
            throw new RuntimeException("Internal error, editor saved method not found");
        }
    }

    public void removeListener(EditorSavedListener listener) {
        removeListener(EditorSavedEvent.class, listener);
    }

    public static class EditorSavedEvent extends Component.Event {

        private Item savedItem;

        public EditorSavedEvent(Component source, Item savedItem) {
            super(source);
            this.savedItem = savedItem;
        }

        public Item getSavedItem() {
            return savedItem;
        }
    }

    public interface EditorSavedListener extends Serializable {
        void editorSaved(EditorSavedEvent event);
    }

}
