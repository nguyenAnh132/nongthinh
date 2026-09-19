# Complete registration trên production

Luồng này thay cho phần admin bootstrap vừa triển khai trong working tree. Không còn runner, cấu hình bootstrap, migration hay publisher outbox của luồng đó. Không thêm migration mới; dùng bảng `users` và các bảng profile hiện có. Các luồng Kafka và outbox nghiệp vụ đã tồn tại ở service khác vẫn giữ nguyên.

## Luồng sử dụng

1. Người vận hành tạo tài khoản trên Keycloak và gán application role phù hợp. Với admin, gán `ROLE_ADMIN` và nhóm/quyền quản trị theo chính sách hiện tại. Complete-registration không tạo tài khoản Keycloak, đặt mật khẩu hoặc cấp quyền admin.
2. Người dùng đăng nhập bình thường bằng Keycloak.
3. `GET /api/v1/auth/me` trả `409`, code `AUTH_REGISTRATION_REQUIRED`, khi thiếu local user, thiếu liên kết `nongthinh_id` hoặc thiếu profile tương ứng.
4. Frontend chuyển sang `/complete-registration`, hiển thị form tương ứng với role do backend trả về.
5. Frontend gọi `POST /api/v1/auth/me/complete-registration`. Backend lấy `sub`, email và role từ JWT đã xác thực; body chỉ chứa thông tin hồ sơ.
6. Auth-service kiểm tra danh tính Keycloak, lưu local user nếu chưa có, liên kết thuộc tính `nongthinh_id`, rồi gọi profile-service qua HTTP nội bộ có API key.
7. Khi thành công, frontend refresh token để nhận claim mới và gọi lại `/me`, sau đó chuyển đến trang phù hợp.

Ví dụ lỗi thiếu đăng ký (các trường envelope khác được lược bỏ):

```json
{
  "code": "AUTH_REGISTRATION_REQUIRED",
  "result": { "email": "admin@example.com", "role": "ROLE_ADMIN" }
}
```

Ví dụ body admin:

```json
{ "firstName": "An", "lastName": "Nguyễn Văn", "phone": "0901234567" }
```

Farmer bổ sung `gender`: `MALE`, `FEMALE` hoặc `OTHER`. Brand dùng `brandName`, `phone`, `representativeName`, `representativePhone`, `representativeEmail`. Không gửi `userId`, email tài khoản, role hoặc password. Thành công trả `200` với `result.userId` và `result.refreshRequired=true`.

Endpoint nội bộ: `POST /profile/internal/registration-profiles` với context-path mặc định; chỉ chấp nhận xác thực service-to-service. Không gọi endpoint này trực tiếp từ frontend.

## Xử lý lỗi và gửi lại

- Chỉ `409 AUTH_REGISTRATION_REQUIRED` kích hoạt form. `401` xử lý phiên đăng nhập; `403` báo không có quyền; timeout/`5xx` hiển thị lỗi và cho thử lại.
- Danh tính được tìm theo Keycloak `sub`, không gắn tài khoản bằng email. Email hoặc ID đã thuộc danh tính khác trả lỗi `AUTH_REGISTRATION_IDENTITY_CONFLICT` và cần vận hành kiểm tra dữ liệu.
- Bản ghi local user được commit trước các cuộc gọi mạng. Nếu Keycloak/profile-service lỗi, người dùng gửi lại cùng request; hệ thống dùng lại local user đó. Không có distributed transaction hay tác vụ retry nền.
- Unique constraint hiện có và `INSERT ... ON CONFLICT DO NOTHING` bảo vệ việc đăng ký local user đồng thời. Profile đã tồn tại được giữ nguyên; gửi lại không sửa thông tin, mở khóa hay phục hồi hồ sơ.
- Brand mới vẫn `PENDING_APPROVAL`. Luồng đồng bộ quyền doanh nghiệp hiện có vẫn được gọi; không tự duyệt brand. Event nghiệp vụ brand hiện có vẫn giữ nguyên.
- Lần GET `/me` tiếp theo vẫn có thể lỗi nếu hạ tầng chưa phục hồi. Nếu token chưa nhận claim mới sau refresh, kiểm tra mapper Keycloak và đăng nhập lại.

## Cấu hình production

Theo `docs/prompt/deployment-operations/03-environment-changes.md`, runtime env Auth nằm ở `/opt/nongthinh/env/auth.env`, Profile ở `/opt/nongthinh/env/profile.env`. `.env` gốc phục vụ nội suy Compose; release `images.env` chỉ chứa image tag.

**Không có biến môi trường mới bắt buộc.** Kiểm tra các biến sẵn có:

| File | Biến | Yêu cầu |
|---|---|---|
| `env/auth.env` | `AUTH_PROFILE_SERVICE_URL` | Base URL profile-service mà Auth truy cập được, gồm context-path `/profile` nếu dùng mặc định |
| Cả hai file | `PROFILE_SERVICE_API_KEY` | Giá trị phải khớp nhau; không đưa secret vào Git |
| `env/auth.env` | `KEYCLOAK_URL`, `KEYCLOAK_REALM` | Admin API base URL và realm đúng |
| `env/auth.env` | `KEYCLOAK_CLIENT_ID`, `KEYCLOAK_CLIENT_SECRET` | Service account có quyền đọc/cập nhật user, giữ quyền phục vụ nghiệp vụ hiện tại |
| Cả hai file | `KEYCLOAK_ISSUER_URI` | Issuer khớp token production |

Có thể thêm vào `env/auth.env` nếu cần chỉnh timeout; không thêm thì dùng đúng các mặc định sau:

```dotenv
INTERNAL_CLIENT_CONNECT_TIMEOUT_MS=2000
INTERNAL_CLIENT_READ_TIMEOUT_MS=10000
```

Hai biến áp dụng cho các Feign client của Auth, gồm Keycloak và Profile. Nếu đã thêm `AUTH_BOOTSTRAP_ADMIN_*`, `AUTH_OUTBOX_*` cho phiên bản trước, bỏ các biến đó và cấu hình/profile/job chạy admin-bootstrap nếu có. Giữ `SPRING_PROFILES_ACTIVE=prod` và các biến nghiệp vụ hiện hành, kể cả `KAFKA_BOOTSTRAP_SERVERS`.

Trên Keycloak, kiểm tra trước rollout:

- Token có UUID `sub`, email và application realm role (`ROLE_ADMIN`, `ROLE_FARMER`, `ROLE_BRAND` hoặc `ROLE_BRAND_PENDING`). Tài khoản không có application role không được complete-registration.
- Mapper thuộc tính `nongthinh_id` xuất vào access token dưới dạng chuỗi UUID. Thuộc tính có thể chưa có ở lần đăng nhập đầu; Auth sẽ bổ sung khi complete-registration.
- Cho phép service account quản lý thuộc tính này; người dùng thường không được tự sửa `nongthinh_id` qua account console/user profile. Không đặt nó là trường bắt buộc do người dùng điền khi đăng nhập.
- Với admin đầu tiên, tạo credential và gán quyền qua kênh quản trị Keycloak hiện hành. Không cần nhập password admin vào env của Auth.

## Rollout

Các bước dưới đây là hướng dẫn, chưa được thực hiện trên VPS.

1. Theo tài liệu deployment-operations mục 02/03, xác định release khớp `/opt/nongthinh/state/backend.json` và image đang chạy. Không chọn release đơn thuần theo thời gian mới nhất. Backup env trước khi sửa; kiểm tra Compose `environment:` có ghi đè env_file hay không.
2. Build image mới cho **profile-service, auth-service và frontend** theo pipeline hiện tại. Chỉ sửa env/restart image cũ không đưa API mới vào production.
3. Triển khai Profile có endpoint nội bộ trước Auth; triển khai frontend tương ứng trong cùng đợt. Phối hợp cửa sổ cập nhật vì `/me` đổi từ trả profile thiếu sang lỗi `409` mà frontend cũ chưa xử lý.
4. Nếu chỉ điều chỉnh timeout sau khi đã rollout code, dùng helper Compose với release đã xác minh theo mục 03, `config --quiet`, rồi recreate riêng `auth-service` dưới deploy lock. `restart` không nạp env mới. Không chép đè Compose production bằng file rỗng trong repository.
5. Kiểm tra login tài khoản hiện có; admin chưa có local user/profile; thiếu riêng profile; refresh claim; gọi lại POST không tạo trùng; lỗi Profile không bị hiểu là thiếu đăng ký; farmer không tạo được admin; brand vẫn chờ duyệt.
6. Khi rollback ứng dụng, dùng các image backend/frontend tương thích theo release manifest và khôi phục env nếu đã sửa. Không xóa user/profile hoặc thuộc tính Keycloak đã được tạo chỉ để rollback code.

Migration bootstrap V5 bị bỏ khỏi working tree vì chưa thuộc baseline trước triển khai bootstrap. Nếu đã tự chạy phiên bản bootstrap đó trên một database dùng chung, kiểm tra lịch sử Flyway và lập migration chuyển tiếp trước rollout; không xóa bảng hay sửa lịch sử migration theo tài liệu này.

## Kiểm chứng trong workspace

- Auth: 63 test thuộc các nhóm identity, complete-registration use case/adapter/controller, brand role synchronization và exception handler.
- Profile: 49 test thuộc nhóm complete-registration, brand access và exception handler.
- Frontend: 28 test về AuthService, interceptor, trang auth/complete-registration, root component và routes.
- Angular production build (browser và SSR).

Các kiểm thử dùng mock cho Keycloak/Profile HTTP và repository; chưa xác minh luồng xuyên suốt trên Keycloak/PostgreSQL production. Thực hiện các bước nghiệm thu rollout ở trên trên môi trường triển khai.

## Gợi ý add và commit

Từ repo root, kiểm tra diff trước khi stage để loại trừ thay đổi ngoài tác vụ:

```powershell
git diff --check
git add services/auth-service/src services/auth-service/COMPLETE_REGISTRATION.md services/profile-service/src apps/web/nongthinh/src/app
git diff --cached --stat
git commit -m "feat: complete account registration after Keycloak login"
```

Message dùng type `feat` theo `docs/commit-message-convention`. Không cần commit thao tác bỏ bootstrap riêng vì phần bootstrap chưa nằm trong HEAD. Tài liệu dưới `docs/prompt/` đang bị ignore; bản hướng dẫn này nằm trong service để có thể commit bình thường.
