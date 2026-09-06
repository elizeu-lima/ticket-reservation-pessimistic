CREATE TABLE event
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(255)                                       NOT NULL,
    date       TIMESTAMP WITH TIME ZONE                           NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE ticket_type
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id           BIGINT                                             NOT NULL,
    name               VARCHAR(100)                                       NOT NULL,
    price              NUMERIC(10, 2)                                     NOT NULL,
    total_quantity     INT                                                NOT NULL CHECK (total_quantity > 0),
    available_quantity INT                                                NOT NULL CHECK (available_quantity >= 0),
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_ticket_type_event FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE,
    CONSTRAINT chk_available_lte_total CHECK (available_quantity <= total_quantity)
);

CREATE INDEX idx_ticket_type_event_id ON ticket_type (event_id);