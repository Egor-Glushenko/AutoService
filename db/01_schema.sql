CREATE TABLE IF NOT EXISTS clients (
    id        SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone     VARCHAR(20)  NOT NULL UNIQUE,
    email     VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS cars (
    id        SERIAL PRIMARY KEY,
    client_id INTEGER     NOT NULL REFERENCES clients (id) ON DELETE RESTRICT,
    brand     VARCHAR(50) NOT NULL,
    model     VARCHAR(50) NOT NULL,
    year      INTEGER     NOT NULL CHECK (year BETWEEN 1950 AND 2100),
    plate     VARCHAR(15) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS mechanics (
    id             SERIAL PRIMARY KEY,
    full_name      VARCHAR(100) NOT NULL,
    specialization VARCHAR(60)  NOT NULL
);

CREATE TABLE IF NOT EXISTS repair_orders (
    id             SERIAL PRIMARY KEY,
    order_number   VARCHAR(20)    NOT NULL UNIQUE,
    car_id         INTEGER        NOT NULL REFERENCES cars (id) ON DELETE RESTRICT,
    mechanic_id    INTEGER        NOT NULL REFERENCES mechanics (id) ON DELETE RESTRICT,
    description    VARCHAR(500)   NOT NULL,
    status         VARCHAR(20)    NOT NULL
        CHECK (status IN ('NEW', 'IN_PROGRESS', 'DONE', 'CANCELLED')),
    created_date   DATE           NOT NULL,
    completed_date DATE,
    cost           NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (cost >= 0),
    CONSTRAINT completed_after_created
        CHECK (completed_date IS NULL OR completed_date >= created_date),
    CONSTRAINT done_has_completed_date
        CHECK (status <> 'DONE' OR completed_date IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_cars_client ON cars (client_id);
CREATE INDEX IF NOT EXISTS idx_orders_car ON repair_orders (car_id);
CREATE INDEX IF NOT EXISTS idx_orders_mechanic ON repair_orders (mechanic_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON repair_orders (status);
