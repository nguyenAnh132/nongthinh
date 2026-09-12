ALTER TABLE post_reports
    ADD CONSTRAINT chk_post_reports_reason_detail_length CHECK (
        reason_detail IS NULL OR char_length(reason_detail) <= 10000
    ) NOT VALID,
    ADD CONSTRAINT chk_post_reports_resolution_note CHECK (
        resolution_note IS NULL OR (
            length(trim(resolution_note)) > 0
            AND char_length(resolution_note) <= 10000
        )
    ) NOT VALID,
    ADD CONSTRAINT chk_post_reports_timestamps CHECK (
        updated_at >= created_at
        AND (
            resolved_at IS NULL
            OR (resolved_at >= created_at AND updated_at >= resolved_at)
        )
    ) NOT VALID;

UPDATE post_reports
SET resolution_note = NULL
WHERE resolution_note IS NOT NULL
  AND length(trim(resolution_note)) = 0;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM post_reports
        WHERE reason_detail IS NOT NULL
          AND char_length(reason_detail) > 10000
    ) THEN
        ALTER TABLE post_reports
            VALIDATE CONSTRAINT chk_post_reports_reason_detail_length;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM post_reports
        WHERE resolution_note IS NOT NULL
          AND (
              length(trim(resolution_note)) = 0
              OR char_length(resolution_note) > 10000
          )
    ) THEN
        ALTER TABLE post_reports
            VALIDATE CONSTRAINT chk_post_reports_resolution_note;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM post_reports
        WHERE updated_at < created_at
           OR (
               resolved_at IS NOT NULL
               AND (resolved_at < created_at OR updated_at < resolved_at)
           )
    ) THEN
        ALTER TABLE post_reports
            VALIDATE CONSTRAINT chk_post_reports_timestamps;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_post_reports_status_created
    ON post_reports(status, created_at, id);

CREATE INDEX IF NOT EXISTS idx_post_reports_created
    ON post_reports(created_at, id);
