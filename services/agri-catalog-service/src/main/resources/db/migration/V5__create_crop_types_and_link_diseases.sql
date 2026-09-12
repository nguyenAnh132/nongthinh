CREATE TABLE crop_types (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by UUID,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    CONSTRAINT uq_crop_types_code UNIQUE (code)
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM diseases
        WHERE BTRIM(crop_type) = ''
    ) THEN
        RAISE EXCEPTION 'Cannot migrate diseases with blank crop_type';
    END IF;
END $$;

INSERT INTO crop_types (
    id,
    code,
    name,
    active,
    created_at,
    created_by,
    updated_at,
    updated_by
)
SELECT
    (
        SUBSTRING(MD5('crop-type:' || normalized.code), 1, 8) || '-' ||
        SUBSTRING(MD5('crop-type:' || normalized.code), 9, 4) || '-' ||
        SUBSTRING(MD5('crop-type:' || normalized.code), 13, 4) || '-' ||
        SUBSTRING(MD5('crop-type:' || normalized.code), 17, 4) || '-' ||
        SUBSTRING(MD5('crop-type:' || normalized.code), 21, 12)
    )::UUID,
    normalized.code,
    INITCAP(REPLACE(LOWER(normalized.code), '_', ' ')),
    TRUE,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000001',
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000001'
FROM (
    SELECT
        DISTINCT UPPER(REGEXP_REPLACE(BTRIM(crop_type), '[^A-Za-z0-9]+', '_', 'g')) AS code
    FROM diseases
) normalized
ON CONFLICT (code) DO NOTHING;

INSERT INTO crop_types (id, code, name, active, created_at, created_by, updated_at, updated_by)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'RICE', 'Lúa', TRUE, CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000002', 'MANGO', 'Xoài', TRUE, CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000001')
ON CONFLICT (code) DO NOTHING;

ALTER TABLE diseases ADD COLUMN crop_type_id UUID;

UPDATE diseases disease
SET crop_type_id = crop_type.id
FROM crop_types crop_type
WHERE crop_type.code = UPPER(
    REGEXP_REPLACE(BTRIM(disease.crop_type), '[^A-Za-z0-9]+', '_', 'g')
);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM diseases WHERE crop_type_id IS NULL) THEN
        RAISE EXCEPTION 'Cannot link every disease to a crop type';
    END IF;
END $$;

ALTER TABLE diseases ALTER COLUMN crop_type_id SET NOT NULL;
ALTER TABLE diseases
    ADD CONSTRAINT fk_diseases_crop_type
    FOREIGN KEY (crop_type_id) REFERENCES crop_types(id) ON DELETE RESTRICT;

DROP INDEX IF EXISTS uq_disease_slug_crop_active;
CREATE UNIQUE INDEX uq_disease_slug_crop_type_active
    ON diseases (LOWER(slug), crop_type_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_diseases_crop_type
    ON diseases (crop_type_id)
    WHERE deleted_at IS NULL;
