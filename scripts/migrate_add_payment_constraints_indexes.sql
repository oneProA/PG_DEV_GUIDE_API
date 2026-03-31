-- PostgreSQL migration for existing pgdev schema objects.
-- Safe to run multiple times.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_payments_amount_nonnegative'
    ) THEN
        ALTER TABLE payments
            ADD CONSTRAINT chk_payments_amount_nonnegative
            CHECK (amount >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_payment_card_details_installment_month_nonnegative'
    ) THEN
        ALTER TABLE payment_card_details
            ADD CONSTRAINT chk_payment_card_details_installment_month_nonnegative
            CHECK (installment_month >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_cancellations_cancel_amount_positive'
    ) THEN
        ALTER TABLE cancellations
            ADD CONSTRAINT chk_cancellations_cancel_amount_positive
            CHECK (cancel_amount > 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_cancellations_remained_amount_nonnegative'
    ) THEN
        ALTER TABLE cancellations
            ADD CONSTRAINT chk_cancellations_remained_amount_nonnegative
            CHECK (remained_amount >= 0);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_payments_tid
    ON payments(tid);

CREATE INDEX IF NOT EXISTS idx_payments_order_id
    ON payments(order_id);

CREATE INDEX IF NOT EXISTS idx_cancellations_payment_id
    ON cancellations(payment_id);
