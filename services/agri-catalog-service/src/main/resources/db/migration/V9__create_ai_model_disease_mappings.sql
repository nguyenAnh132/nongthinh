CREATE TABLE ai_model_disease_mappings (
    id UUID PRIMARY KEY,
    model_version_class_id UUID NOT NULL REFERENCES ai_model_version_classes(id) ON DELETE RESTRICT,
    crop_type_id UUID NOT NULL REFERENCES crop_types(id) ON DELETE RESTRICT,
    disease_id UUID NOT NULL REFERENCES diseases(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL,
    created_by UUID NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    CONSTRAINT uq_ai_model_disease_mappings_class_crop
        UNIQUE (model_version_class_id, crop_type_id)
);

CREATE INDEX idx_ai_model_disease_mappings_resolver
    ON ai_model_disease_mappings (crop_type_id, model_version_class_id);

CREATE OR REPLACE FUNCTION validate_ai_model_disease_mapping_crop()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM diseases d
        WHERE d.id = NEW.disease_id
          AND d.crop_type_id = NEW.crop_type_id
    ) THEN
        RAISE EXCEPTION 'Mapped disease must belong to the mapping crop type';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validate_ai_model_disease_mapping_crop
BEFORE INSERT OR UPDATE OF disease_id, crop_type_id
ON ai_model_disease_mappings
FOR EACH ROW
EXECUTE FUNCTION validate_ai_model_disease_mapping_crop();
