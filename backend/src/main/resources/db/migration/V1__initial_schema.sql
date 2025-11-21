-- Users Table (Replaces Django's auth_user)
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    username   VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    is_active  BOOLEAN   DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Profiles Table
CREATE TABLE profiles (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE REFERENCES users (id),
    full_name     VARCHAR(255) NOT NULL,
    job_title     VARCHAR(100) NOT NULL,
    salary        DECIMAL(12, 2),
    gender        VARCHAR(20),
    date_of_birth DATE,
    address       TEXT,
    phone_number  VARCHAR(20),
    bio           TEXT,
    joined_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    department    VARCHAR(100),
    manager_id    BIGINT REFERENCES users (id) -- Self-referencing FK to User
);

-- Bank Holidays
CREATE TABLE bank_holidays (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    date DATE         NOT NULL UNIQUE
);

-- Absence Requests
CREATE TABLE absence_requests (
    id          BIGSERIAL PRIMARY KEY,
    employee_id BIGINT      NOT NULL REFERENCES users (id),
    start_date  DATE        NOT NULL,
    end_date    DATE        NOT NULL,
    reason      TEXT        NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

-- Feedback
CREATE TABLE feedback (
    id            BIGSERIAL PRIMARY KEY,
    profile_id    BIGINT NOT NULL REFERENCES profiles (id), -- The receiver
    author_id     BIGINT NOT NULL REFERENCES users (id),    -- The writer
    text          TEXT   NOT NULL,
    polished_text TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Absence Ledger
CREATE TABLE absence_ledger (
    id                 BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES users (id),
    absence_request_id BIGINT REFERENCES absence_requests (id),
    amount             INT          NOT NULL,
    year               INT          NOT NULL,
    description        VARCHAR(255) NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);