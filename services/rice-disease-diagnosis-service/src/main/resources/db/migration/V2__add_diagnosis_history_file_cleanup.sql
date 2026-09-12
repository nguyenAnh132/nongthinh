CREATE TABLE diagnosis_history_files (
    diagnosis_history_id UUID NOT NULL REFERENCES diagnosis_histories(id) ON DELETE CASCADE,
    file_id UUID NOT NULL,
    PRIMARY KEY (diagnosis_history_id, file_id)
);

CREATE INDEX idx_diagnosis_history_files_file
    ON diagnosis_history_files (file_id);

INSERT INTO diagnosis_history_files (diagnosis_history_id, file_id)
SELECT history.id, (image -> 'file' ->> 'id')::uuid
FROM diagnosis_histories history
CROSS JOIN LATERAL jsonb_array_elements(history.result_snapshot -> 'images') AS image
WHERE image -> 'file' ->> 'id' IS NOT NULL
ON CONFLICT DO NOTHING;

CREATE TABLE diagnosis_image_cleanup_tasks (
    id UUID PRIMARY KEY,
    diagnosis_history_id UUID NOT NULL,
    file_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMPTZ NULL,
    completed_at TIMESTAMPTZ NULL,
    CONSTRAINT uq_diagnosis_image_cleanup_task_history_file UNIQUE (diagnosis_history_id, file_id),
    CONSTRAINT chk_diagnosis_image_cleanup_tasks_attempts CHECK (attempts >= 0)
);

CREATE INDEX idx_diagnosis_image_cleanup_tasks_pending
    ON diagnosis_image_cleanup_tasks (created_at ASC)
    WHERE completed_at IS NULL;
