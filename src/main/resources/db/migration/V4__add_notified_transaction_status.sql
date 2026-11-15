ALTER TABLE transactions
DROP CONSTRAINT transactions_categorization_status_check;

ALTER TABLE transactions
    ADD CONSTRAINT transactions_categorization_status_check
        CHECK (categorization_status IN ('UNCATEGORIZED', 'NOTIFIED', 'CATEGORIZED', 'AUTO_CATEGORIZED'));