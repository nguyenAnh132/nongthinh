# Docker cho backend services

Mỗi thư mục service có `Dockerfile` và `.dockerignore` riêng. Chạy các lệnh
dưới đây từ thư mục gốc repository với Docker Engine ở chế độ Linux containers.
Dockerfile dùng BuildKit để cache Maven dependencies, build bằng JDK 21 và chạy
bằng JRE 21 với UID/GID `10001:10001`. Maven và source code chỉ nằm ở build stage.

## Build

```powershell
docker build -t nongthinh/api-gateway:local ./services/api-gateway
```

Build toàn bộ 11 service bằng PowerShell:

```powershell
Get-ChildItem ./services -Directory | Where-Object {
    Test-Path (Join-Path $_.FullName 'Dockerfile')
} | ForEach-Object {
    docker build -t "nongthinh/$($_.Name):local" $_.FullName
    if ($LASTEXITCODE -ne 0) { throw "Build failed: $($_.Name)" }
}
```

Build context phải là thư mục của từng service. Không cần build JAR trên máy
trước. Docker build bỏ qua compile/chạy test; chạy `mvn test` trong từng service
hoặc pipeline CI trước khi triển khai.

## Chạy container

Các image mặc định dùng `SPRING_PROFILES_ACTIVE=prod`. Chuẩn bị file biến môi trường
theo các placeholder trong `src/main/resources/application-prod.yaml` (riêng
auth-service là `application-prod.yml`) và `application.yaml` của service.
File env dùng cú pháp `KEY=value` cho `docker run --env-file`; không dựa vào nội suy
`${...}` hoặc cú pháp shell trong file này. File env không được copy vào image.

```powershell
docker run --rm --name api-gateway --env-file .env -p 8888:8888 nongthinh/api-gateway:local
```

Đặt URL/host của database, Redis, Kafka, Keycloak và các service khác thành địa chỉ
truy cập được từ container. Khi các container cùng một Docker network, dùng tên
container làm hostname và truyền `--network <ten-network>`. Để gọi dịch vụ trên
máy host qua Docker Desktop, dùng `host.docker.internal`. `localhost` trong
container trỏ về chính container đó. Cấu hình cả `BRAND_ACCESS_PROFILE_SERVICE_URL`
khi service sử dụng biến này.

| Service | Cổng mặc định |
| --- | --- |
| api-gateway | 8888 |
| auth-service | 9090 |
| profile-service | 9092 |
| notification-service | 9093 |
| location-service | 9094 |
| file-service | 9095 |
| bo-portal-service | 9096 |
| brand-service | 9097 |
| agri-catalog-service | 9098 |
| rice-disease-diagnosis-service | 9099 |
| post-service | 9100 |

`EXPOSE` chỉ mô tả cổng; dùng `-p` để publish cổng. Nếu đổi biến cổng của service
hoặc `SERVER_PORT`, cập nhật cổng phía container trong `-p` tương ứng.
Có thể truyền tùy chọn JVM bằng `-e JAVA_TOOL_OPTIONS="-Xms256m -Xmx512m"`.

## Lưu trữ file

`file-service` chuẩn bị `/data/storage` với quyền ghi cho UID/GID `10001:10001`,
khớp đường dẫn mặc định của profile prod. Gắn named volume để giữ dữ liệu:

```powershell
docker run --rm --name file-service --env-file .env -e FILE_STORAGE_BASE_PATH=/data/storage --mount source=nongthinh-files,target=/data/storage -p 9095:9095 nongthinh/file-service:local
```

Nếu dùng bind mount hoặc thay `FILE_STORAGE_BASE_PATH`, thư mục đích phải cho
UID/GID `10001:10001` quyền ghi. Service chẩn đoán dùng ONNX Runtime; runtime image
dùng Ubuntu/glibc và model được lấy qua luồng quản lý model hiện có của ứng dụng.

Base images: [Maven official image](https://github.com/docker-library/official-images/blob/master/library/maven)
và [Eclipse Temurin official image](https://github.com/docker-library/official-images/blob/master/library/eclipse-temurin).
