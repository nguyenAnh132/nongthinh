CREATE TABLE ai_model_versions (
    id UUID PRIMARY KEY,
    model_id UUID NOT NULL REFERENCES ai_models(id) ON DELETE RESTRICT,
    version VARCHAR(128) NOT NULL,
    artifact_file_id UUID NOT NULL,
    artifact_sha256 VARCHAR(64) NOT NULL,
    input_width INTEGER NOT NULL,
    input_height INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    validation_report JSONB,
    created_at TIMESTAMPTZ NOT NULL,
    created_by UUID NOT NULL,
    validated_at TIMESTAMPTZ,
    validated_by UUID,
    retired_at TIMESTAMPTZ,
    retired_by UUID,
    CONSTRAINT uq_ai_model_versions_model_version UNIQUE (model_id, version),
    CONSTRAINT chk_ai_model_versions_status
        CHECK (status IN ('DRAFT', 'VALIDATING', 'VALIDATED', 'READY', 'ACTIVE', 'RETIRED')),
    CONSTRAINT chk_ai_model_versions_sha256
        CHECK (artifact_sha256 ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_ai_model_versions_input_dimensions
        CHECK (input_width > 0 AND input_height > 0)
);

CREATE TABLE ai_model_version_classes (
    id UUID PRIMARY KEY,
    model_version_id UUID NOT NULL REFERENCES ai_model_versions(id) ON DELETE RESTRICT,
    class_index INTEGER NOT NULL,
    class_code VARCHAR(100) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    class_kind VARCHAR(30) NOT NULL,
    CONSTRAINT uq_ai_model_version_classes_index UNIQUE (model_version_id, class_index),
    CONSTRAINT uq_ai_model_version_classes_code UNIQUE (model_version_id, class_code),
    CONSTRAINT chk_ai_model_version_classes_index CHECK (class_index >= 0),
    CONSTRAINT chk_ai_model_version_classes_kind CHECK (class_kind IN ('DISEASE', 'HEALTHY'))
);

CREATE TABLE ai_model_deployments (
    id UUID PRIMARY KEY,
    model_version_id UUID NOT NULL REFERENCES ai_model_versions(id) ON DELETE RESTRICT,
    crop_type_id UUID REFERENCES crop_types(id) ON DELETE RESTRICT,
    deployment_scope VARCHAR(30) NOT NULL,
    priority INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by UUID NOT NULL,
    activated_at TIMESTAMPTZ,
    activated_by UUID,
    CONSTRAINT chk_ai_model_deployments_scope
        CHECK ((deployment_scope = 'CROP' AND crop_type_id IS NOT NULL)
            OR (deployment_scope = 'ALL_CROPS' AND crop_type_id IS NULL)),
    CONSTRAINT chk_ai_model_deployments_status
        CHECK (status IN ('INACTIVE', 'ACTIVE', 'RETIRED')),
    CONSTRAINT chk_ai_model_deployments_priority CHECK (priority >= 0)
);

CREATE INDEX idx_ai_model_versions_model_created_at
    ON ai_model_versions (model_id, created_at DESC);

CREATE INDEX idx_ai_model_version_classes_model_version
    ON ai_model_version_classes (model_version_id, class_index);

CREATE INDEX idx_ai_model_deployments_active_resolver
    ON ai_model_deployments (crop_type_id, priority)
    WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX uq_ai_model_deployments_active_crop_priority
    ON ai_model_deployments (crop_type_id, priority)
    WHERE status = 'ACTIVE' AND deployment_scope = 'CROP';

CREATE UNIQUE INDEX uq_ai_model_deployments_active_all_crops_priority
    ON ai_model_deployments (priority)
    WHERE status = 'ACTIVE' AND deployment_scope = 'ALL_CROPS';
