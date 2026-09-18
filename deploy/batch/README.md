# Triển khai đồng loạt toàn bộ 11 backend

Áp dụng cho VPS `160.250.186.236` đã có `/opt/nongthinh/compose.prod.yml`,
PostgreSQL, Auth và Profile như các bước trước. Không thay file Compose gốc
bằng file `deploy/compose.prod.yml` đang trống trong repository.

## 1. Đưa bộ triển khai lên GitHub

Commit/push các file `ci/Jenkinsfile.remaining`, `ci/build-remaining.sh` và
`deploy/batch/` lên `main`. Không commit file env thực tế.

Tạo một lượt tám repository **Public**, namespace `nguyenanh132` trên Docker Hub:

```text
nongthinh-location-service
nongthinh-bo-portal-service
nongthinh-notification-service
nongthinh-brand-service
nongthinh-file-service
nongthinh-agri-catalog-service
nongthinh-post-service
nongthinh-rice-disease-diagnosis-service
```

## 2. Jenkins: một build cho 11 image

Tạo job Pipeline `nongthinh-backends`. Chọn **Pipeline script from SCM**:

- SCM: Git
- Repository: `https://github.com/nguyenAnh132/nongthinh.git`
- Credentials: `nongthinh`
- Branch: `*/main`
- Script Path: `ci/Jenkinsfile.remaining`

Lần đầu Build Now; các lần sau Build with Parameters. `DEPLOY=false` ở lần đầu.
`RUN_TESTS=false` mặc định: bỏ qua Maven test và test script chuẩn bị; vẫn kiểm tra
JAR không chứa cấu hình dev và có cấu hình production. Muốn kiểm thử, bật
`RUN_TESTS=true`; lỗi test sẽ dừng trước khi push. Build bỏ qua test không được coi
là đã qua CI test. Các test tích hợp có điều kiện có thể được Maven bỏ qua.

Build tuần tự, 180 phút timeout cho cả batch. Script lấy source từ commit
đang checkout, loại cấu hình dev khỏi Docker context, kiểm tra từng JAR, rồi push.
Kiểm tra JAR không phải công cụ quét mọi loại secret; không hardcode secret trong source.
Jenkins không nhận mật khẩu database/SMTP/Redis production.
Agent Ubuntu cần `python3` cho kiểm tra script chuẩn bị và `tar`, `bash`, Docker/Git
như các bước trước. VPS deploy cần `python3`, `flock` (thường có sẵn trên Ubuntu).

Khi thành công, tải artifact `nongthinh-backends-batch-N-COMMIT.tar.gz` từ build.
Bundle mới ghi cùng tag cho cả 11 image, bao gồm Auth/Profile/Gateway; overlay ghi đè
tag cũ trong Compose gốc. Các repository Auth/Profile/Gateway đã có từ bước trước.
Giữ nguyên tên job và Script Path để không phải tạo lại Jenkins job.

## 3. Chuyển bundle lên VPS, chuẩn bị secret một lần

Trong PowerShell tại thư mục chứa artifact vừa tải (thay tên file bằng tên thật):

```powershell
scp .\nongthinh-backends-batch-N-COMMIT.tar.gz root@160.250.186.236:/opt/nongthinh/
```

Trên VPS deploy, chọn thư mục mới tương ứng với tag:

```bash
cd /opt/nongthinh
mkdir -p releases/batch-N-COMMIT
tar -xzf nongthinh-backends-batch-N-COMMIT.tar.gz -C releases/batch-N-COMMIT
python3 releases/batch-N-COMMIT/prepare.py
```

Script hỏi tám mật khẩu tài khoản database đã tạo, Gmail gửi thư, Gmail App Password
và mật khẩu mới cho Camunda admin. Mật khẩu nhập không hiển thị. Từng mật khẩu DB
được kiểm tra qua TCP trước khi ghi file. Script không thay đổi mật khẩu database.

Script lấy issuer, Kafka, Redis và key Profile/Location từ cấu hình Auth/Profile hiện tại.
Các key mới được sinh và phân phối nhất quán. Secret được ghi vào `env/batch/`, quyền
thư mục 700 và file 600. File `env/location.env` trước đây không được dùng trong batch.
Không chạy `source` các file này: chúng dùng định dạng Compose `env_file: format: raw`.

`env/batch` đã tồn tại thì script dừng, tránh thay key đang dùng. Nếu cần chỉnh sửa,
dùng `nano /opt/nongthinh/env/batch/<service>.env`; không thêm dấu nháy bao mật khẩu
vì định dạng raw coi dấu nháy là một phần giá trị. File `env/auth.env`, `env/profile.env`
cũ vẫn theo định dạng dotenv ban đầu, có thể có nháy đơn.

File upload được giữ tại `/opt/nongthinh/data/files`, UID/GID 10001. Script chỉ cấp
quyền cho thư mục mới; không đổi chủ sở hữu đệ quy trên dữ liệu đã có.

## 4. Deploy cả batch

```bash
bash /opt/nongthinh/releases/batch-N-COMMIT/deploy.sh
```

Script ghép Compose gốc và overlay, pull các image, khởi động từng service để hạn chế
đỉnh RAM/CPU. Sau đó cập nhật URL nội bộ Auth → Profile và Profile → Location, gây
restart ngắn cho hai service này. PostgreSQL không bị tạo lại, volume không bị xóa.
File này dùng đúng project `nongthinh` từ Compose gốc.

Khi lỗi, script dừng tại service lỗi, in log và giữ các service đã chạy. Sau khi sửa
cấu hình, chạy lại cùng lệnh. Không có tự động rollback database migration. Mỗi lần
thay đổi schema sau khi có dữ liệu thật cần backup trước khi deploy.

Script tạo lại từng backend và kiểm tra log từ lần khởi động hiện tại, rồi kiểm tra
Auth 302, Profile 401, Gateway 401 và không có restart/OOM. Thành công in
`BATCH_STARTUP_OK`. Kiểm tra Gateway thủ công:

```bash
curl -i http://127.0.0.1:8888/api/v1/profile/farmer-profiles/me
```

Mong đợi HTTP 401 do chưa có token, giống Profile đã sửa. Cổng các service bind
loopback trên VPS. Frontend/Nginx/HTTPS chưa được tạo trong batch backend này.

## 5. Các lần deploy tiếp theo

Job `nongthinh-backends` → Build with Parameters → `DEPLOY=true`.
Jenkins dùng credential `nongthinh-deploy-ssh`, copy bundle mới vào thư mục release
mới và chạy deploy.sh. Secret đã có trên VPS được tái sử dụng. Host key SSH phải
được tin cậy trên agent chạy job như lần kiểm tra trước.

Pipeline build toàn bộ 11 backend, chưa lọc service theo diff. Trigger `githubPush()`
dùng webhook GitHub hiện có (`<JENKINS_URL>/github-webhook/`) để kiểm tra thay đổi main.
Sau khi cập nhật Jenkinsfile, chạy thủ công một lần với `DEPLOY=true`, `RUN_TESTS=false`
để đăng ký trigger và giá trị mặc định mới. Các push main tiếp theo tự build/deploy,
không chạy test mặc định. Cần xác nhận bằng một push main thực tế và log Jenkins.
Đặt node Jenkins đang build chỉ có 1 executor để backend và frontend không build
đồng thời trên VPS 4 GB; `disableConcurrentBuilds()` chỉ giới hạn trong từng job.

## Những kiểm tra chức năng còn lại

- Startup log không chứng minh Kafka consumer, gửi mail, Redis auth và API nghiệp vụ hoạt động.
- Kafka/Redis hiện kết nối qua IP public. Cần kiểm tra firewall hạn chế nguồn truy cập;
  Kafka đang PLAINTEXT. Bộ này không thay firewall của VPS Jenkins.
- Brand bật `BRAND_CAMUNDA_DATABASE_SCHEMA_UPDATE=true` cho lần tạo database trống
  vì Flyway chỉ có bảng ứng dụng. Sau khi tạo đủ bảng Camunda và kiểm tra, đặt false
  trong `env/batch/brand-service.env` rồi tạo lại Brand container.
- Database mới chưa có dữ liệu nghiệp vụ/model. Chẩn đoán cần đăng ký artifact,
  mapping và kích hoạt model qua luồng quản trị; readiness mô hình có thể chưa đạt.
- Cần kiểm tra dữ liệu tỉnh/xã từ migration và luồng đăng ký/đăng nhập sau khi có HTTPS.
- RAM là giới hạn khởi điểm, khoảng 6 GiB tổng container cùng PostgreSQL và Auth/Profile;
  theo dõi `docker stats --no-stream`, đặc biệt Brand và ONNX.
- Không dùng `docker compose down -v` hoặc prune volume khi cập nhật ứng dụng.

Để xem trạng thái/log, luôn ghép cả hai Compose và env tag của release đang dùng:

```bash
docker compose --env-file /opt/nongthinh/.env \
  --env-file /opt/nongthinh/releases/batch-N-COMMIT/images.env \
  -f /opt/nongthinh/compose.prod.yml \
  -f /opt/nongthinh/releases/batch-N-COMMIT/compose.remaining.yml ps
```
