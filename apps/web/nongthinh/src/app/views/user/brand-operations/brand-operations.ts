import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, DestroyRef, OnDestroy, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, finalize, forkJoin, map, Observable, of, switchMap } from 'rxjs';
import {
  AgriCatalogApiService,
  CropTypeView,
  DiseaseView,
  EffectivenessLevel,
  ProductCategoryCreationPayload,
  ProductCategoryView,
  ProductCreationPayload,
  ProductDiseaseTreatmentPayload,
  ProductDiseaseTreatmentView,
  ProductHistoryAction,
  ProductHistoryView,
  ProductPublicationStatus,
  ProductView,
} from '../../../core/api/agri-catalog-api.service';
import { FileApiService, FileView } from '../../../core/api/file-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { apiErrorMessage, unwrapApiResult } from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';
import { UserAvatarComponent } from '../../../shared/user-avatar/user-avatar.component';
import { DiseaseManagement } from '../../shared/disease-management/disease-management';
import { BrandProductList, productStatus, productStatusLabel } from './components/product-list';

type OperationTab = 'products' | 'product' | 'category' | 'disease' | 'history' | 'detail' | 'edit';

@Component({
  selector: 'app-brand-operations',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DiseaseManagement,
    BrandProductList,
    UserAvatarComponent,
  ],
  templateUrl: './brand-operations.html',
  styleUrl: './brand-operations.scss',
})
export class BrandOperations implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly catalogApi = inject(AgriCatalogApiService);
  private readonly auth = inject(AuthService);
  private readonly fileApi = inject(FileApiService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cdr = inject(ChangeDetectorRef);
  private detailRequestId = 0;

  get businessName(): string {
    return this.auth.currentUser()?.profile?.displayName?.trim() || 'Doanh nghiệp';
  }

  get businessAvatarUrl(): string | null {
    return this.auth.currentUser()?.profile?.avatarUrl ?? null;
  }

  activeTab: OperationTab = 'products';
  brandProfileId: string | null = null;
  isAdmin = false;
  categories: ProductCategoryView[] = [];
  cropTypes: CropTypeView[] = [];
  approvedDiseases: DiseaseView[] = [];
  products: ProductView[] = [];
  productsLoaded = false;
  loadingProducts = false;
  expandedProductHistoryId: string | null = null;
  productHistoryLoadingId: string | null = null;
  readonly productHistories: Record<string, ProductHistoryView[]> = {};
  loading = true;
  savingCategory = false;
  savingProduct = false;
  selectedImage: File | null = null;
  imagePreviewUrl: string | null = null;
  imageError = '';
  productsError = '';
  detailError = '';
  loadingDetail = false;
  statusUpdatingId: string | null = null;
  selectedProduct: ProductView | null = null;
  treatments: ProductDiseaseTreatmentView[] = [];
  historyProductId: string | null = null;
  readonly historyErrors: Record<string, string> = {};
  readonly statusLabel = productStatusLabel;
  readonly statusKey = productStatus;
  readonly detailFields: { key: keyof ProductView; label: string }[] = [
    { key: 'sku', label: 'Mã SKU' },
    { key: 'registrationNumber', label: 'Số đăng ký' },
    { key: 'manufacturerName', label: 'Nhà sản xuất' },
    { key: 'originCountry', label: 'Xuất xứ' },
    { key: 'form', label: 'Dạng sản phẩm' },
    { key: 'unit', label: 'Đơn vị' },
    { key: 'packageSpecification', label: 'Quy cách đóng gói' },
    { key: 'slug', label: 'Đường dẫn (slug)' },
  ];
  readonly descriptionFields: { key: keyof ProductView; label: string }[] = [
    { key: 'description', label: 'Mô tả chi tiết' },
    { key: 'ingredients', label: 'Thành phần' },
    { key: 'usageInstruction', label: 'Hướng dẫn sử dụng' },
    { key: 'dosageInstruction', label: 'Liều lượng' },
    { key: 'safetyInstruction', label: 'Chỉ dẫn an toàn' },
    { key: 'storageInstruction', label: 'Bảo quản' },
    { key: 'warning', label: 'Cảnh báo' },
  ];

  get pageTitle(): string {
    return {
      products: 'Tất cả sản phẩm',
      product: 'Thêm sản phẩm',
      category: 'Danh mục sản phẩm',
      disease: 'Bệnh cây trồng',
      history: 'Lịch sử sản phẩm',
      detail: 'Chi tiết sản phẩm',
      edit: 'Cập nhật sản phẩm',
    }[this.activeTab];
  }

  get historyProducts(): ProductView[] {
    return this.historyProductId
      ? this.products.filter((product) => product.id === this.historyProductId)
      : this.products;
  }
  categoryLabel(id: string): string {
    return this.categories.find((category) => category.id === id)?.name ?? 'Danh mục chưa khả dụng';
  }
  diseaseLabel(id: string): string {
    return (
      this.approvedDiseases.find((disease) => disease.id === id)?.name ??
      'Bệnh không còn trong danh sách được duyệt'
    );
  }
  hasActiveCategory(id: string): boolean {
    return this.categories.some((item) => item.id === id);
  }
  hasApprovedDisease(id: string): boolean {
    return this.approvedDiseases.some((item) => item.id === id);
  }
  effectivenessLabel(value: EffectivenessLevel | null): string {
    return value
      ? { LOW: 'Thấp', MEDIUM: 'Trung bình', HIGH: 'Cao', VERY_HIGH: 'Rất cao' }[value]
      : 'Chưa xác định';
  }
  safePurchaseUrl(value: string | null): string | null {
    if (!value) return null;
    try {
      const url = new URL(value);
      return ['http:', 'https:'].includes(url.protocol) ? url.href : null;
    } catch {
      return null;
    }
  }

  publicationActionLabel(product: ProductView): string {
    return product.publicationStatus === 'PUBLISHED' ? 'Gỡ đăng' : 'Đăng sản phẩm';
  }

  updatePublicationStatus(product: ProductView, status?: ProductPublicationStatus): void {
    if (this.statusUpdatingId || product.moderationStatus === 'LOCKED') return;
    const nextStatus =
      status ?? (product.publicationStatus === 'PUBLISHED' ? 'UNPUBLISHED' : 'PUBLISHED');
    if (
      nextStatus === 'UNPUBLISHED' &&
      !window.confirm(`Gỡ đăng sản phẩm “${product.name}”? Sản phẩm sẽ không còn hiển thị công khai.`)
    )
      return;

    this.statusUpdatingId = product.id;
    this.catalogApi
      .updateProductPublicationStatus(product.id, nextStatus)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.statusUpdatingId = null;
          this.cdr.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          const responseProduct = unwrapApiResult<ProductView>(response);
          const updated = this.normalizeProduct({
            ...(responseProduct ?? product),
            publicationStatus: nextStatus,
          });
          this.products = this.products.map((item) => (item.id === updated.id ? updated : item));
          if (this.selectedProduct?.id === updated.id) this.selectedProduct = updated;
          delete this.productHistories[updated.id];
          this.toast.success(
            nextStatus === 'PUBLISHED'
              ? `Đã đăng sản phẩm “${updated.name}”.`
              : `Đã gỡ đăng sản phẩm “${updated.name}”.`,
          );
        },
        error: (error) => {
          this.syncProductState(product.id, nextStatus, error);
        },
      });
  }

  readonly categoryForm = this.fb.nonNullable.group({
    parentId: [''],
    name: ['', [Validators.required, Validators.maxLength(150)]],
    slug: ['', [Validators.required, Validators.maxLength(180)]],
    description: ['', [Validators.maxLength(10000)]],
    displayOrder: [0, [Validators.required, Validators.min(0)]],
  });

  readonly productForm = this.fb.nonNullable.group({
    brandId: ['', Validators.required],
    categoryId: ['', Validators.required],
    name: ['', [Validators.required, Validators.maxLength(255)]],
    slug: ['', [Validators.required, Validators.maxLength(280)]],
    sku: ['', Validators.maxLength(100)],
    registrationNumber: ['', Validators.maxLength(100)],
    manufacturerName: ['', Validators.maxLength(255)],
    originCountry: ['Việt Nam', Validators.maxLength(100)],
    shortDescription: ['', Validators.maxLength(500)],
    description: [''],
    ingredients: [''],
    usageInstruction: [''],
    dosageInstruction: [''],
    safetyInstruction: [''],
    storageInstruction: [''],
    warning: [''],
    form: ['', Validators.maxLength(100)],
    unit: ['', Validators.maxLength(50)],
    packageSpecification: ['', Validators.maxLength(255)],
    purchaseUrl: [''],
    imageAltText: ['', Validators.maxLength(255)],
  });

  readonly treatmentRows = this.fb.array([this.createTreatmentGroup()]);

  ngOnInit(): void {
    this.isAdmin = this.auth.hasRole('ADMIN');
    if (this.isAdmin) {
      this.activeTab = 'category';
      this.loading = false;
    } else {
      this.activeTab = 'products';
      const currentUser = this.auth.currentUser();
      const profile = currentUser?.profile;
      this.brandProfileId = profile?.profileId ?? null;
      this.productForm.controls.brandId.setValue(currentUser?.userId ?? '');
      this.loading = false;
    }
    this.loadCategories();
    if (!this.isAdmin) {
      this.loadCropTypes();
      this.loadApprovedDiseases();
      this.loadProducts();
      this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
        const view = params.get('view') as OperationTab;
        this.activeTab = ['products', 'product', 'disease', 'history', 'detail', 'edit'].includes(
          view,
        )
          ? view
          : 'products';
        const productId = params.get('productId');
        this.detailRequestId++;
        this.loadingDetail = false;
        this.detailError = '';
        if (this.activeTab === 'product') {
          this.selectedProduct = null;
          this.treatments = [];
          this.resetProductForm();
        } else if (this.activeTab === 'detail' || this.activeTab === 'edit') {
          if (productId) this.loadProductDetail(productId, this.activeTab === 'edit');
          else this.activeTab = 'products';
        }
        this.historyProductId = this.activeTab === 'history' ? productId : null;
        if (this.historyProductId) {
          this.expandedProductHistoryId = this.historyProductId;
          this.loadProductHistory(this.historyProductId);
        }
        this.cdr.markForCheck();
      });
    }
  }

  ngOnDestroy(): void {
    this.revokeImagePreview();
  }

  selectTab(tab: OperationTab, productId: string | null = null): void {
    if (this.savingProduct) return;
    if (
      (this.activeTab === 'product' || this.activeTab === 'edit') &&
      (this.productForm.dirty || this.treatmentRows.dirty || this.selectedImage) &&
      !window.confirm('Bạn có thay đổi chưa lưu. Rời khỏi biểu mẫu?')
    )
      return;
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { view: tab, productId },
    });
  }

  retryDetail(): void {
    const productId = this.route.snapshot.queryParamMap.get('productId');
    if (productId) this.loadProductDetail(productId, this.activeTab === 'edit');
  }

  private loadProductDetail(productId: string, editing: boolean): void {
    const requestId = ++this.detailRequestId;
    this.loadingDetail = true;
    this.detailError = '';
    this.selectedProduct = null;
    this.treatments = [];
    forkJoin({
      product: this.catalogApi.getProduct(productId),
      treatments: this.catalogApi.listProductDiseaseTreatments(productId),
    })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          if (requestId === this.detailRequestId) {
            this.loadingDetail = false;
            this.cdr.markForCheck();
          }
        }),
      )
      .subscribe({
        next: (response) => {
          if (requestId !== this.detailRequestId) return;
          const rawProduct = unwrapApiResult<ProductView>(response.product);
          const product = rawProduct ? this.normalizeProduct(rawProduct) : null;
          if (!product) {
            this.detailError = 'Không tìm thấy sản phẩm.';
            return;
          }
          this.selectedProduct = product;
          this.treatments =
            unwrapApiResult<ProductDiseaseTreatmentView[]>(response.treatments) ?? [];
          if (editing) {
            this.resetProductForm();
            const values = Object.fromEntries(
              Object.entries(product).map(([key, value]) => [key, value ?? '']),
            );
            this.productForm.patchValue(values);
            this.productForm.markAsPristine();
            this.treatmentRows.clear();
            for (const treatment of this.treatments) {
              const row = this.createTreatmentGroup();
              row.patchValue({
                ...treatment,
                effectivenessLevel: treatment.effectivenessLevel ?? '',
                dosage: treatment.dosage ?? '',
                applicationMethod: treatment.applicationMethod ?? '',
                applicationTiming: treatment.applicationTiming ?? '',
                frequencyInstruction: treatment.frequencyInstruction ?? '',
                treatmentNote: treatment.treatmentNote ?? '',
              });
              this.treatmentRows.push(row);
            }
          }
        },
        error: (error) => {
          if (requestId === this.detailRequestId)
            this.detailError = apiErrorMessage(error, 'Không thể tải chi tiết sản phẩm.');
        },
      });
  }

  createCategory(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    const value = this.categoryForm.getRawValue();
    const payload: ProductCategoryCreationPayload = {
      parentId: this.nullIfBlank(value.parentId),
      name: value.name.trim(),
      slug: value.slug.trim(),
      description: this.nullIfBlank(value.description),
      displayOrder: Number(value.displayOrder),
    };

    this.savingCategory = true;
    this.catalogApi
      .createCategory(payload)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.savingCategory = false;
          this.cdr.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          const category = unwrapApiResult<ProductCategoryView>(response);
          if (!category) {
            this.toast.error('Máy chủ không trả về danh mục vừa tạo.');
            return;
          }
          this.categories = [...this.categories, category].sort(this.sortCategories);
          this.productForm.controls.categoryId.setValue(category.id);
          this.categoryForm.reset({
            parentId: '',
            name: '',
            slug: '',
            description: '',
            displayOrder: 0,
          });
          this.toast.success(`Đã tạo danh mục “${category.name}”.`);
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể tạo danh mục.')),
      });
  }

  createProduct(): void {
    if (this.savingProduct || this.loadingDetail) return;
    const editingProduct = this.activeTab === 'edit' ? this.selectedProduct : null;
    if (this.activeTab === 'edit' && !editingProduct) return;
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      this.toast.error('Vui lòng kiểm tra các thông tin bắt buộc và độ dài nội dung.');
      return;
    }
    if (this.imageError) return;
    if (this.treatmentRows.invalid) {
      this.treatmentRows.markAllAsTouched();
      this.toast.error('Vui lòng kiểm tra thứ tự ưu tiên và độ dài thông tin điều trị.');
      return;
    }

    const value = this.productForm.getRawValue();
    if (!value.name.trim() || !value.slug.trim()) {
      this.toast.error('Vui lòng nhập tên và đường dẫn sản phẩm.');
      return;
    }
    const payload: ProductCreationPayload = {
      brandId: value.brandId,
      categoryId: value.categoryId,
      name: value.name.trim(),
      slug: value.slug.trim(),
      sku: this.nullIfBlank(value.sku),
      registrationNumber: this.nullIfBlank(value.registrationNumber),
      manufacturerName: this.nullIfBlank(value.manufacturerName),
      originCountry: this.nullIfBlank(value.originCountry),
      shortDescription: this.nullIfBlank(value.shortDescription),
      description: this.nullIfBlank(value.description),
      ingredients: this.nullIfBlank(value.ingredients),
      usageInstruction: this.nullIfBlank(value.usageInstruction),
      dosageInstruction: this.nullIfBlank(value.dosageInstruction),
      safetyInstruction: this.nullIfBlank(value.safetyInstruction),
      storageInstruction: this.nullIfBlank(value.storageInstruction),
      warning: this.nullIfBlank(value.warning),
      form: this.nullIfBlank(value.form),
      unit: this.nullIfBlank(value.unit),
      packageSpecification: this.nullIfBlank(value.packageSpecification),
      purchaseUrl: this.nullIfBlank(value.purchaseUrl),
    };

    let createdProduct: ProductView | null = null;
    this.savingProduct = true;
    const { brandId, ...updatePayload } = payload;
    const request = editingProduct
      ? this.catalogApi.updateProduct(editingProduct.id, updatePayload)
      : this.catalogApi.createProduct(payload);
    request
      .pipe(
        switchMap((response) => {
          const product = unwrapApiResult<ProductView>(response);
          if (!product) throw new Error('PRODUCT_RESPONSE_EMPTY');
          createdProduct = product;
          const operations = this.buildProductFollowUpOperations(
            product,
            value.imageAltText,
            Boolean(editingProduct),
          );
          return operations.length
            ? forkJoin(
                operations.map((operation) =>
                  operation.pipe(
                    map(() => true),
                    catchError(() => of(false)),
                  ),
                ),
              ).pipe(
                map((results) => {
                  if (results.some((result) => !result))
                    throw new Error('PRODUCT_FOLLOW_UP_FAILED');
                  return product;
                }),
              )
            : of(product);
        }),
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.savingProduct = false;
          this.cdr.markForCheck();
        }),
      )
      .subscribe({
        next: (product) => {
          this.toast.success(
            `Đã ${editingProduct ? 'cập nhật' : 'tạo'} sản phẩm “${product.name}”.`,
          );
          delete this.productHistories[product.id];
          this.resetProductForm();
          this.loadProducts();
          void this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { view: 'detail', productId: product.id },
          });
        },
        error: (error) => {
          if (createdProduct) {
            this.toast.warning(
              `Thông tin sản phẩm “${createdProduct.name}” đã được lưu, nhưng một số ảnh hoặc thông tin điều trị chưa thể lưu. Vui lòng kiểm tra lại trong phần cập nhật.`,
              { duration: 7500 },
            );
            delete this.productHistories[createdProduct.id];
            this.resetProductForm();
            this.loadProducts();
            void this.router.navigate([], {
              relativeTo: this.route,
              queryParams: { view: 'detail', productId: createdProduct.id },
            });
            return;
          }
          const fallback =
            error instanceof Error && error.message.endsWith('_EMPTY')
              ? 'Máy chủ trả về dữ liệu không hợp lệ.'
              : 'Không thể lưu sản phẩm.';
          this.toast.error(apiErrorMessage(error, fallback), { duration: 6500 });
        },
      });
  }

  onCategoryNameInput(): void {
    const slugControl = this.categoryForm.controls.slug;
    if (!slugControl.dirty) {
      slugControl.setValue(this.toSlug(this.categoryForm.controls.name.value));
    }
  }

  onProductNameInput(): void {
    if (this.activeTab === 'edit') return;
    const slugControl = this.productForm.controls.slug;
    if (!slugControl.dirty) {
      slugControl.setValue(this.toSlug(this.productForm.controls.name.value));
    }
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.imageError = '';
    this.selectedImage = null;
    this.revokeImagePreview();
    if (!file) return;

    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      this.imageError = 'Chỉ chấp nhận ảnh JPEG, PNG hoặc WebP.';
      input.value = '';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.imageError = 'Ảnh không được vượt quá 5 MB.';
      input.value = '';
      return;
    }
    this.selectedImage = file;
    this.imagePreviewUrl = URL.createObjectURL(file);
  }

  addTreatmentRow(): void {
    this.treatmentRows.push(this.createTreatmentGroup());
    this.treatmentRows.markAsDirty();
  }

  removeTreatmentRow(index: number): void {
    this.treatmentRows.markAsDirty();
    if (this.treatmentRows.length === 1) {
      this.treatmentRows.at(0).reset({
        diseaseId: '',
        effectivenessLevel: '',
        priority: 0,
        dosage: '',
        applicationMethod: '',
        applicationTiming: '',
        frequencyInstruction: '',
        treatmentNote: '',
      });
      return;
    }
    this.treatmentRows.removeAt(index);
  }

  isDiseaseSelectedElsewhere(diseaseId: string, currentIndex: number): boolean {
    return this.treatmentRows.controls.some(
      (control, index) => index !== currentIndex && control.controls.diseaseId.value === diseaseId,
    );
  }

  toggleProductHistory(product: ProductView): void {
    if (this.expandedProductHistoryId === product.id) {
      this.expandedProductHistoryId = null;
      return;
    }
    this.expandedProductHistoryId = product.id;
    if (!this.productHistories[product.id]) {
      this.loadProductHistory(product.id);
    }
  }

  productActionLabel(action: ProductHistoryAction): string {
    return {
      CREATED: 'Tạo sản phẩm',
      UPDATED: 'Cập nhật sản phẩm',
      PUBLISHED: 'Đăng sản phẩm',
      UNPUBLISHED: 'Gỡ đăng',
      LOCKED: 'Khóa sản phẩm',
      UNLOCKED: 'Mở khóa',
      DELETED: 'Xóa sản phẩm',
      RESTORED: 'Khôi phục sản phẩm',
    }[action];
  }

  historyActorLabel(actorType: ProductHistoryView['actorType']): string {
    return {
      ADMIN: 'Quản trị viên',
      BRAND: 'Thương hiệu',
      SYSTEM: 'Hệ thống',
    }[actorType];
  }

  cropTypeLabel(cropTypeId: string): string {
    const cropType = this.cropTypes.find((item) => item.id === cropTypeId);
    return cropType ? `${cropType.name} (${cropType.code})` : cropTypeId;
  }

  private loadCategories(): void {
    const request = this.isAdmin
      ? this.catalogApi.listCategories()
      : this.catalogApi.listActiveCategories();

    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cdr.markForCheck()),
      )
      .subscribe({
        next: (response) => {
          this.categories = (unwrapApiResult<ProductCategoryView[]>(response) ?? []).sort(
            this.sortCategories,
          );
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể tải danh sách danh mục.')),
      });
  }

  private loadApprovedDiseases(): void {
    this.catalogApi
      .listApprovedDiseases()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cdr.markForCheck()),
      )
      .subscribe({
        next: (response) => {
          this.approvedDiseases = (unwrapApiResult<DiseaseView[]>(response) ?? []).sort((a, b) =>
            a.name.localeCompare(b.name, 'vi'),
          );
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể tải danh sách bệnh đã được duyệt.')),
      });
  }

  private loadCropTypes(): void {
    this.catalogApi
      .listActiveCropTypes()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.cdr.markForCheck()),
      )
      .subscribe({
        next: (response) => {
          this.cropTypes = unwrapApiResult<CropTypeView[]>(response) ?? [];
        },
        error: (error) =>
          this.toast.error(apiErrorMessage(error, 'Không thể tải danh mục loại cây trồng.')),
      });
  }

  loadProducts(): void {
    if (this.loadingProducts) return;
    this.loadingProducts = true;
    this.productsError = '';
    this.catalogApi
      .listProducts()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.loadingProducts = false;
          this.cdr.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          this.products = (unwrapApiResult<ProductView[]>(response) ?? []).map((product) =>
            this.normalizeProduct(product),
          );
          if (this.selectedProduct) {
            this.selectedProduct =
              this.products.find((product) => product.id === this.selectedProduct?.id) ??
              this.selectedProduct;
          }
          this.productsLoaded = true;
        },
        error: (error) => {
          this.productsError = apiErrorMessage(error, 'Không thể tải danh sách sản phẩm.');
        },
      });
  }

  loadProductHistory(productId: string): void {
    this.productHistoryLoadingId = productId;
    this.historyErrors[productId] = '';
    this.catalogApi
      .listProductHistory(productId)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          if (this.productHistoryLoadingId === productId) this.productHistoryLoadingId = null;
          this.cdr.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          this.productHistories[productId] = unwrapApiResult<ProductHistoryView[]>(response) ?? [];
        },
        error: (error) => {
          this.historyErrors[productId] = apiErrorMessage(error, 'Không thể tải lịch sử sản phẩm.');
        },
      });
  }

  private resetProductForm(): void {
    const selectedCategory = this.productForm.controls.categoryId.value;
    this.productForm.reset({
      brandId: this.productForm.controls.brandId.value,
      categoryId: selectedCategory,
      name: '',
      slug: '',
      sku: '',
      registrationNumber: '',
      manufacturerName: '',
      originCountry: 'Việt Nam',
      shortDescription: '',
      description: '',
      ingredients: '',
      usageInstruction: '',
      dosageInstruction: '',
      safetyInstruction: '',
      storageInstruction: '',
      warning: '',
      form: '',
      unit: '',
      packageSpecification: '',
      purchaseUrl: '',
      imageAltText: '',
    });
    this.selectedImage = null;
    this.imageError = '';
    this.revokeImagePreview();
    this.treatmentRows.clear();
    this.treatmentRows.push(this.createTreatmentGroup());
    this.treatmentRows.markAsPristine();
  }

  private createTreatmentGroup() {
    return this.fb.nonNullable.group({
      diseaseId: [''],
      effectivenessLevel: ['' as '' | EffectivenessLevel],
      priority: [0, [Validators.required, Validators.min(0)]],
      dosage: ['', Validators.maxLength(255)],
      applicationMethod: [''],
      applicationTiming: [''],
      frequencyInstruction: ['', Validators.maxLength(255)],
      treatmentNote: [''],
    });
  }

  private buildProductFollowUpOperations(
    product: ProductView,
    imageAltText: string,
    editing = false,
  ): Observable<unknown>[] {
    const operations: Observable<unknown>[] = this.treatmentRows.controls
      .filter((control) => !!control.controls.diseaseId.value)
      .map((control) => {
        const row = control.getRawValue();
        const payload: ProductDiseaseTreatmentPayload = {
          diseaseId: row.diseaseId,
          effectivenessLevel: row.effectivenessLevel || null,
          priority: Number(row.priority),
          dosage: this.nullIfBlank(row.dosage),
          applicationMethod: this.nullIfBlank(row.applicationMethod),
          applicationTiming: this.nullIfBlank(row.applicationTiming),
          frequencyInstruction: this.nullIfBlank(row.frequencyInstruction),
          treatmentNote: this.nullIfBlank(row.treatmentNote),
        };
        const existing = editing
          ? this.treatments.find((item) => item.diseaseId === row.diseaseId)
          : null;
        if (existing) {
          const { diseaseId, ...updatePayload } = payload;
          return this.catalogApi.updateProductDiseaseTreatment(
            product.id,
            existing.id,
            updatePayload,
          );
        }
        return this.catalogApi.createProductDiseaseTreatment(product.id, payload);
      });

    if (editing) {
      const retained = new Set(this.treatmentRows.getRawValue().map((row) => row.diseaseId));
      for (const treatment of this.treatments) {
        if (!retained.has(treatment.diseaseId))
          operations.push(this.catalogApi.deleteProductDiseaseTreatment(product.id, treatment.id));
      }
    }

    if (this.selectedImage) {
      operations.push(
        this.fileApi.upload(this.selectedImage, 'PRODUCT_IMAGE').pipe(
          switchMap((fileResponse) => {
            const file = unwrapApiResult<FileView>(fileResponse);
            if (!file) throw new Error('FILE_RESPONSE_EMPTY');
            return this.catalogApi.attachProductImage(
              product.id,
              file.id,
              this.nullIfBlank(imageAltText),
            );
          }),
        ),
      );
    }
    return operations;
  }

  private revokeImagePreview(): void {
    if (this.imagePreviewUrl) URL.revokeObjectURL(this.imagePreviewUrl);
    this.imagePreviewUrl = null;
  }

  private syncProductState(
    productId: string,
    expectedStatus: ProductPublicationStatus,
    requestError: unknown,
  ): void {
    this.catalogApi
      .getProduct(productId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          const rawProduct = unwrapApiResult<ProductView>(response);
          if (!rawProduct) return;
          const product = this.normalizeProduct(rawProduct);
          this.products = this.products.map((item) => (item.id === product.id ? product : item));
          if (this.selectedProduct?.id === product.id) this.selectedProduct = product;
          if (product.publicationStatus === expectedStatus) {
            this.toast.success('Trạng thái sản phẩm đã được đồng bộ từ máy chủ.');
          } else {
            this.toast.error(
              apiErrorMessage(requestError, 'Không thể cập nhật trạng thái sản phẩm.'),
            );
          }
          this.cdr.markForCheck();
        },
        error: () =>
          this.toast.error(apiErrorMessage(requestError, 'Không thể cập nhật trạng thái sản phẩm.')),
      });
  }

  private normalizeProduct(product: ProductView): ProductView {
    return {
      ...product,
      publicationStatus: String(product.publicationStatus ?? 'DRAFT').toUpperCase(),
      moderationStatus: String(product.moderationStatus ?? 'NORMAL').toUpperCase(),
      averageRating: Number.isFinite(Number(product.averageRating))
        ? Math.max(0, Math.min(5, Number(product.averageRating)))
        : 0,
      reviewCount: Number.isFinite(Number(product.reviewCount))
        ? Math.max(0, Math.trunc(Number(product.reviewCount)))
        : 0,
    };
  }

  private nullIfBlank(value: string): string | null {
    const trimmed = value.trim();
    return trimmed ? trimmed : null;
  }

  private toSlug(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/đ/g, 'd')
      .replace(/Đ/g, 'D')
      .toLowerCase()
      .trim()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
  }

  private readonly sortCategories = (
    left: ProductCategoryView,
    right: ProductCategoryView,
  ): number => left.displayOrder - right.displayOrder || left.name.localeCompare(right.name, 'vi');
}
