CREATE TABLE product_categories (
    id              UUID PRIMARY KEY, -- Khóa chính định danh duy nhất loại sản phẩm.
    parent_id       UUID, -- ID loại sản phẩm cha; NULL nếu đây là danh mục cấp cao nhất.
    name            VARCHAR(150) NOT NULL, -- Tên hiển thị của loại sản phẩm.
    slug            VARCHAR(180) NOT NULL, -- Chuỗi định danh thân thiện dùng trong URL.
    description     TEXT, -- Mô tả chi tiết về loại sản phẩm.

    display_order   INTEGER NOT NULL DEFAULT 0, -- Thứ tự ưu tiên hiển thị của loại sản phẩm.
    is_active       BOOLEAN NOT NULL DEFAULT TRUE, -- Cho biết loại sản phẩm hiện còn được phép sử dụng hay không.

    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Thời điểm bản ghi được tạo.
    created_by      UUID NOT NULL, -- ID người dùng tạo bản ghi.
    updated_at      TIMESTAMPTZ, -- Thời điểm bản ghi được cập nhật gần nhất.
    updated_by      UUID, -- ID người dùng cập nhật bản ghi gần nhất.
    deleted_at      TIMESTAMPTZ, -- Thời điểm bản ghi bị xóa mềm; NULL nghĩa là chưa bị xóa.
    deleted_by      UUID, -- ID người dùng thực hiện xóa mềm.

    CONSTRAINT fk_product_category_parent
        FOREIGN KEY (parent_id)
        REFERENCES product_categories(id),

    CONSTRAINT chk_product_category_not_self_parent
        CHECK (parent_id IS NULL OR parent_id <> id)
);

CREATE TABLE products (
    id                      UUID PRIMARY KEY, -- Khóa chính định danh duy nhất sản phẩm.
    brand_id                UUID NOT NULL, -- ID Brand sở hữu và đăng sản phẩm.
    category_id             UUID NOT NULL, -- ID loại sản phẩm thuộc bảng product_categories.

    name                    VARCHAR(255) NOT NULL, -- Tên thương mại của sản phẩm.
    slug                    VARCHAR(280) NOT NULL, -- Chuỗi định danh thân thiện dùng trong URL sản phẩm.
    sku                     VARCHAR(100), -- Mã sản phẩm nội bộ do Brand quản lý.
    registration_number     VARCHAR(100), -- Số đăng ký lưu hành hoặc số đăng ký liên quan của sản phẩm.

    manufacturer_name       VARCHAR(255), -- Tên đơn vị hoặc doanh nghiệp sản xuất sản phẩm.
    origin_country          VARCHAR(100), -- Quốc gia xuất xứ của sản phẩm.

    short_description       VARCHAR(500), -- Mô tả ngắn dùng khi hiển thị trong danh sách sản phẩm.
    description             TEXT, -- Mô tả chi tiết về sản phẩm.

    ingredients             TEXT, -- Thành phần của sản phẩm được lưu dưới dạng văn bản.
    usage_instruction       TEXT, -- Hướng dẫn sử dụng chung của sản phẩm.
    dosage_instruction      TEXT, -- Hướng dẫn về liều lượng sử dụng sản phẩm.
    safety_instruction      TEXT, -- Hướng dẫn an toàn trong quá trình sử dụng sản phẩm.
    storage_instruction     TEXT, -- Hướng dẫn bảo quản sản phẩm.
    warning                 TEXT, -- Các cảnh báo và lưu ý quan trọng của sản phẩm.

    form                    VARCHAR(100), -- Dạng sản phẩm, ví dụ bột, hạt hoặc dung dịch.
    unit                    VARCHAR(50), -- Đơn vị tính hoặc đơn vị bán của sản phẩm.
    package_specification   VARCHAR(255), -- Quy cách đóng gói của sản phẩm.

    thumbnail_url           TEXT, -- URL ảnh đại diện của sản phẩm.
    purchase_url            TEXT, -- Đường dẫn mua sản phẩm do Brand cung cấp, có thể là trang bán hàng của Brand hoặc đường dẫn tiếp thị liên kết.

    publication_status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT', -- Trạng thái đăng do Brand quản lý: DRAFT, PUBLISHED hoặc UNPUBLISHED.
    moderation_status       VARCHAR(20) NOT NULL DEFAULT 'NORMAL', -- Trạng thái kiểm soát do Admin quản lý: NORMAL hoặc LOCKED.

    moderation_reason       TEXT, -- Lý do Admin khóa hoặc áp dụng biện pháp kiểm soát với sản phẩm.
    locked_at               TIMESTAMPTZ, -- Thời điểm sản phẩm bị Admin khóa.
    locked_by               UUID, -- ID Admin thực hiện khóa sản phẩm.

    published_at            TIMESTAMPTZ, -- Thời điểm Brand đăng công khai sản phẩm.
    unpublished_at          TIMESTAMPTZ, -- Thời điểm Brand chủ động gỡ sản phẩm khỏi trạng thái công khai.

    is_featured             BOOLEAN NOT NULL DEFAULT FALSE, -- Cho biết sản phẩm có được đánh dấu nổi bật hay không.

    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Thời điểm sản phẩm được tạo.
    created_by              UUID NOT NULL, -- ID người dùng tạo sản phẩm.
    updated_at              TIMESTAMPTZ, -- Thời điểm sản phẩm được cập nhật gần nhất.
    updated_by              UUID, -- ID người dùng cập nhật sản phẩm gần nhất.
    deleted_at              TIMESTAMPTZ, -- Thời điểm sản phẩm bị xóa mềm; NULL nghĩa là chưa bị xóa.
    deleted_by              UUID, -- ID người dùng thực hiện xóa mềm sản phẩm.

    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id)
        REFERENCES product_categories(id),

    CONSTRAINT chk_product_publication_status
        CHECK (publication_status IN (
            'DRAFT',
            'PUBLISHED',
            'UNPUBLISHED'
        )),

    CONSTRAINT chk_product_moderation_status
        CHECK (moderation_status IN (
            'NORMAL',
            'LOCKED'
        ))
);

CREATE TABLE product_images (
    id              UUID PRIMARY KEY, -- Khóa chính định danh duy nhất hình ảnh sản phẩm.
    product_id      UUID NOT NULL, -- ID sản phẩm sở hữu hình ảnh.
    file_id         UUID, -- ID file tương ứng trong file-service, nếu có.
    image_url       TEXT NOT NULL, -- URL truy cập hình ảnh sản phẩm.
    alt_text        VARCHAR(255), -- Văn bản thay thế mô tả nội dung hình ảnh.
    display_order   INTEGER NOT NULL DEFAULT 0, -- Thứ tự hiển thị hình ảnh trong danh sách ảnh sản phẩm.
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE, -- Cho biết hình ảnh có phải ảnh đại diện chính của sản phẩm hay không.

    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Thời điểm hình ảnh được tạo.
    created_by      UUID NOT NULL, -- ID người dùng thêm hình ảnh.
    updated_at      TIMESTAMPTZ, -- Thời điểm hình ảnh được cập nhật gần nhất.
    updated_by      UUID, -- ID người dùng cập nhật hình ảnh gần nhất.
    deleted_at      TIMESTAMPTZ, -- Thời điểm hình ảnh bị xóa mềm; NULL nghĩa là chưa bị xóa.
    deleted_by      UUID, -- ID người dùng thực hiện xóa mềm hình ảnh.

    CONSTRAINT fk_product_image_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
);

CREATE TABLE diseases (
    id                      UUID PRIMARY KEY, -- Khóa chính định danh duy nhất bệnh.
    created_source          VARCHAR(20) NOT NULL, -- Nguồn tạo bệnh: ADMIN hoặc BRAND.
    brand_id                UUID, -- ID Brand tạo bệnh; bắt buộc khi created_source là BRAND và NULL khi do ADMIN tạo.

    name                    VARCHAR(255) NOT NULL, -- Tên bệnh.
    slug                    VARCHAR(280) NOT NULL, -- Chuỗi định danh thân thiện dùng trong URL bệnh.
    scientific_name         VARCHAR(255), -- Tên khoa học của bệnh hoặc tác nhân gây bệnh.

    crop_type               VARCHAR(100) NOT NULL, -- Loại cây trồng bị ảnh hưởng bởi bệnh.
    affected_part           VARCHAR(100), -- Bộ phận của cây bị ảnh hưởng, ví dụ lá, thân, rễ hoặc hạt.
    pathogen_type           VARCHAR(50), -- Loại tác nhân hoặc nhóm nguyên nhân gây bệnh.

    short_description       VARCHAR(500), -- Mô tả ngắn dùng khi hiển thị danh sách bệnh.
    description             TEXT, -- Mô tả chi tiết về bệnh.
    symptoms                TEXT, -- Các triệu chứng dùng để nhận biết bệnh.
    causes                  TEXT, -- Nguyên nhân hoặc tác nhân gây bệnh.
    favorable_conditions    TEXT, -- Điều kiện môi trường hoặc canh tác thuận lợi cho bệnh phát triển.
    prevention_method       TEXT, -- Các biện pháp phòng ngừa bệnh.
    treatment_guideline     TEXT, -- Hướng dẫn điều trị hoặc xử lý bệnh.

    thumbnail_url           TEXT, -- URL ảnh đại diện của bệnh.

    review_status           VARCHAR(30) NOT NULL DEFAULT 'DRAFT', -- Trạng thái xét duyệt bệnh: DRAFT, PENDING_REVIEW, APPROVED, REJECTED, NEEDS_REVISION hoặc HIDDEN.
    rejection_reason        TEXT, -- Lý do bệnh bị từ chối hoặc yêu cầu chỉnh sửa.
    submitted_at            TIMESTAMPTZ, -- Thời điểm Brand gửi bệnh để Admin xét duyệt.
    reviewed_at             TIMESTAMPTZ, -- Thời điểm Admin hoàn tất việc xét duyệt bệnh.
    reviewed_by             UUID, -- ID Admin thực hiện xét duyệt bệnh.
    published_at            TIMESTAMPTZ, -- Thời điểm bệnh được công khai trên hệ thống.

    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Thời điểm bản ghi bệnh được tạo.
    created_by              UUID NOT NULL, -- ID người dùng tạo bản ghi bệnh.
    updated_at              TIMESTAMPTZ, -- Thời điểm bản ghi bệnh được cập nhật gần nhất.
    updated_by              UUID, -- ID người dùng cập nhật bản ghi bệnh gần nhất.
    deleted_at              TIMESTAMPTZ, -- Thời điểm bệnh bị xóa mềm; NULL nghĩa là chưa bị xóa.
    deleted_by              UUID, -- ID người dùng thực hiện xóa mềm bệnh.

    CONSTRAINT chk_disease_created_source
        CHECK (created_source IN ('ADMIN', 'BRAND')),

    CONSTRAINT chk_disease_review_status
        CHECK (review_status IN (
            'DRAFT',
            'PENDING_REVIEW',
            'APPROVED',
            'REJECTED',
            'HIDDEN'
        )),

    CONSTRAINT chk_disease_source_brand
        CHECK (
            (created_source = 'ADMIN' AND brand_id IS NULL)
            OR
            (created_source = 'BRAND' AND brand_id IS NOT NULL)
        )
);

CREATE TABLE product_disease_treatments (
    id                      UUID PRIMARY KEY, -- Khóa chính định danh duy nhất quan hệ sản phẩm - bệnh.
    product_id              UUID NOT NULL, -- ID sản phẩm có công dụng đối với bệnh.
    disease_id              UUID NOT NULL, -- ID bệnh mà sản phẩm có thể đặc trị, kiểm soát, phòng ngừa hoặc hỗ trợ.
    brand_id                UUID NOT NULL, -- ID Brand khai báo quan hệ giữa sản phẩm và bệnh.

    effectiveness_level     VARCHAR(20), -- Mức hiệu quả được khai báo: LOW, MEDIUM, HIGH hoặc VERY_HIGH; có thể để NULL.
    priority                INTEGER NOT NULL DEFAULT 0, -- Thứ tự ưu tiên hiển thị sản phẩm đối với bệnh.

    dosage                  VARCHAR(255), -- Liều lượng sử dụng sản phẩm riêng cho bệnh này.
    application_method      TEXT, -- Phương pháp áp dụng sản phẩm để xử lý bệnh.
    application_timing      TEXT, -- Thời điểm phù hợp để sử dụng sản phẩm đối với bệnh.
    frequency_instruction   VARCHAR(255), -- Hướng dẫn về tần suất hoặc khoảng cách giữa các lần sử dụng.
    treatment_note          TEXT, -- Các lưu ý bổ sung khi sử dụng sản phẩm đối với bệnh.

    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Thời điểm quan hệ sản phẩm - bệnh được tạo.
    created_by              UUID NOT NULL, -- ID người dùng tạo quan hệ sản phẩm - bệnh.
    updated_at              TIMESTAMPTZ, -- Thời điểm quan hệ được cập nhật gần nhất.
    updated_by              UUID, -- ID người dùng cập nhật quan hệ gần nhất.
    deleted_at              TIMESTAMPTZ, -- Thời điểm quan hệ bị xóa mềm; NULL nghĩa là chưa bị xóa.
    deleted_by              UUID, -- ID người dùng thực hiện xóa mềm quan hệ.

    CONSTRAINT fk_treatment_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT fk_treatment_disease
        FOREIGN KEY (disease_id)
        REFERENCES diseases(id),

    CONSTRAINT chk_effectiveness_level
        CHECK (
            effectiveness_level IS NULL -- Mức hiệu quả được khai báo: LOW, MEDIUM, HIGH hoặc VERY_HIGH; có thể để NULL.
            OR effectiveness_level IN (
                'LOW',
                'MEDIUM',
                'HIGH',
                'VERY_HIGH'
            )
        )
);

CREATE TABLE product_histories (
    id                      UUID PRIMARY KEY, -- Khóa chính định danh duy nhất bản ghi lịch sử sản phẩm.
    product_id              UUID NOT NULL, -- ID sản phẩm liên quan đến bản ghi lịch sử.

    action                  VARCHAR(30) NOT NULL, -- Loại hành động đã thực hiện với sản phẩm.
    actor_id                UUID NOT NULL, -- ID người dùng hoặc tác nhân thực hiện hành động.
    actor_type              VARCHAR(20) NOT NULL, -- Loại tác nhân thực hiện hành động: BRAND, ADMIN hoặc SYSTEM.

    previous_publication_status VARCHAR(20), -- Trạng thái đăng của sản phẩm trước khi hành động xảy ra.
    new_publication_status      VARCHAR(20), -- Trạng thái đăng của sản phẩm sau khi hành động xảy ra.

    previous_moderation_status  VARCHAR(20), -- Trạng thái kiểm soát của sản phẩm trước khi hành động xảy ra.
    new_moderation_status       VARCHAR(20), -- Trạng thái kiểm soát của sản phẩm sau khi hành động xảy ra.

    reason                  TEXT, -- Lý do thực hiện hành động, đặc biệt khi khóa, mở khóa, xóa hoặc khôi phục.
    change_summary          TEXT, -- Mô tả ngắn gọn các nội dung đã thay đổi.
    snapshot_data           JSONB, -- Dữ liệu JSON chụp lại trạng thái sản phẩm tại thời điểm ghi lịch sử.

    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Thời điểm bản ghi lịch sử được tạo.

    CONSTRAINT fk_product_history_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT chk_product_history_action
        CHECK (action IN (
            'CREATED',
            'UPDATED',
            'PUBLISHED',
            'UNPUBLISHED',
            'LOCKED',
            'UNLOCKED',
            'DELETED',
            'RESTORED'
        )),

    CONSTRAINT chk_product_history_actor_type
        CHECK (actor_type IN (
            'BRAND',
            'ADMIN',
            'SYSTEM'
        ))
);

CREATE TABLE disease_review_histories (
    id                  UUID PRIMARY KEY, -- Khóa chính định danh duy nhất bản ghi lịch sử xét duyệt bệnh.
    disease_id          UUID NOT NULL, -- ID bệnh liên quan đến bản ghi lịch sử.

    action              VARCHAR(30) NOT NULL, -- Hành động đã thực hiện trong vòng đời xét duyệt bệnh.
    previous_status     VARCHAR(30), -- Trạng thái xét duyệt của bệnh trước hành động.
    new_status          VARCHAR(30) NOT NULL, -- Trạng thái xét duyệt của bệnh sau hành động.

    comment             TEXT, -- Nhận xét, lý do từ chối hoặc nội dung yêu cầu chỉnh sửa.
    actor_id            UUID NOT NULL, -- ID người dùng hoặc tác nhân thực hiện hành động.
    actor_type          VARCHAR(20) NOT NULL, -- Loại tác nhân thực hiện hành động: BRAND, ADMIN hoặc SYSTEM.

    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Thời điểm hành động xét duyệt được ghi nhận.

    CONSTRAINT fk_disease_review_history
        FOREIGN KEY (disease_id)
        REFERENCES diseases(id),

    CONSTRAINT chk_disease_review_action
        CHECK (action IN (
            'CREATED',
            'SUBMITTED',
            'APPROVED',
            'REJECTED',
            'HIDDEN',
            'RESTORED'
        )),

    CONSTRAINT chk_disease_review_actor_type
        CHECK (actor_type IN (
            'BRAND',
            'ADMIN',
            'SYSTEM'
        ))
);

CREATE TABLE product_reviews (
    id                  UUID PRIMARY KEY, -- Khóa chính định danh duy nhất đánh giá sản phẩm.
    product_id          UUID NOT NULL, -- ID sản phẩm được Farmer đánh giá.
    farmer_id           UUID NOT NULL, -- ID Farmer thực hiện đánh giá; đây là ID tham chiếu logic từ user-service hoặc profile-service.

    rating              SMALLINT NOT NULL, -- Số sao Farmer đánh giá cho sản phẩm, có giá trị từ 1 đến 5.
    comment             TEXT, -- Nội dung nhận xét hoặc chia sẻ trải nghiệm của Farmer về sản phẩm.

    status              VARCHAR(20) NOT NULL DEFAULT 'VISIBLE', -- Trạng thái hiển thị của đánh giá: VISIBLE là đang hiển thị, HIDDEN là bị Admin ẩn.
    moderation_reason   TEXT, -- Lý do Admin ẩn đánh giá khi phát hiện nội dung vi phạm hoặc không phù hợp.
    hidden_at           TIMESTAMPTZ, -- Thời điểm đánh giá bị Admin ẩn.
    hidden_by           UUID, -- ID Admin thực hiện ẩn đánh giá.

    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Thời điểm Farmer tạo đánh giá.
    created_by          UUID NOT NULL, -- ID người tạo đánh giá, thông thường trùng với farmer_id.
    updated_at          TIMESTAMPTZ, -- Thời điểm đánh giá được cập nhật gần nhất.
    updated_by          UUID, -- ID người cập nhật đánh giá gần nhất.
    deleted_at          TIMESTAMPTZ, -- Thời điểm đánh giá bị xóa mềm; NULL nghĩa là đánh giá chưa bị xóa.
    deleted_by          UUID, -- ID người thực hiện xóa mềm đánh giá.
    version             BIGINT NOT NULL DEFAULT 0, -- Phiên bản bản ghi dùng cho optimistic locking.

    CONSTRAINT fk_product_review_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    CONSTRAINT chk_product_review_rating
        CHECK (rating BETWEEN 1 AND 5),

    CONSTRAINT chk_product_review_status
        CHECK (status IN (
            'VISIBLE',
            'HIDDEN'
        ))
);

CREATE UNIQUE INDEX uq_product_review_farmer_active
ON product_reviews (product_id, farmer_id)
WHERE deleted_at IS NULL;


CREATE TABLE product_purchase_clicks (
    id                  UUID PRIMARY KEY, -- Khóa chính định danh duy nhất lượt nhấp vào liên kết mua hàng.
    product_id          UUID NOT NULL, -- ID sản phẩm có liên kết được nhấp.
    farmer_id           UUID, -- ID Farmer thực hiện lượt nhấp; có thể NULL nếu người dùng chưa đăng nhập.
    brand_id            UUID NOT NULL, -- ID Brand sở hữu sản phẩm tại thời điểm lượt nhấp được ghi nhận.

    purchase_url        TEXT NOT NULL, -- Đường dẫn mua hàng thực tế mà hệ thống đã chuyển hướng người dùng đến
    user_agent          TEXT, -- Thông tin trình duyệt hoặc thiết bị dùng để hỗ trợ thống kê và phát hiện lượt nhấp bất thường.
    clicked_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Thời điểm người dùng nhấp vào liên kết mua hàng.

    CONSTRAINT fk_product_purchase_click_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
);
CREATE INDEX idx_product_purchase_clicks_product
ON product_purchase_clicks (product_id, clicked_at DESC);

CREATE INDEX idx_product_purchase_clicks_brand
ON product_purchase_clicks (brand_id, clicked_at DESC);

CREATE UNIQUE INDEX uq_product_category_slug_active
ON product_categories (LOWER(slug))
WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_product_slug_per_brand_active
ON products (brand_id, LOWER(slug))
WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_disease_slug_crop_active
ON diseases (LOWER(slug), LOWER(crop_type))
WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_product_disease_treatment_active
ON product_disease_treatments (product_id, disease_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_products_public_list
ON products (published_at DESC)
WHERE deleted_at IS NULL
  AND publication_status = 'PUBLISHED'
  AND moderation_status = 'NORMAL';

CREATE INDEX idx_products_brand
ON products (brand_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_products_category
ON products (category_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_products_moderation
ON products (moderation_status)
WHERE deleted_at IS NULL;

CREATE INDEX idx_diseases_review_status
ON diseases (review_status)
WHERE deleted_at IS NULL;

CREATE INDEX idx_treatments_disease
ON product_disease_treatments (disease_id)
WHERE deleted_at IS NULL;

CREATE INDEX idx_product_histories_product
ON product_histories (product_id, created_at DESC);

CREATE INDEX idx_disease_review_histories_disease
ON disease_review_histories (disease_id, created_at DESC);

CREATE INDEX idx_product_reviews_product
ON product_reviews (product_id, created_at DESC)
WHERE deleted_at IS NULL;

CREATE INDEX idx_product_reviews_visible
ON product_reviews (product_id, rating)
WHERE deleted_at IS NULL
  AND status = 'VISIBLE';