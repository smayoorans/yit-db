package org.madrona.addressbook.data;

import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class DatabaseHelper implements Serializable {

    private Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

    /**
     * Natural property order for SQLContainer linked with the PersonAddress
     * database table. Used in tables and forms.
     */
    public static final Object[] NATURAL_COL_ORDER = new Object[]{
            "firstname", "lastname", "email", "phonenumber", "streetaddress", "postalcode", "cityId"};

    /**
     * "Human readable" captions for properties in same order as in
     * NATURAL_COL_ORDER.
     */
    public static final String[] COL_HEADERS_ENGLISH = new String[]{
            "First name", "Last name", "Email", "Phone number",
            "Street Address", "Postal Code", "City"};
    /**
     * JDBC Connection pool and the two SQLContainers connecting to the persons
     * and cities DB tables.
     */
    private JDBCConnectionPool connectionPool = null;
    private SQLContainer personContainer = null;

    private SQLContainer cityContainer = null;
    /**
     * Enable debug mode to output SQL queries to System.out.
     */
    private boolean debugMode = true;

    public DatabaseHelper() {
        initConnectionPool();
        initContainers();
    }

    private void initConnectionPool() {
        try {
            connectionPool = new SimpleJDBCConnectionPool("com.mysql.jdbc.Driver",
                    "jdbc:mysql://localhost:3306/vaadin", "user", "password", 2, 5);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initContainers() {
        try {
            /* TableQuery and SQLContainer for personaddress -table */
            TableQuery q1 = new TableQuery("personaddress", connectionPool);
            q1.setVersionColumn("version");
            personContainer = new SQLContainer(q1);

            /* TableQuery and SQLContainer for city -table */
            TableQuery q2 = new TableQuery("city", connectionPool);
            q2.setVersionColumn("version");
            cityContainer = new SQLContainer(q2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public SQLContainer getPersonContainer() {
        return personContainer;
    }

    public SQLContainer getCityContainer() {
        return cityContainer;
    }

    /**
     * Fetches a city name based on its key.
     *
     * @param cityId Key
     * @return City name
     */
    public String getCityName(int cityId) {
        Object cityItemId = cityContainer.getIdByIndex(cityId);
        return cityContainer.getItem(cityItemId).getItemProperty("name").getValue().toString();
    }

    /**
     * Adds a new city to the container and commits changes to the database.
     *
     * @param cityName Name of the city to add
     * @return true if the city was added successfully
     */
    public boolean addCity(String cityName) {
        cityContainer.getItem(cityContainer.addItem()).getItemProperty("name")
                .setValue(cityName);
        try {
            cityContainer.commit();
            return true;
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
