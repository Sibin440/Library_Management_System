-- V1: create core tables

CREATE TABLE roles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  password VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE UNIQUE INDEX ux_users_email ON users(email);
CREATE UNIQUE INDEX ux_users_username ON users(username);

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB;

CREATE TABLE book (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(255),
  author VARCHAR(255),
  isbn VARCHAR(255),
  published_date DATE,
  available BOOLEAN DEFAULT TRUE,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE UNIQUE INDEX ux_book_isbn ON book(isbn);

CREATE TABLE loan (
  id BIGINT NOT NULL AUTO_INCREMENT,
  book_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  loan_date DATETIME,
  due_date DATETIME,
  returned_date DATETIME,
  status VARCHAR(50),
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT,
  action VARCHAR(255),
  entity VARCHAR(255),
  entity_id VARCHAR(255),
  details TEXT,
  timestamp DATETIME,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Foreign keys
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id);
ALTER TABLE loan ADD CONSTRAINT fk_loan_book FOREIGN KEY (book_id) REFERENCES book (id);
ALTER TABLE loan ADD CONSTRAINT fk_loan_user FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE audit_log ADD CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (id);
