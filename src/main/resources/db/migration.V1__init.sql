-- ✅ 1. users first (no dependencies)
CREATE TABLE users (
                       id       BIGSERIAL PRIMARY KEY,
                       role     VARCHAR(80)  NOT NULL DEFAULT 'USER',
                       username VARCHAR(30)  NOT NULL UNIQUE,
                       password VARCHAR(255),          -- ✅ safe for BCrypt
                       email    VARCHAR(90)  UNIQUE,
                       created  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ✅ 2. companies depends on users
CREATE TABLE companies (
                           id               BIGSERIAL PRIMARY KEY,
                           user_id          BIGINT       NOT NULL,
                           company_name     VARCHAR(255) NOT NULL,
                           industry         VARCHAR(255) NOT NULL,
                           company_age_years INT         NOT NULL,
                           created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_company_user
                               FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ✅ 3. reports depends on companies
CREATE TABLE reports (
                         id               BIGSERIAL PRIMARY KEY,
                         company_id       BIGINT        NOT NULL,
                         report_month     DATE          NOT NULL,
                         monthly_revenue  NUMERIC(14,2) NOT NULL,
                         current_payments NUMERIC(14,2) DEFAULT 0,
                         late_payments_count INT        DEFAULT 0,
                         created_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_fin_company
                             FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
                         CONSTRAINT unique_company_month
                             UNIQUE (company_id, report_month)
);

-- ✅ 4. applications depends on reports only (company_id removed — redundant)
CREATE TABLE applications (
                              id            BIGSERIAL PRIMARY KEY,
                              report_id     BIGINT        NOT NULL,    -- ✅ renamed from financial_id, clearer
                              loan_amount   NUMERIC(14,2) NOT NULL,
                              interest_rate NUMERIC(5,2)  NOT NULL,
                              term_months   INT           NOT NULL,
                              created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_app_report
                                  FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE
);

-- ✅ 5. scoring depends on applications
CREATE TABLE scoring (
                         id             BIGSERIAL PRIMARY KEY,
                         application_id BIGINT        NOT NULL UNIQUE,
                         new_payment    NUMERIC(14,2),
                         debt_load      NUMERIC(10,4),
                         dscr           NUMERIC(10,4),
                         rating         TEXT,
                         decision       TEXT,
                         created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_scoring_app
                             FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

-- ✅ 6. ai_analysis depends on applications
CREATE TABLE ai_analysis (
                             id               BIGSERIAL PRIMARY KEY,
                             application_id   BIGINT NOT NULL UNIQUE,   -- ✅ UNIQUE: one AI result per application
                             risk_level       VARCHAR(30),
                             summary          TEXT,
                             strengths        TEXT,
                             weaknesses       TEXT,
                             metric_analysis  TEXT,
                             stop_factors     TEXT,
                             recommendation   TEXT,
                             reasoning        TEXT,
                             confidence_score INT,
                             raw_response     TEXT,
                             created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_ai_app
                                 FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

-- ✅ 7. refresh_token depends on users
CREATE TABLE refresh_token (
                               id         BIGSERIAL PRIMARY KEY,
                               token      TEXT      NOT NULL UNIQUE,     -- ✅ TEXT: no length issues
                               user_id    BIGINT    NOT NULL,
                               expires_at TIMESTAMP NOT NULL,            -- ✅ added: token expiry
                               created    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_refresh_token_user
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);