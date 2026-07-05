CREATE TABLE obligations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    amount NUMERIC NOT NULL,
    currency VARCHAR(3) NOT NULL,
    category VARCHAR(255) NOT NULL,
    recurrence VARCHAR(255),
    next_payment_date DATE NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    obligation_id UUID NOT NULL REFERENCES obligations(id),
    amount NUMERIC NOT NULL,
    currency VARCHAR(3) NOT NULL,
    paid_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_payments_obligation_id ON payments(obligation_id);