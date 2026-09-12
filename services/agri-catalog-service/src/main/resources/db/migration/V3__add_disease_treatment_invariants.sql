ALTER TABLE product_disease_treatments
ADD CONSTRAINT chk_product_disease_treatment_priority
CHECK (priority >= 0);

CREATE INDEX idx_treatments_product
ON product_disease_treatments (product_id, priority, created_at)
WHERE deleted_at IS NULL;
