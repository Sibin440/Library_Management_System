-- V1: create core tables (PostgreSQL compatible)

CREATE TABLE IF NOT EXISTS roles (
  id BIGSERIAL NOT NULL,
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL NOT NULL,
  email VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  password VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users(email);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_username ON users(username);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS book (
  id BIGSERIAL NOT NULL,
  title VARCHAR(255),
  author VARCHAR(255),
  isbn VARCHAR(255),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS loan (
  id BIGSERIAL NOT NULL,
  book_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  loan_date TIMESTAMP,
  due_date TIMESTAMP,
  returned_date TIMESTAMP,
  status VARCHAR(50),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGSERIAL NOT NULL,
  user_id BIGINT,
  action VARCHAR(255),
  entity VARCHAR(255),
  entity_id VARCHAR(255),
  details TEXT,
  timestamp TIMESTAMP,
  PRIMARY KEY (id)
);

-- Foreign keys
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id);
ALTER TABLE loan ADD CONSTRAINT fk_loan_book FOREIGN KEY (book_id) REFERENCES book (id);
ALTER TABLE loan ADD CONSTRAINT fk_loan_user FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE audit_log ADD CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users (id);
