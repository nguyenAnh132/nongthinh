CREATE INDEX idx_disease_review_histories_created_at
ON disease_review_histories (created_at DESC, id DESC);

CREATE INDEX idx_diseases_brand_source
ON diseases (brand_id, id)
WHERE created_source = 'BRAND';
