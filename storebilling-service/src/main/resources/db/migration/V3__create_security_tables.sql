-- app_user: "user" is a MySQL 8.4 reserved word, hence app_user.
CREATE TABLE app_user
(
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    username             VARCHAR(50)  NOT NULL,
    email                VARCHAR(150) NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    first_name           VARCHAR(100) NOT NULL,
    last_name            VARCHAR(100) NOT NULL,
    phone                VARCHAR(30)  NULL,
    organization_id      BIGINT       NULL,
    is_all_store_access  BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    is_account_locked    BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_date      DATETIME     NULL,
    created_by           VARCHAR(50)  NOT NULL,
    created_date         DATETIME(6)  NOT NULL,
    updated_by           VARCHAR(50)  NULL,
    updated_date         DATETIME(6)  NULL,
    version              BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_app_user_username UNIQUE (username),
    CONSTRAINT uk_app_user_email UNIQUE (email),
    CONSTRAINT fk_app_user_organization FOREIGN KEY (organization_id) REFERENCES organization (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_app_user_organization_id ON app_user (organization_id);

CREATE TABLE role
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    code           VARCHAR(50)  NOT NULL,
    name           VARCHAR(100) NOT NULL,
    description    VARCHAR(500) NULL,
    is_system_role BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by     VARCHAR(50)  NOT NULL,
    created_date   DATETIME(6)  NOT NULL,
    updated_by     VARCHAR(50)  NULL,
    updated_date   DATETIME(6)  NULL,
    version        BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE permission
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    code         VARCHAR(80)  NOT NULL,
    name         VARCHAR(150) NOT NULL,
    module       VARCHAR(50)  NOT NULL,
    description  VARCHAR(500) NULL,
    created_by   VARCHAR(50)  NOT NULL,
    created_date DATETIME(6)  NOT NULL,
    updated_by   VARCHAR(50)  NULL,
    updated_date DATETIME(6)  NULL,
    version      BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_permission_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE user_role
(
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL,
    role_id      BIGINT      NOT NULL,
    created_by   VARCHAR(50) NOT NULL,
    created_date DATETIME(6) NOT NULL,
    updated_by   VARCHAR(50) NULL,
    updated_date DATETIME(6) NULL,
    version      BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE role_permission
(
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    role_id       BIGINT      NOT NULL,
    permission_id BIGINT      NOT NULL,
    created_by    VARCHAR(50) NOT NULL,
    created_date  DATETIME(6) NOT NULL,
    updated_by    VARCHAR(50) NULL,
    updated_date  DATETIME(6) NULL,
    version       BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES role (id),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permission (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE user_store_access
(
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL,
    store_id     BIGINT      NOT NULL,
    created_by   VARCHAR(50) NOT NULL,
    created_date DATETIME(6) NOT NULL,
    updated_by   VARCHAR(50) NULL,
    updated_date DATETIME(6) NULL,
    version      BIGINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_store_access UNIQUE (user_id, store_id),
    CONSTRAINT fk_user_store_access_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_user_store_access_store FOREIGN KEY (store_id) REFERENCES store (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
