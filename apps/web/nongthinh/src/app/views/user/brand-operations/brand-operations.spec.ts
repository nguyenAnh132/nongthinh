import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { BehaviorSubject, of, Subject, throwError } from 'rxjs';
import { AgriCatalogApiService, ProductView } from '../../../core/api/agri-catalog-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { FileApiService } from '../../../core/api/file-api.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { BrandOperations } from './brand-operations';
import { BrandProductList } from './components/product-list';

describe('Brand product workspace', () => {
  let params: BehaviorSubject<ReturnType<typeof convertToParamMap>>;
  let api: Record<string, ReturnType<typeof vi.fn>>;
  let navigate: ReturnType<typeof vi.fn>;
  let toast: Record<string, ReturnType<typeof vi.fn>>;

  beforeEach(async () => {
    params = new BehaviorSubject(convertToParamMap({}));
    navigate = vi.fn().mockResolvedValue(true);
    toast = { success: vi.fn(), error: vi.fn(), warning: vi.fn() };
    api = {
      listActiveCategories: vi.fn(() =>
        of({ result: [{ id: 'category-1', name: 'Phân bón', displayOrder: 0 }] }),
      ),
      listActiveCropTypes: vi.fn(() => of({ result: [] })),
      listApprovedDiseases: vi.fn(() => of({ result: [] })),
      listProducts: vi.fn(() => of({ result: [product()] })),
      getProduct: vi.fn(() => of({ result: product() })),
      listProductDiseaseTreatments: vi.fn(() => of({ result: [] })),
      listProductHistory: vi.fn(() => of({ result: [] })),
      updateProduct: vi.fn(() => of({ result: { ...product(), name: 'Đã sửa' } })),
      updateProductPublicationStatus: vi.fn(() =>
        of({ result: { ...product(), publicationStatus: 'draft' } }),
      ),
      createProduct: vi.fn(() => of({ result: product() })),
      createProductDiseaseTreatment: vi.fn(() => of({ result: {} })),
      updateProductDiseaseTreatment: vi.fn(() => of({ result: {} })),
      deleteProductDiseaseTreatment: vi.fn(() => of({ result: null })),
    };
    await TestBed.configureTestingModule({
      imports: [BrandOperations],
      providers: [
        { provide: AgriCatalogApiService, useValue: api },
        {
          provide: AuthService,
          useValue: {
            hasRole: () => false,
            currentUser: signal({
              userId: 'brand-1',
              profile: {
                profileId: 'profile-1',
                displayName: 'Nông nghiệp Xanh',
                avatarUrl: 'https://files.example.test/brand-logo.webp',
              },
            }),
          },
        },
        { provide: FileApiService, useValue: { upload: vi.fn() } },
        { provide: ToastService, useValue: toast },
        {
          provide: ActivatedRoute,
          useValue: { queryParamMap: params, snapshot: { queryParamMap: params.value } },
        },
        { provide: Router, useValue: { navigate } },
      ],
    }).compileComponents();
  });

  it('renders asynchronously loaded products without another user interaction', async () => {
    const pending = new Subject<{ result: ProductView[] }>();
    api['listProducts'].mockReturnValue(pending);
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.skeleton-list')).not.toBeNull();
    pending.next({ result: [product()] });
    pending.complete();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.skeleton-list')).toBeNull();
    expect(fixture.nativeElement.querySelector('.product-identity').textContent).toContain('Phân bón hữu cơ');
  });

  it('opens the company product list by default and separates disease navigation', () => {
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();
    expect(api['listProducts']).toHaveBeenCalledOnce();
    expect(fixture.nativeElement.querySelector('app-brand-product-list')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.list-heading h1').textContent.trim()).toBe('Sản phẩm');
    expect(fixture.nativeElement.querySelector('.workspace-identity app-user-avatar img').getAttribute('src')).toBe(
      'https://files.example.test/brand-logo.webp',
    );
    expect(fixture.nativeElement.querySelector('.workspace-icon')).toBeNull();
    expect(fixture.nativeElement.querySelectorAll('.nav-group-title')).toHaveLength(2);
    expect(fixture.nativeElement.querySelector('.product-identity').textContent).toContain(
      'Phân bón hữu cơ',
    );
    expect(fixture.nativeElement.querySelector('.product-image-placeholder img').getAttribute('src')).toBe(
      '/icons/business/image-slash.png',
    );
    expect(fixture.nativeElement.querySelector('.status-action--publish')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.product-rating').textContent).toContain('4.6');
    expect(fixture.nativeElement.querySelector('.product-rating').textContent).toContain('18 đánh giá');
  });

  it('groups product navigation actions inside the three-dot menu', () => {
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.actions-menu')).toBeNull();

    (fixture.nativeElement.querySelector('.more-actions-button') as HTMLButtonElement).click();
    fixture.detectChanges();
    const menu = fixture.nativeElement.querySelector('.actions-menu');
    expect(menu).not.toBeNull();
    expect(menu.textContent).toContain('Cập nhật');
    expect(menu.textContent).toContain('Chi tiết');
    expect(menu.textContent).toContain('Lịch sử');
  });

  it('loads a linked edit view and updates the existing product without sending ownership', () => {
    params.next(convertToParamMap({ view: 'edit', productId: 'product-1' }));
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(fixture.nativeElement.querySelector('.product-form').classList).not.toContain(
      'operation-card',
    );
    expect(fixture.nativeElement.querySelector('.publication-control').textContent).toContain(
      'Trạng thái hiển thị: Bản nháp',
    );
    expect(component.productForm.controls.name.value).toBe('Phân bón hữu cơ');
    component.productForm.controls.name.setValue('Đã sửa');
    component.createProduct();
    const [id, payload] = api['updateProduct'].mock.calls[0];
    expect(id).toBe('product-1');
    expect(payload.name).toBe('Đã sửa');
    expect(payload.brandId).toBeUndefined();
    expect(api['createProduct']).not.toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(
      [],
      expect.objectContaining({ queryParams: { view: 'detail', productId: 'product-1' } }),
    );
  });

  it('keeps the edit form and values when saving fails', () => {
    api['updateProduct'].mockReturnValue(throwError(() => new Error('Unavailable')));
    params.next(convertToParamMap({ view: 'edit', productId: 'product-1' }));
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();
    fixture.componentInstance.productForm.controls.name.setValue('Nội dung chưa lưu');
    fixture.componentInstance.createProduct();
    expect(fixture.componentInstance.productForm.controls.name.value).toBe('Nội dung chưa lưu');
    expect(fixture.componentInstance.savingProduct).toBe(false);
    expect(navigate).not.toHaveBeenCalled();
    expect(toast['error']).toHaveBeenCalled();
  });

  it('shows a retryable load error instead of a misleading empty product list', () => {
    api['listProducts'].mockReturnValue(throwError(() => new Error('Unavailable')));
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain(
      'Chưa tải được sản phẩm',
    );
  });

  it('shows product status and lets the brand publish it from the list', () => {
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.status').textContent).toContain('Bản nháp');
    const action = fixture.nativeElement.querySelector('.status-action') as HTMLButtonElement;
    expect(action.textContent).toContain('Đăng sản phẩm');
    action.click();
    fixture.detectChanges();

    expect(api['updateProductPublicationStatus']).toHaveBeenCalledWith('product-1', 'PUBLISHED');
    expect(fixture.nativeElement.querySelector('.status').textContent).toContain('Đã đăng');
    expect(toast['success']).toHaveBeenCalled();
  });

  it('synchronizes the published state when the mutation response fails after the database commit', () => {
    api['updateProductPublicationStatus'].mockReturnValue(
      throwError(() => new Error('Response interrupted')),
    );
    api['getProduct'].mockReturnValue(of({ result: { ...product(), publicationStatus: 'published' } }));
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('.status-action') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(api['getProduct']).toHaveBeenCalledWith('product-1');
    expect(fixture.nativeElement.querySelector('.status').textContent).toContain('Đã đăng');
    expect(toast['success']).toHaveBeenCalledWith(
      'Trạng thái sản phẩm đã được đồng bộ từ máy chủ.',
    );
  });

  it('ignores stale detail responses after navigation', () => {
    const pending = new Subject<{ result: ProductView }>();
    api['getProduct'].mockReturnValue(pending);
    params.next(convertToParamMap({ view: 'detail', productId: 'product-1' }));
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();
    params.next(convertToParamMap({ view: 'products' }));
    pending.next({ result: product() });
    pending.complete();
    fixture.detectChanges();
    expect(fixture.componentInstance.activeTab).toBe('products');
    expect(fixture.componentInstance.selectedProduct).toBeNull();
  });

  it('does not show the publication control on the product detail view', () => {
    params.next(convertToParamMap({ view: 'detail', productId: 'product-1' }));
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.product-status').textContent).toContain('Bản nháp');
    expect(fixture.nativeElement.querySelector('.publication-control')).toBeNull();
    expect(fixture.nativeElement.querySelector('.product-detail-view')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.product-detail-card')).toBeNull();
    expect(fixture.nativeElement.querySelector('.detail-rating').textContent).toContain('18 đánh giá');
    expect(fixture.nativeElement.querySelector('.purchase-button')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Liên kết mua hàng');
  });

  it('shows a buy button beside the product name only when a purchase URL exists', () => {
    api['getProduct'].mockReturnValue(
      of({ result: { ...product(), purchaseUrl: 'https://shop.example.test/products/product-1' } }),
    );
    params.next(convertToParamMap({ view: 'detail', productId: 'product-1' }));
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();

    const titleRow = fixture.nativeElement.querySelector('.detail-title-row') as HTMLElement;
    const buyButton = titleRow.querySelector('.purchase-button') as HTMLAnchorElement;
    expect(titleRow.querySelector('h2')?.textContent).toContain('Phân bón hữu cơ');
    expect(buyButton.textContent?.trim()).toBe('Mua sản phẩm');
    expect(buyButton.href).toBe('https://shop.example.test/products/product-1');
    expect(buyButton.target).toBe('_blank');
  });

  it('renders the product history without an outer operation card', () => {
    params.next(convertToParamMap({ view: 'history' }));
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();

    const historyPanel = fixture.nativeElement.querySelector('.product-history-panel');
    expect(historyPanel).not.toBeNull();
    expect(historyPanel.classList).not.toContain('operation-card');
  });

  it('recovers to the created product after a follow-up failure without offering duplicate creation', () => {
    api['createProductDiseaseTreatment'].mockReturnValue(
      throwError(() => new Error('Unavailable')),
    );
    params.next(convertToParamMap({ view: 'product' }));
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(fixture.nativeElement.querySelector('.product-form').classList).toContain(
      'operation-card',
    );
    component.productForm.patchValue({
      categoryId: 'category-1',
      name: 'Phân bón',
      slug: 'phan-bon',
    });
    component.treatmentRows.at(0).controls.diseaseId.setValue('disease-1');
    component.createProduct();
    expect(api['createProduct']).toHaveBeenCalledOnce();
    expect(toast['warning']).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(
      [],
      expect.objectContaining({ queryParams: { view: 'detail', productId: 'product-1' } }),
    );
    expect(component.productForm.pristine).toBe(true);
  });

  it('updates retained treatments and removes omitted treatments instead of recreating them', () => {
    api['listProductDiseaseTreatments'].mockReturnValue(
      of({
        result: [
          { id: 't-1', diseaseId: 'd-1', priority: 0 },
          { id: 't-2', diseaseId: 'd-2', priority: 0 },
        ],
      }),
    );
    params.next(convertToParamMap({ view: 'edit', productId: 'product-1' }));
    const fixture = TestBed.createComponent(BrandOperations);
    fixture.detectChanges();
    fixture.componentInstance.removeTreatmentRow(1);
    fixture.componentInstance.createProduct();
    expect(api['updateProductDiseaseTreatment']).toHaveBeenCalledWith(
      'product-1',
      't-1',
      expect.objectContaining({ priority: 0 }),
    );
    expect(api['deleteProductDiseaseTreatment']).toHaveBeenCalledWith('product-1', 't-2');
    expect(api['createProductDiseaseTreatment']).not.toHaveBeenCalled();
  });

  it('filters by Vietnamese name, category, and effective status before paging', () => {
    const list = new BrandProductList();
    list.products = Array.from({ length: 12 }, (_, index) => ({ ...product(), id: `p-${index}` }));
    list.products.push({ ...product(), id: 'locked', moderationStatus: 'LOCKED' });
    list.keyword = 'phan bon huu co';
    list.category = 'category-1';
    list.status = 'DRAFT';
    expect(list.filtered).toHaveLength(12);
    expect(list.visible).toHaveLength(10);
    list.page = 2;
    expect(list.visible).toHaveLength(2);
    list.status = 'LOCKED';
    expect(list.currentPage).toBe(1);
    expect(list.visible[0].id).toBe('locked');
  });

  function product(): ProductView {
    return {
      id: 'product-1',
      brandId: 'brand-1',
      categoryId: 'category-1',
      name: 'Phân bón hữu cơ',
      slug: 'phan-bon-huu-co',
      sku: 'PB-01',
      registrationNumber: null,
      manufacturerName: null,
      originCountry: 'Việt Nam',
      shortDescription: null,
      description: null,
      ingredients: null,
      usageInstruction: null,
      dosageInstruction: null,
      safetyInstruction: null,
      storageInstruction: null,
      warning: null,
      form: null,
      unit: null,
      packageSpecification: null,
      purchaseUrl: null,
      thumbnailUrl: null,
      publicationStatus: 'DRAFT',
      moderationStatus: 'NORMAL',
      featured: false,
      averageRating: 4.6,
      reviewCount: 18,
      createdAt: '2026-09-08T00:00:00Z',
      updatedAt: '2026-09-08T00:00:00Z',
    };
  }
});
