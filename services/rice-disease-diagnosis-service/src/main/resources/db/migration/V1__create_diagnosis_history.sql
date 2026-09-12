CREATE TABLE diagnosis_histories (
    id UUID PRIMARY KEY,
    farmer_user_id UUID NOT NULL,
    crop_type_id UUID NOT NULL,
    model_id UUID NOT NULL,
    model_version_id UUID NOT NULL,
    model_version VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL,
    result_snapshot JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_diagnosis_histories_status
        CHECK (status IN ('DISEASED', 'HEALTHY', 'UNDETERMINED'))
);

CREATE TABLE farmer_diagnosis_history_locks (
    farmer_user_id UUID PRIMARY KEY
);

CREATE INDEX idx_diagnosis_histories_farmer_created
    ON diagnosis_histories (farmer_user_id, created_at DESC, id DESC);
