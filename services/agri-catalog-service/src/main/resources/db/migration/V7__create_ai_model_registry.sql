CREATE TABLE ai_models (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    task_type VARCHAR(50) NOT NULL,
    crop_coverage_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by UUID NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    CONSTRAINT chk_ai_models_task_type
        CHECK (task_type IN ('DISEASE_DETECTION')),
    CONSTRAINT chk_ai_models_crop_coverage_type
        CHECK (crop_coverage_type IN ('SELECTED_CROPS', 'ALL_CROPS')),
    CONSTRAINT chk_ai_models_status
        CHECK (status IN ('DRAFT', 'ACTIVE', 'RETIRED'))
);

CREATE TABLE ai_model_crops (
    model_id UUID NOT NULL REFERENCES ai_models(id) ON DELETE RESTRICT,
    crop_type_id UUID NOT NULL REFERENCES crop_types(id) ON DELETE RESTRICT,
    PRIMARY KEY (model_id, crop_type_id)
);

CREATE INDEX idx_ai_models_status
    ON ai_models (status)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_ai_model_crops_crop_type
    ON ai_model_crops (crop_type_id);
