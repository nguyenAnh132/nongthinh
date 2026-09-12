CREATE UNIQUE INDEX uq_product_image_file_active
ON product_images (file_id)
WHERE deleted_at IS NULL
  AND file_id IS NOT NULL;

CREATE UNIQUE INDEX uq_product_image_primary_active
ON product_images (product_id)
WHERE deleted_at IS NULL
  AND is_primary = TRUE;
