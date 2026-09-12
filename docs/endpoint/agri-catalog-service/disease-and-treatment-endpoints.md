# Disease và Product Disease Treatment endpoints

Base URL trực tiếp ở môi trường dev:

```text
http://localhost:9098/agri-catalog
```

Tất cả response dùng `ApiResponse<T>`.

## 1. Disease workflow

### Quy tắc nghiệp vụ

- Admin tạo Disease: `createdSource=ADMIN`, `brandId=null`, trạng thái ban đầu `APPROVED` và được public ngay.
- Brand tạo Disease: `createdSource=BRAND`, `brandId` lấy từ current user, trạng thái ban đầu `DRAFT`.
- Brand chỉ xem/sửa/xóa Disease của chính mình.
- Brand chỉ sửa/xóa khi Disease là `DRAFT` hoặc `REJECTED`.
- Brand submit `DRAFT` hoặc `REJECTED` sang `PENDING_REVIEW`.
- Admin chỉ approve/reject Disease đang `PENDING_REVIEW`.
- Admin chỉ hide Disease đang `APPROVED`.
- Mỗi thao tác create/submit/approve/reject/hide được ghi vào `disease_review_histories`.
- Public API chỉ trả Disease `APPROVED`, chưa soft delete.
- Cặp `(LOWER(slug), LOWER(cropType))` duy nhất trong các Disease chưa soft delete.

### Request body create/update

```json
{
  "name": "Rice blast",
  "slug": "rice-blast",
  "scientificName": "Magnaporthe oryzae",
  "cropType": "RICE",
  "affectedPart": "LEAF",
  "pathogenType": "FUNGUS",
  "shortDescription": "Common fungal disease in rice",
  "description": "Detailed description",
  "symptoms": "Leaf lesions",
  "causes": "Fungal pathogen",
  "favorableConditions": "High humidity",
  "preventionMethod": "Use resistant varieties",
  "treatmentGuideline": "Apply an appropriate registered product",
  "thumbnailUrl": "https://example.com/rice-blast.jpg"
}
```

### Authenticated endpoints

| Method | Path | Role | Mô tả |
|---|---|---|---|
| `GET` | `/diseases?brandId=&reviewStatus=&cropType=` | Admin, Brand | Admin xem theo filter; Brand luôn chỉ nhận dữ liệu của mình |
| `GET` | `/diseases/{id}` | Admin, Brand | Chi tiết; Brand phải sở hữu Disease |
| `POST` | `/diseases` | Admin, Brand | Tạo mới; source/brand suy ra từ JWT |
| `PUT` | `/diseases/{id}` | Admin, Brand | Cập nhật nội dung |
| `DELETE` | `/diseases/{id}` | Admin, Brand | Soft delete Disease và treatment liên quan |
| `POST` | `/diseases/{id}/submit` | Brand | Gửi xét duyệt |
| `POST` | `/diseases/{id}/approve` | Admin | Duyệt và public |
| `POST` | `/diseases/{id}/reject` | Admin | Từ chối |
| `POST` | `/diseases/{id}/hide` | Admin | Ẩn khỏi public |
| `GET` | `/diseases/{id}/review-history` | Admin, Brand | Lịch sử xét duyệt |

Request reject:

```json
{
  "reason": "The symptom description needs supporting details"
}
```

`reviewStatus` nhận một trong:

```text
DRAFT, PENDING_REVIEW, APPROVED, REJECTED, HIDDEN
```

### Public endpoints

| Method | Path | Mô tả |
|---|---|---|
| `GET` | `/public/diseases?cropType=` | Danh sách Disease đã duyệt |
| `GET` | `/public/diseases/{id}` | Chi tiết Disease đã duyệt |

## 2. Product Disease Treatment

### Quy tắc nghiệp vụ

- Đây là nested resource của Product.
- `productId` lấy từ path.
- `brandId` luôn lấy từ Product, client không được gửi.
- Brand chỉ thao tác treatment của Product thuộc Brand mình.
- Admin có thể thao tác mọi Product.
- Disease được liên kết phải tồn tại và có trạng thái `APPROVED`.
- Một cặp `(productId, diseaseId)` chỉ có một treatment chưa soft delete.
- `priority >= 0`.
- Public API chỉ trả treatment khi Product là `PUBLISHED` + `NORMAL` và Disease là `APPROVED`.
- Xóa Product hoặc Disease sẽ soft-delete các treatment liên quan.

### Request create

```json
{
  "diseaseId": "00000000-0000-0000-0000-000000000000",
  "effectivenessLevel": "HIGH",
  "priority": 0,
  "dosage": "10 ml per 10 liters of water",
  "applicationMethod": "Spray evenly on affected plants",
  "applicationTiming": "Early morning",
  "frequencyInstruction": "Repeat every seven days",
  "treatmentNote": "Follow the product label"
}
```

Update có cùng body nhưng không có `diseaseId`; Product và Disease của quan hệ là bất biến.

`effectivenessLevel` có thể `null` hoặc một trong:

```text
LOW, MEDIUM, HIGH, VERY_HIGH
```

### Authenticated endpoints

| Method | Path | Role | Mô tả |
|---|---|---|---|
| `GET` | `/products/{productId}/disease-treatments` | Admin, Brand | Danh sách theo Product |
| `GET` | `/products/{productId}/disease-treatments/{treatmentId}` | Admin, Brand | Chi tiết |
| `POST` | `/products/{productId}/disease-treatments` | Admin, Brand | Tạo liên kết |
| `PUT` | `/products/{productId}/disease-treatments/{treatmentId}` | Admin, Brand | Cập nhật metadata điều trị |
| `DELETE` | `/products/{productId}/disease-treatments/{treatmentId}` | Admin, Brand | Soft delete |
| `GET` | `/diseases/{diseaseId}/product-treatments` | Admin | Danh sách Product treatment theo Disease |

### Public endpoints

| Method | Path | Mô tả |
|---|---|---|
| `GET` | `/public/products/{productId}/disease-treatments` | Treatment công khai theo Product |
| `GET` | `/public/diseases/{diseaseId}/product-treatments` | Product treatment công khai theo Disease |

## 3. Error codes chính

```text
NOT_FOUND_DISEASE_NOT_FOUND
NOT_FOUND_PRODUCT_DISEASE_TREATMENT_NOT_FOUND
BUS_DISEASE_SLUG_ALREADY_EXISTS
BUS_DISEASE_STATUS_TRANSITION_INVALID
BUS_DISEASE_NOT_APPROVED
BUS_PRODUCT_DISEASE_TREATMENT_ALREADY_EXISTS
AUTH_BRAND_RESOURCE_ACCESS_DENIED
VAL_DISEASE_*
VAL_PRODUCT_DISEASE_TREATMENT_*
```
