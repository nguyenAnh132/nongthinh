# Hướng dẫn phát triển endpoint cho `agri-catalog-service`

Tài liệu này là playbook tái sử dụng khi bổ sung endpoint cho một bảng hoặc một tài nguyên mới trong:

```text
services/agri-catalog-service
```

Mục tiêu là giữ đúng Clean Architecture đang được áp dụng trong service, đồng thời thống nhất với cách triển khai của các luồng `ProductCategory`, `Product` và `ProductImage`.

> Ví dụ trong tài liệu dùng tài nguyên giả định `Resource`. Khi triển khai, thay `Resource`, `resource`, `resources` và `resources_table` bằng tên nghiệp vụ thực tế. Không sao chép nguyên trạng business rule của ví dụ sang tài nguyên khác.

## 1. Nguồn tham chiếu chuẩn trong service

Khi có điểm chưa rõ, ưu tiên đối chiếu các luồng hiện có:

- CRUD cơ bản, phân quyền admin, public list, soft delete và cây cha-con: `ProductCategory`.
- Bộ lọc danh sách, ownership theo Brand và trạng thái công khai: `Product`.
- Gọi service ngoài, invariant liên quan nhiều bản ghi và support class dùng chung: `ProductImage`.

Các thư mục chính:

```text
com.nongthinh.agri_catalog_service
├── domain
│   └── <resource>
├── application
│   ├── command
│   ├── view
│   └── port
│       ├── in/<resource>
│       │   └── impl
│       └── out/repository
├── infra
│   └── persistence/<resource>
├── presentation
│   ├── controller
│   ├── dto/request
│   └── mapper
├── common
│   ├── exception
│   └── response
└── configuration
```

## 2. Dependency rule

Luồng phụ thuộc phải hướng vào trong:

```text
HTTP
  -> Controller / Request DTO / Presentation Mapper
  -> Input Port (Use Case)
  -> Use Case Implementation
  -> Domain
  -> Output Port (Repository interface)
  -> Persistence Adapter
  -> Spring Data JPA
  -> PostgreSQL
```

Chiều phụ thuộc theo layer:

| Layer | Được phụ thuộc | Không được phụ thuộc |
|---|---|---|
| `domain` | Java thuần, value object/domain exception khi cần | Spring MVC, Spring Data, JPA entity, request DTO |
| `application` | Domain, input/output port, command, view | Controller, JPA repository/entity, HTTP |
| `presentation` | Input port, command/view, `ApiResponse` | JPA repository/entity, persistence adapter |
| `infra` | Output port, domain, framework persistence/client | Controller/request DTO |

Các nguyên tắc bắt buộc:

- Controller chỉ xử lý HTTP, validation đầu vào, mapping request và đóng gói response.
- Use case điều phối nghiệp vụ, transaction, quyền sở hữu, clock, current user và các port.
- Domain giữ trạng thái cùng hành vi nghiệp vụ; không đặt annotation JPA lên domain.
- Application chỉ biết interface repository, không inject `JpaRepository`.
- Adapter persistence chịu trách nhiệm chuyển đổi domain ↔ JPA entity.
- Không trả JPA entity trực tiếp ra API.
- Không nhận request DTO trực tiếp trong use case.

## 3. Quy ước hiện tại của dự án

### 3.1. Tên và package

| Thành phần | Quy ước | Ví dụ |
|---|---|---|
| Domain | `<Resource>` | `ProductCategory` |
| Request tạo | `<Resource>CreationRequest` | `ProductCategoryCreationRequest` |
| Request cập nhật | `<Resource>UpdateRequest` | `ProductCategoryUpdateRequest` |
| Command tạo | `<Resource>CreationCommand` | `ProductCreationCommand` |
| Command cập nhật | `<Resource>UpdateCommand` | `ProductUpdateCommand` |
| View | `<Resource>View` | `ProductImageView` |
| Input port | `<Verb><Resource>UseCase` | `CreateProductUseCase` |
| Use case implementation | `<Verb><Resource>UseCaseImpl` | `DeleteProductUseCaseImpl` |
| Output port | `<Resource>Repository` | `ProductRepository` |
| JPA entity | `Jpa<Resource>Entity` | `JpaProductEntity` |
| Spring Data repository | `Jpa<Resource>Repository` | `JpaProductRepository` |
| Persistence mapper | `<Resource>PersistenceMapper` | `ProductPersistenceMapper` |
| Adapter | `<Resource>RepositoryImpl` | `ProductRepositoryImpl` |
| Controller nội bộ | `<Resource>Controller` | `ProductController` |
| Controller public | `Public<Resource>Controller` | `PublicProductController` |

Tên endpoint dùng danh từ số nhiều dạng kebab-case:

```text
resources
resources/{id}
public/resources
products/{productId}/images
```

Package gốc:

```java
package com.nongthinh.agri_catalog_service;
```

### 3.2. HTTP và response

Service bọc response bằng:

```java
ApiResponse<T>
```

Quy ước status hiện tại:

| Thao tác | HTTP method | Status thành công |
|---|---|---|
| Danh sách | `GET /resources` | `200 OK` |
| Chi tiết | `GET /resources/{id}` | `200 OK` |
| Tạo | `POST /resources` | `201 Created` |
| Cập nhật toàn bộ trường cho phép sửa | `PUT /resources/{id}` | `200 OK` |
| Soft delete | `DELETE /resources/{id}` | `200 OK` |

Mẫu response:

```json
{
  "code": null,
  "message": "Resources retrieved successfully",
  "traceId": null,
  "result": {}
}
```

Không tự tạo một response envelope khác cho endpoint mới.

### 3.3. Security

- Endpoint yêu cầu đăng nhập phải có `@PreAuthorize` theo role/authority nghiệp vụ.
- Convention hiện tại dùng `hasAuthority('ROLE_ADMIN')`, `hasAuthority('ROLE_BRAND')`.
- Không mặc định mọi CRUD đều là Admin; xác định actor được phép theo use case.
- Endpoint public GET đặt dưới `/public/**`.
- `SecurityConfig` hiện chỉ permit GET cho `/public/**`; không đặt thao tác ghi dưới namespace này.
- Kiểm tra role ở controller chưa đủ cho dữ liệu có chủ sở hữu. Use case vẫn phải kiểm tra Brand/User hiện tại có được thao tác trên đúng resource hay không.

Ví dụ:

```java
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
```

Nếu resource thuộc Brand, cần đối chiếu `resource.getBrandId()` với user hiện tại trong application layer, tương tự nguyên tắc ownership của `Product`.

### 3.4. Validation và error

Request dùng Java `record` và Jakarta Validation. Giá trị `message` của annotation là **tên enum** trong `ErrorCode`, không phải câu mô tả:

```java
@NotBlank(message = "RESOURCE_NAME_REQUIRED")
@Size(max = 150, message = "RESOURCE_NAME_TOO_LONG")
String name
```

Sau đó khai báo:

```java
RESOURCE_NAME_REQUIRED(
        "VAL_RESOURCE_NAME_REQUIRED",
        "Resource name is required",
        ErrorType.VALIDATION
),
RESOURCE_NOT_FOUND(
        "NOT_FOUND_RESOURCE_NOT_FOUND",
        "Resource not found",
        ErrorType.NOT_FOUND
),
RESOURCE_CODE_ALREADY_EXISTS(
        "BUS_RESOURCE_CODE_ALREADY_EXISTS",
        "Resource code already exists",
        ErrorType.BUSINESS_RULE
),
```

`GlobalExceptionHandler` ánh xạ:

| `ErrorType` | HTTP status |
|---|---|
| `VALIDATION`, `BUSINESS_RULE` | `400 Bad Request` |
| `NOT_FOUND` | `404 Not Found` |
| `AUTHENTICATION` | `401 Unauthorized` |
| `AUTHORIZATION` | `403 Forbidden` |
| `INTERNAL_SERVICE`, `INFRASTRUCTURE` | `502 Bad Gateway` |
| `SYSTEM` | `500 Internal Server Error` |

Business rule thất bại phải ném:

```java
throw new BusinessException(ErrorCode.RESOURCE_CODE_ALREADY_EXISTS);
```

Không ném exception framework từ controller để biểu diễn business rule.

### 3.5. ID, thời gian và audit

Trong use case:

- Sinh ID qua `IdGenerator`, không gọi trực tiếp `UUID.randomUUID()`.
- Lấy thời gian qua `ClockProvider`, không gọi trực tiếp `Instant.now()`.
- Lấy actor qua `CurrentUserProvider`.
- `createdBy`, `updatedBy`, `deletedBy` lấy từ current user.

Ví dụ:

```java
UUID actorId = currentUserProvider.getCurrentUser().getUserId();
Instant now = clockProvider.now();
UUID id = idGenerator.generate();
```

Cách này giữ use case có thể kiểm thử bằng fake/mock deterministic.

### 3.6. Transaction

- Gắn `@Transactional` tại use case ghi: create, update, delete và thao tác thay đổi nhiều aggregate/bản ghi.
- Use case chỉ đọc đơn giản không bắt buộc transaction.
- Không đặt transaction orchestration ở controller.

### 3.7. Soft delete

Các bảng nghiệp vụ thông thường trong service dùng:

```text
deleted_at TIMESTAMPTZ
deleted_by UUID
```

Quy tắc:

- `DELETE` cập nhật `deletedAt`, `deletedBy`; không gọi hard delete.
- Tất cả query nghiệp vụ mặc định phải lọc `deleted_at IS NULL`.
- Kiểm tra unique trong application và unique index trong database đều chỉ xét bản ghi chưa xóa.
- `findById` của output port phải không trả bản ghi đã soft delete.
- Chỉ cascade soft delete khi đó là business rule rõ ràng; thực hiện trong cùng transaction.

Ví dụ Spring Data:

```java
Optional<JpaResourceEntity> findByIdAndDeletedAtIsNull(UUID id);

boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);

List<JpaResourceEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc();
```

Ví dụ PostgreSQL:

```sql
CREATE UNIQUE INDEX uq_resource_code_active
ON resources_table (LOWER(code))
WHERE deleted_at IS NULL;
```

Application check tạo lỗi dễ hiểu; database constraint bảo vệ trước race condition. Cần có cả hai.

## 4. Bộ file tối thiểu cho CRUD

Với CRUD đầy đủ, thường cần:

```text
domain/<resource>/Resource.java

application/command/ResourceCreationCommand.java
application/command/ResourceUpdateCommand.java
application/view/ResourceView.java

application/port/in/<resource>/CreateResourceUseCase.java
application/port/in/<resource>/GetResourceByIdUseCase.java
application/port/in/<resource>/ListResourcesUseCase.java
application/port/in/<resource>/UpdateResourceUseCase.java
application/port/in/<resource>/DeleteResourceUseCase.java

application/port/in/<resource>/impl/CreateResourceUseCaseImpl.java
application/port/in/<resource>/impl/GetResourceByIdUseCaseImpl.java
application/port/in/<resource>/impl/ListResourcesUseCaseImpl.java
application/port/in/<resource>/impl/UpdateResourceUseCaseImpl.java
application/port/in/<resource>/impl/DeleteResourceUseCaseImpl.java

application/port/out/repository/ResourceRepository.java

infra/persistence/<resource>/JpaResourceEntity.java
infra/persistence/<resource>/JpaResourceRepository.java
infra/persistence/<resource>/ResourcePersistenceMapper.java
infra/persistence/<resource>/ResourceRepositoryImpl.java

presentation/dto/request/ResourceCreationRequest.java
presentation/dto/request/ResourceUpdateRequest.java
presentation/mapper/ResourceMapper.java
presentation/controller/ResourceController.java
```

Tùy nghiệp vụ, bổ sung:

```text
presentation/controller/PublicResourceController.java
domain/<resource>/valueobject/...
application/port/out/<ExternalServicePort>.java
infra/client/<external-service>/...
src/main/resources/db/migration/V<n>__<description>.sql
```

Không bắt buộc tạo đủ năm CRUD use case nếu nghiệp vụ không cho phép thao tác tương ứng.

## 5. Trình tự triển khai khuyến nghị

### Bước 1: Chốt API và business rule

Trước khi viết code, xác định:

- Endpoint, method, request, response và status.
- Actor/role nào được gọi.
- Resource có owner hay không.
- Field nào client được nhập, field nào hệ thống quản lý.
- Unique key và phạm vi unique.
- Điều kiện public/visible.
- Delete là soft delete hay không.
- Quan hệ cha-con/cascade và điều kiện chặn delete.
- Có gọi service ngoài hoặc phát event hay không.

Không đưa các field do hệ thống quản lý như `id`, audit, moderation status vào request chỉ vì chúng tồn tại trong bảng.

### Bước 2: Migration

Nếu bảng/chỉ mục/constraint chưa tồn tại, tạo migration Flyway mới:

```text
src/main/resources/db/migration/V<n>__create_resources.sql
```

Không sửa migration đã chạy ở môi trường dùng chung. Số phiên bản phải lớn hơn migration mới nhất trong service.

Checklist migration:

- UUID primary key.
- Nullability và độ dài khớp request/JPA.
- Default chỉ dùng khi có ý nghĩa ở database.
- Foreign key nội bộ service.
- Check constraint cho invariant đơn giản.
- Partial unique index tương thích soft delete.
- Index cho filter/sort thường dùng.
- Audit và soft-delete columns nếu resource cần audit.

Các ID thuộc service khác là logical reference; không tạo foreign key xuyên database/service.

### Bước 3: Domain

Domain là Java thuần. Dùng factory `create`, `reconstruct` và method biểu diễn hành vi:

```java
public class Resource {

    private final UUID id;
    private String name;
    private String code;
    private boolean active;
    private final Instant createdAt;
    private final UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
    private Instant deletedAt;
    private UUID deletedBy;

    private Resource(
            UUID id,
            String name,
            String code,
            boolean active,
            Instant createdAt,
            UUID createdBy,
            Instant updatedAt,
            UUID updatedBy,
            Instant deletedAt,
            UUID deletedBy
    ) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.active = active;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static Resource create(
            UUID id,
            String name,
            String code,
            UUID actorId,
            Instant now
    ) {
        return new Resource(
                id,
                name,
                code,
                true,
                now,
                actorId,
                now,
                actorId,
                null,
                null
        );
    }

    public static Resource reconstruct(
            UUID id,
            String name,
            String code,
            boolean active,
            Instant createdAt,
            UUID createdBy,
            Instant updatedAt,
            UUID updatedBy,
            Instant deletedAt,
            UUID deletedBy
    ) {
        return new Resource(
                id,
                name,
                code,
                active,
                createdAt,
                createdBy,
                updatedAt,
                updatedBy,
                deletedAt,
                deletedBy
        );
    }

    public void update(
            String name,
            String code,
            boolean active,
            UUID actorId,
            Instant now
    ) {
        this.name = name;
        this.code = code;
        this.active = active;
        this.updatedAt = now;
        this.updatedBy = actorId;
    }

    public void delete(UUID actorId, Instant now) {
        this.deletedAt = now;
        this.deletedBy = actorId;
        this.updatedAt = now;
        this.updatedBy = actorId;
    }

    // Getters
}
```

Lưu ý:

- `create` đặt default nghiệp vụ.
- `reconstruct` chỉ dùng khi hydrate từ persistence, không áp lại default.
- Ưu tiên method có ý nghĩa như `publish`, `lock`, `approve`, `markPrimary` thay vì setter công khai.
- Truyền `now` và `actorId` từ application vào domain để deterministic.
- Invariant chỉ thuộc một aggregate nên được giữ trong domain; rule cần query repository/service ngoài được điều phối ở use case.

### Bước 4: Output port

Interface repository thuộc application:

```java
public interface ResourceRepository {

    boolean existsByCode(String code);

    Optional<Resource> findById(UUID id);

    List<Resource> findAll();

    List<Resource> findAllActive();

    Resource save(Resource resource);
}
```

Đặt tên theo nhu cầu nghiệp vụ, không để lộ framework:

- Tốt: `findAllPublishedOrderByPublishedAtDesc()`.
- Tránh: truyền `JpaSpecificationExecutor`, JPA entity hoặc persistence-specific query object ra application.

### Bước 5: Request, command, view và mapper

Request thuộc presentation:

```java
public record ResourceCreationRequest(
        @NotBlank(message = "RESOURCE_NAME_REQUIRED")
        @Size(max = 150, message = "RESOURCE_NAME_TOO_LONG")
        String name,

        @NotBlank(message = "RESOURCE_CODE_REQUIRED")
        @Size(max = 100, message = "RESOURCE_CODE_TOO_LONG")
        String code
) {
}
```

Command thuộc application và không chứa annotation HTTP/validation:

```java
public record ResourceCreationCommand(
        String name,
        String code
) {
}
```

View thuộc application:

```java
public record ResourceView(
        UUID id,
        String name,
        String code,
        boolean active,
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy
) {
    public static ResourceView from(Resource resource) {
        return new ResourceView(
                resource.getId(),
                resource.getName(),
                resource.getCode(),
                resource.isActive(),
                resource.getCreatedAt(),
                resource.getCreatedBy(),
                resource.getUpdatedAt(),
                resource.getUpdatedBy()
        );
    }
}
```

Presentation mapper dùng MapStruct:

```java
@Mapper(componentModel = "spring")
public interface ResourceMapper {

    ResourceCreationCommand toResourceCreationCommand(ResourceCreationRequest request);

    ResourceUpdateCommand toResourceUpdateCommand(ResourceUpdateRequest request);
}
```

Không dùng một model duy nhất cho request, domain, persistence và response.

### Bước 6: Input port và use case

Mỗi hành động có input port riêng:

```java
public interface CreateResourceUseCase {

    ResourceView execute(ResourceCreationCommand command);
}
```

Template create:

```java
@Service
@RequiredArgsConstructor
public class CreateResourceUseCaseImpl implements CreateResourceUseCase {

    private final ResourceRepository resourceRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ResourceView execute(ResourceCreationCommand command) {
        Objects.requireNonNull(command, "command is required");

        if (resourceRepository.existsByCode(command.code())) {
            throw new BusinessException(ErrorCode.RESOURCE_CODE_ALREADY_EXISTS);
        }

        UUID actorId = currentUserProvider.getCurrentUser().getUserId();
        Instant now = clockProvider.now();
        Resource resource = Resource.create(
                idGenerator.generate(),
                command.name(),
                command.code(),
                actorId,
                now
        );

        return ResourceView.from(resourceRepository.save(resource));
    }
}
```

Template get:

```java
@Service
@RequiredArgsConstructor
public class GetResourceByIdUseCaseImpl implements GetResourceByIdUseCase {

    private final ResourceRepository resourceRepository;

    @Override
    public ResourceView execute(UUID id) {
        return resourceRepository.findById(id)
                .map(ResourceView::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
```

Template update:

```java
@Service
@RequiredArgsConstructor
public class UpdateResourceUseCaseImpl implements UpdateResourceUseCase {

    private final ResourceRepository resourceRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ResourceView execute(UUID id, ResourceUpdateCommand command) {
        Objects.requireNonNull(command, "command is required");

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!resource.getCode().equalsIgnoreCase(command.code())
                && resourceRepository.existsByCode(command.code())) {
            throw new BusinessException(ErrorCode.RESOURCE_CODE_ALREADY_EXISTS);
        }

        resource.update(
                command.name(),
                command.code(),
                command.active(),
                currentUserProvider.getCurrentUser().getUserId(),
                clockProvider.now()
        );

        return ResourceView.from(resourceRepository.save(resource));
    }
}
```

Template delete:

```java
@Service
@RequiredArgsConstructor
public class DeleteResourceUseCaseImpl implements DeleteResourceUseCase {

    private final ResourceRepository resourceRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void execute(UUID id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // Kiểm tra reference/children/ownership trước khi xóa nếu nghiệp vụ yêu cầu.
        resource.delete(
                currentUserProvider.getCurrentUser().getUserId(),
                clockProvider.now()
        );
        resourceRepository.save(resource);
    }
}
```

### Bước 7: Persistence adapter

JPA entity phản ánh schema, không chứa business orchestration:

```java
@Entity
@Table(name = "resources_table")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaResourceEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;
}
```

Spring Data repository:

```java
@Repository
public interface JpaResourceRepository
        extends JpaRepository<JpaResourceEntity, UUID> {

    boolean existsByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    Optional<JpaResourceEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<JpaResourceEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    List<JpaResourceEntity>
            findAllByDeletedAtIsNullAndActiveTrueOrderByCreatedAtDesc();
}
```

Persistence mapper có hai chiều:

```java
@Mapper(componentModel = "spring")
public interface ResourcePersistenceMapper {

    default Resource toDomain(JpaResourceEntity entity) {
        return Resource.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    default JpaResourceEntity toEntity(Resource resource) {
        return JpaResourceEntity.builder()
                .id(resource.getId())
                .name(resource.getName())
                .code(resource.getCode())
                .active(resource.isActive())
                .createdAt(resource.getCreatedAt())
                .createdBy(resource.getCreatedBy())
                .updatedAt(resource.getUpdatedAt())
                .updatedBy(resource.getUpdatedBy())
                .deletedAt(resource.getDeletedAt())
                .deletedBy(resource.getDeletedBy())
                .build();
    }
}
```

Adapter implement output port:

```java
@Repository
@RequiredArgsConstructor
public class ResourceRepositoryImpl implements ResourceRepository {

    private final JpaResourceRepository jpaResourceRepository;
    private final ResourcePersistenceMapper resourcePersistenceMapper;

    @Override
    public boolean existsByCode(String code) {
        return jpaResourceRepository
                .existsByCodeIgnoreCaseAndDeletedAtIsNull(code);
    }

    @Override
    public Optional<Resource> findById(UUID id) {
        return jpaResourceRepository.findByIdAndDeletedAtIsNull(id)
                .map(resourcePersistenceMapper::toDomain);
    }

    @Override
    public List<Resource> findAll() {
        return jpaResourceRepository
                .findAllByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(resourcePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Resource> findAllActive() {
        return jpaResourceRepository
                .findAllByDeletedAtIsNullAndActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(resourcePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Resource save(Resource resource) {
        JpaResourceEntity saved = jpaResourceRepository.save(
                resourcePersistenceMapper.toEntity(resource)
        );
        return resourcePersistenceMapper.toDomain(saved);
    }
}
```

### Bước 8: Controller

Controller nội bộ:

```java
@RestController
@RequestMapping("resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ListResourcesUseCase listResourcesUseCase;
    private final GetResourceByIdUseCase getResourceByIdUseCase;
    private final CreateResourceUseCase createResourceUseCase;
    private final UpdateResourceUseCase updateResourceUseCase;
    private final DeleteResourceUseCase deleteResourceUseCase;
    private final ResourceMapper resourceMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<ResourceView>>> getResources() {
        List<ResourceView> result = listResourcesUseCase.execute(false);
        return ResponseEntity.ok(ApiResponse.<List<ResourceView>>builder()
                .message("Resources retrieved successfully")
                .result(result)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ResourceView>> getResourceById(
            @PathVariable UUID id
    ) {
        ResourceView result = getResourceByIdUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<ResourceView>builder()
                .message("Resource retrieved successfully")
                .result(result)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ResourceView>> createResource(
            @RequestBody @Valid ResourceCreationRequest request
    ) {
        ResourceView result = createResourceUseCase.execute(
                resourceMapper.toResourceCreationCommand(request)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ResourceView>builder()
                        .message("Resource created successfully")
                        .result(result)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ResourceView>> updateResource(
            @PathVariable UUID id,
            @RequestBody @Valid ResourceUpdateRequest request
    ) {
        ResourceView result = updateResourceUseCase.execute(
                id,
                resourceMapper.toResourceUpdateCommand(request)
        );
        return ResponseEntity.ok(ApiResponse.<ResourceView>builder()
                .message("Resource updated successfully")
                .result(result)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteResource(
            @PathVariable UUID id
    ) {
        deleteResourceUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Resource deleted successfully")
                .build());
    }
}
```

Controller public chỉ chứa read model được phép công khai:

```java
@RestController
@RequestMapping("public/resources")
@RequiredArgsConstructor
public class PublicResourceController {

    private final ListResourcesUseCase listResourcesUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResourceView>>> getActiveResources() {
        List<ResourceView> result = listResourcesUseCase.execute(true);
        return ResponseEntity.ok(ApiResponse.<List<ResourceView>>builder()
                .message("Resources retrieved successfully")
                .result(result)
                .build());
    }
}
```

Không trả bản ghi inactive, draft, locked, rejected hoặc soft-deleted qua public API nếu business rule không cho phép.

## 6. Filter, sort và pagination

Các endpoint hiện tại đang trả `List` và dùng repository method theo từng filter. Khi thêm filter:

- Đặt filter trong method của input port hoặc tạo query object nếu số lượng filter lớn.
- Đẩy filter xuống database; tránh tải toàn bộ dữ liệu rồi lọc bằng stream.
- Sort phải deterministic, ví dụ `displayOrder ASC, name ASC` hoặc `createdAt DESC, id DESC`.
- Public query phải áp điều kiện visibility ngay tại repository query.

Nếu bảng có thể tăng lớn, bổ sung pagination ngay từ đầu thay vì trả `List` không giới hạn. Giữ Spring `Pageable` ở rìa framework hoặc ánh xạ sang application query/page model nếu muốn giữ application độc lập framework hoàn toàn. Response page phải dùng một model thống nhất, không trả JPA `Page` trực tiếp nếu dự án đã có page response riêng.

## 7. Quan hệ với service ngoài

Clean Architecture yêu cầu application gọi service ngoài qua output port:

```text
application/port/out/FileServicePort.java
infra/client/fileservice/FileServiceAdapter.java
infra/client/fileservice/FileServiceClient.java
```

Không inject Feign client trực tiếp vào controller hoặc domain.

Use case chịu trách nhiệm:

- Gọi port.
- Ánh xạ lỗi hạ tầng thành `ErrorCode` phù hợp.
- Kiểm tra metadata/ownership cần thiết.
- Đảm bảo thứ tự thao tác và transaction boundary hợp lý.

Lưu ý: transaction database không rollback được một side effect đã thành công ở service khác. Với workflow quan trọng, cân nhắc idempotency, compensation hoặc outbox/event thay vì giả định một transaction phân tán.

## 8. Testing tối thiểu

Hiện service mới có context-load test; endpoint mới nên bổ sung test theo rủi ro.

### 8.1. Domain unit test

Kiểm tra:

- Factory đặt default đúng.
- Update thay đổi đúng field/audit.
- State transition hợp lệ và không hợp lệ.
- Soft delete ghi đúng actor/time.

### 8.2. Use case unit test

Mock/fake output port và provider để kiểm tra:

- Happy path.
- Not found.
- Duplicate unique key.
- Reference không tồn tại.
- Ownership/authorization.
- Rule chặn delete.
- Repository `save` nhận domain đúng.

### 8.3. Persistence adapter test

Kiểm tra:

- Mapping domain ↔ entity không mất field.
- Query không trả soft-deleted record.
- Query public chỉ trả record visible.
- Unique/index/constraint hoạt động đúng.
- Filter và sort đúng.

### 8.4. Controller test

Kiểm tra:

- Role đúng được truy cập; role sai nhận `403`.
- Public GET không cần JWT.
- Request không hợp lệ trả đúng `code`.
- Create trả `201`; các thao tác còn lại trả status theo convention.
- Response đúng `ApiResponse<T>`.

Chạy kiểm tra trên Windows:

```powershell
cd services/agri-catalog-service
.\mvnw.cmd test
```

Compile nhanh khi cần:

```powershell
cd services/agri-catalog-service
.\mvnw.cmd -DskipTests compile
```

## 9. Checklist review trước khi hoàn tất

### API

- [ ] Path là danh từ số nhiều dạng kebab-case.
- [ ] Status code đúng convention.
- [ ] Response dùng `ApiResponse<T>`.
- [ ] Request dùng `record`, `@Valid` và validation đầy đủ.
- [ ] Public endpoint nằm dưới `/public/**` và chỉ lộ dữ liệu được phép.
- [ ] `@PreAuthorize` đúng actor nghiệp vụ.

### Clean Architecture

- [ ] Domain không import Spring/JPA/presentation.
- [ ] Controller không gọi JPA repository.
- [ ] Use case không nhận request DTO và không trả JPA entity.
- [ ] Application phụ thuộc output port, adapter implement port.
- [ ] External service được bọc bởi output port.
- [ ] Mapping HTTP ↔ command và domain ↔ persistence tách riêng.

### Nghiệp vụ và dữ liệu

- [ ] Default được đặt trong domain factory.
- [ ] ID dùng `IdGenerator`.
- [ ] Time dùng `ClockProvider`.
- [ ] Audit dùng `CurrentUserProvider`.
- [ ] Use case ghi có `@Transactional`.
- [ ] Ownership được kiểm tra trong application.
- [ ] Mọi query nghiệp vụ lọc soft delete.
- [ ] Unique được kiểm tra cả application lẫn database.
- [ ] Foreign key, check constraint và index đã đủ.
- [ ] Không sửa migration đã áp dụng; tạo migration mới.

### Error handling

- [ ] Mọi validation message trỏ tới enum `ErrorCode` tồn tại.
- [ ] Not found dùng `ErrorType.NOT_FOUND`.
- [ ] Business conflict/rule dùng `ErrorType.BUSINESS_RULE`.
- [ ] Lỗi quyền dùng `ErrorType.AUTHORIZATION`.
- [ ] Không để exception framework/persistence rò ra API.

### Kiểm thử và tài liệu

- [ ] Có test happy path và các business rule quan trọng.
- [ ] Có test security/validation cho controller.
- [ ] `mvn test` thành công.
- [ ] Tài liệu endpoint/Postman được cập nhật nếu feature yêu cầu.

## 10. Những lỗi cần tránh

- Đặt `@Entity` trực tiếp lên domain.
- Inject `Jpa<Resource>Repository` vào use case hoặc controller.
- Trả JPA entity ra response.
- Dùng request DTO làm command/domain object.
- Viết business rule trong controller.
- Gọi `UUID.randomUUID()` hoặc `Instant.now()` trong use case/domain mới.
- Hard delete một bảng đang theo convention soft delete.
- Quên `DeletedAtIsNull` trong một query hoặc kiểm tra unique.
- Chỉ check duplicate ở application mà không có unique constraint/index.
- Cho client tự gửi `createdBy`, `updatedBy`, moderation status hoặc owner không được phép chọn.
- Chỉ kiểm tra `ROLE_BRAND` mà không kiểm tra Brand có sở hữu resource.
- Lọc tập dữ liệu lớn trong Java thay vì query database.
- Mở public endpoint nhưng tái sử dụng query trả cả draft/inactive/locked record.
- Sửa `V1` hoặc migration đã chạy thay vì thêm migration mới.
- Tạo một “generic CRUD service” che mất business rule chỉ để giảm số lượng class.

## 11. Definition of Done

Một endpoint được xem là hoàn tất khi:

1. API contract và quyền truy cập rõ ràng.
2. Dependency direction đúng Clean Architecture.
3. Business rule nằm ở domain/use case, không nằm ở controller/JPA adapter.
4. Persistence đảm bảo soft delete, unique, constraint, filter và sort.
5. Error trả đúng `ApiResponse` và `ErrorCode`.
6. Audit lấy từ provider, không tin dữ liệu audit từ client.
7. Test phù hợp với rủi ro chạy thành công.
8. Migration và tài liệu API liên quan đã được cập nhật.
