# Quyền truy cập của doanh nghiệp

## Quy tắc

Tài khoản đăng ký doanh nghiệp được cấp `ROLE_BRAND_PENDING`. Chỉ hồ sơ có trạng thái `ACTIVE` mới được cấp `ROLE_BRAND` và sử dụng các chức năng nghiệp vụ.

| Trạng thái hồ sơ | Xem hồ sơ của mình | Sửa hồ sơ | Nộp giấy tờ | Dùng chức năng nghiệp vụ |
| --- | --- | --- | --- | --- |
| Chưa tạo hồ sơ | Có thể tạo hồ sơ | Có | Không | Không |
| `PENDING_APPROVAL` | Có | Có | Không | Không |
| `UNDER_REVIEW`, `NEEDS_REVISION` | Có | Có | Có | Không |
| `READY_FOR_FINAL_REVIEW` | Có | Có | Không | Không |
| `ACTIVE` và token có `ROLE_BRAND` | Có | Có | Theo luồng xét duyệt hiện có | Có |
| `REJECTED`, `LOCKED`, `DISABLED`, `DELETED` | Có | Không | Không | Không |

Việc nộp giấy tờ giữ đúng các bước xét duyệt hiện có: sau bước kiểm tra ban đầu hoặc khi được yêu cầu bổ sung. Các API địa chỉ, chính sách upload và file cần để hoàn thiện hồ sơ được cho phép với phạm vi giới hạn. File đọc trong giai đoạn này phải thuộc tài khoản hiện tại và có mục đích logo, banner hoặc giấy phép doanh nghiệp.

Các service kiểm tra trạng thái hiện tại qua profile-service cho từng yêu cầu của doanh nghiệp. Token cũ còn `ROLE_BRAND` không cấp quyền khi hồ sơ đã bị từ chối hoặc khóa. Khi không truy vấn được trạng thái, API trả lỗi hạ tầng và không thực hiện nghiệp vụ. Những endpoint vốn cho phép truy cập ẩn danh vẫn giữ chính sách ẩn danh hiện có.

## API và đồng bộ

- `GET /profile/brand-profiles/me/access`: nhận JWT có `ROLE_BRAND` hoặc `ROLE_BRAND_PENDING`, trả `ApiResponse` với `result: { active, canEditProfile, canSubmitDocuments }`.
- Yêu cầu ngoài quyền hiện tại trả HTTP 403 với mã `BRAND_ACCESS_DENIED`.
- Migration `V10__brand_access_outbox.sql` ghi sự kiện cùng transaction với thay đổi trạng thái hồ sơ, đồng thời tạo sự kiện cho các hồ sơ đã tồn tại.
- Auth-service nhận sự kiện `brand-access-changed`, đọc lại trạng thái mới nhất rồi thêm role phù hợp và gỡ role cũ. Sự kiện lặp hoặc đến muộn không được dùng để ghi đè trạng thái hiện tại. Đồng bộ thất bại được retry.
- `/auth/me` cũng đồng bộ khi role trong token lệch với trạng thái hồ sơ. Frontend refresh token một lần rồi tải lại thông tin; tài khoản bị giới hạn được đưa về trang hồ sơ.
- Notification-service đóng các kết nối SSE của tài khoản khi nhận sự kiện thay đổi trạng thái. Kết nối lại phải qua kiểm tra quyền. Việc đóng kết nối hiện hữu phụ thuộc thời điểm nhận sự kiện Kafka.

## Cấu hình triển khai

1. Keycloak cần realm role `ROLE_BRAND_PENDING`, không kế thừa quyền nghiệp vụ. Có thể tạo trước; auth-service có hỗ trợ tạo role khi chưa tồn tại nếu service account được cấp quyền quản lý role. Service account cần quyền đọc và sửa role mapping của người dùng.
2. Cấu hình `BRAND_ACCESS_PROFILE_SERVICE_URL` tại post, agri-catalog, rice-disease-diagnosis, notification, brand, file, location và bo-portal service. URL phải gồm context path `/profile`; mặc định local là `http://localhost:9092/profile`.
3. Tùy chọn timeout HTTP: `BRAND_ACCESS_CONNECT_TIMEOUT_MS` (mặc định 2000), `BRAND_ACCESS_READ_TIMEOUT_MS` (mặc định 3000).
4. Auth, profile và notification service dùng cùng `KAFKA_TOPIC_BRAND_ACCESS_CHANGED` (mặc định `brand-access-changed`). Topic cần tồn tại nếu broker không tự tạo topic. Auth consumer dùng group `auth-brand-access`; mỗi notification instance có group riêng để đóng các kết nối cục bộ.
5. Triển khai kiểm tra quyền tại các service và frontend cùng thay đổi auth/profile; cho Flyway chạy V10 trên database của profile-service. Theo dõi outbox chưa phát và consumer auth để xác nhận các tài khoản cũ đã được đồng bộ. Chưa chạy migration hay thay đổi Keycloak trên môi trường dùng chung trong tác vụ này.

Kiểm tra HTTP trạng thái hồ sơ không dùng cache, nên profile-service cần sẵn sàng cho lưu lượng gọi từ các service nghiệp vụ.

## Kiểm chứng thay đổi

Các lệnh dưới đây đã chạy thành công trong thư mục tương ứng:

| Thư mục | Lệnh |
| --- | --- |
| `services/auth-service` | `mvn -q test '-Dtest=AuthIdentityUseCasesTest,SynchronizeBrandRoleUseCaseTest'` |
| Các service post, agri-catalog, rice-disease-diagnosis, location, bo-portal | `mvn -q test '-Dtest=BrandAccessConfigTest'` |
| `services/brand-service` | `mvn -q test '-Dtest=BrandAccessConfigTest'` và `mvn -q test '-Dtest=BrandAccessQueryHttpTest'` |
| `services/file-service` | `mvn -q test '-Dtest=BrandAccessQueryHttpTest,BrandAccessConfigTest'` |
| `services/notification-service` | `mvn -q test '-Dtest=BrandAccessConfigTest'` và `mvn -q test '-Dtest=SseConnectionRegistryTest'` |
| `services/profile-service` | `mvn -q test '-Dtest=BrandAccessConfigTest,BrandAccessPolicyTest'` và `mvn -q test '-Dtest=LocalBrandAccessOutboxTest,BrandAccessMvcTest' '-Dbrand.local.integration=true'` |
| `apps/web/nongthinh` | `npm run build` |

Kiểm thử outbox dùng PostgreSQL tạm trên localhost, port 55441; đã dừng sau kiểm thử. Không sử dụng database chung. Tổng cộng 117 kiểm thử backend trong các nhóm trên pass.

Frontend: 47 kiểm thử pass với lệnh chạy tại `apps/web/nongthinh`:

```powershell
npm test -- --watch=false --include=src/app/core/guards/brand-feature.guard.spec.ts --include=src/app/core/auth/auth.service.spec.ts --include=src/app/core/interceptors/auth.interceptor.spec.ts --include=src/app/layouts/user-layout/user-layout.spec.ts --include=src/app/views/user/brand-verification/brand-verification.spec.ts --include=src/app/core/realtime/realtime.service.spec.ts
```

Chưa kiểm chứng toàn luồng với Keycloak và Kafka thực tế trên môi trường triển khai.
