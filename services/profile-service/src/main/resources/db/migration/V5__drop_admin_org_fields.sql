ALTER TABLE admin_profiles DROP COLUMN IF EXISTS department_id;
ALTER TABLE admin_profiles DROP COLUMN IF EXISTS position_id;

DROP INDEX IF EXISTS idx_admin_profiles_department_id;
DROP INDEX IF EXISTS idx_admin_profiles_position_id;
