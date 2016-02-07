USE vaadin;

CREATE TABLE city (
  id      INTEGER AUTO_INCREMENT PRIMARY KEY,
  name    VARCHAR(64),
  version INTEGER DEFAULT 0 NOT NULL
);

CREATE TABLE personaddress (
  id            INTEGER AUTO_INCREMENT PRIMARY KEY,
  firstname     VARCHAR(64),
  lastname      VARCHAR(64),
  email         VARCHAR(64),
  phonenumber   VARCHAR(64),
  streetaddress VARCHAR(128),
  postalcode    INTEGER,
  cityId        INTEGER           NOT NULL,
  version       INTEGER DEFAULT 0 NOT NULL,
  FOREIGN KEY (cityId) REFERENCES city (id)
);


CREATE TABLE person (
  id            INTEGER AUTO_INCREMENT PRIMARY KEY,
  firstname     VARCHAR(64),
  lastname      VARCHAR(64),
  email         VARCHAR(64)
)