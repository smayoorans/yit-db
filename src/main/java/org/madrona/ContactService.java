package org.madrona;

import org.apache.commons.beanutils.BeanUtils;
import org.madrona.model.Contact;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContactService {

    private static ContactService instance;

    public static ContactService createDemoService() {
        if (instance == null) {

            final ContactService contactService = new ContactService();

            Random r = new Random(0);
            Calendar cal = Calendar.getInstance();

            Contact samantha = new Contact();
            samantha.setFirstName("Samantha");
            samantha.setLastName("Jeyakumar");
            samantha.setEmail(samantha.getFirstName().toLowerCase() + "@gmail.com");
            samantha.setPhone("+ 358 555 " + (100 + r.nextInt(900)));
            cal.set(1930 + r.nextInt(70), r.nextInt(11), r.nextInt(28));
            samantha.setBirthDate(cal.getTime());

            Contact kayu = new Contact();
            kayu.setFirstName("Kayukaran");
            kayu.setLastName("Parameswaran");
            kayu.setEmail(kayu.getFirstName().toLowerCase() + "@gmail.com");
            kayu.setPhone("+ 358 555 " + (100 + r.nextInt(900)));
            cal.set(1930 + r.nextInt(70), r.nextInt(11), r.nextInt(28));
            kayu.setBirthDate(cal.getTime());

            contactService.save(kayu);
            contactService.save(samantha);

            instance = contactService;
        }

        return instance;
    }

    private HashMap<Long, Contact> contacts = new HashMap<>();
    private long nextId = 0;

    public synchronized List<Contact> findAll(String stringFilter) {
        ArrayList arrayList = new ArrayList();
        for (Contact contact : contacts.values()) {
            try {
                boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
                        || contact.toString().toLowerCase()
                        .contains(stringFilter.toLowerCase());
                if (passesFilter) {
                    arrayList.add(contact.clone());
                }
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(ContactService.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
        Collections.sort(arrayList, new Comparator<Contact>() {

            @Override
            public int compare(Contact o1, Contact o2) {
                return (int) (o2.getId() - o1.getId());
            }
        });
        return arrayList;
    }

    public synchronized long count() {
        return contacts.size();
    }

    public synchronized void delete(Contact value) {
        contacts.remove(value.getId());
    }

    public synchronized void save(Contact entry) {
        if (entry.getId() == null) {
            entry.setId(nextId++);
        }
        try {
            entry = (Contact) BeanUtils.cloneBean(entry);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        contacts.put(entry.getId(), entry);
    }
}
