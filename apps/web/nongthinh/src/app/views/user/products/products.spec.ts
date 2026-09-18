import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { BehaviorSubject, Subject, of, throwError } from 'rxjs';
import { AgriCatalogApiService, ProductView } from '../../../core/api/agri-catalog-api.service';
import { ProfileApiService } from '../../../core/api/profile-api.service';
import { PublicProducts } from './products';
import { PublicProductDetailPage } from './product-detail';

describe('Public product catalog', () => {
  const queryParams = new BehaviorSubject(convertToParamMap({}));
  const params = new BehaviorSubject(convertToParamMap({ productId: 'product-1' }));
  const search = vi.fn();
  const getDetail = vi.fn();
  const product = { id: 'product-1', brandId: 'brand-1', name: 'Sản phẩm thử', reviewCount: 0, thumbnailUrl: null } as ProductView;
  const page = { items: [product], page: 0, size: 10, totalElements: 11, totalPages: 2, hasNext: true };

  beforeEach(async () => {
    queryParams.next(convertToParamMap({}));
    params.next(convertToParamMap({ productId: 'product-1' }));
    search.mockReset().mockReturnValue(of({ result: page }));
    getDetail.mockReset().mockReturnValue(of({ result: { product, categoryName: 'Thuốc BVTV', treatments: [] } }));
    await TestBed.configureTestingModule({
      imports: [PublicProducts, PublicProductDetailPage],
      providers: [provideRouter([]),
        { provide: ActivatedRoute, useValue: { queryParamMap: queryParams, paramMap: params, snapshot: {} } },
        { provide: AgriCatalogApiService, useValue: { searchPublicProducts: search, getPublicProduct: getDetail } },
        { provide: ProfileApiService, useValue: {
          getPublicBrandProfileByUserId: vi.fn(() => of({ result: { brandName: 'Nông Thịnh' } })),
        } },
      ],
    }).compileComponents();
  });

  it('renders public product cards linking to their detail pages', async () => {
    const fixture = TestBed.createComponent(PublicProducts);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(search).toHaveBeenCalledWith('', 0);
    expect(fixture.nativeElement.querySelector('.product-card').getAttribute('href')).toBe('/app/products/product-1');
    expect(fixture.nativeElement.querySelector('.product-placeholder-icon').getAttribute('src'))
      .toBe('/icons/business/box-open.png');
    expect(fixture.nativeElement.querySelector('.detail-link')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Chưa có đánh giá');
    expect(fixture.nativeElement.querySelector('.brand-name').textContent).toContain('Nông Thịnh');
  });

  it('searches product and disease names with one keyword and preserves it when paging', async () => {
    queryParams.next(convertToParamMap({ q: 'blast', page: '1' }));
    const fixture = TestBed.createComponent(PublicProducts);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(search).toHaveBeenCalledWith('blast', 1);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.componentInstance.changePage(2);
    expect(navigate).toHaveBeenCalledWith([], expect.objectContaining({ queryParams: { page: 2 }, queryParamsHandling: 'merge' }));
    fixture.componentInstance.keyword = ' New keyword ';
    fixture.componentInstance.search();
    expect(navigate).toHaveBeenLastCalledWith([], expect.objectContaining({
      queryParams: { q: 'New keyword', name: null, diseaseName: null, page: null },
    }));
  });

  it('ignores the old response when filters change and shows a retry on failure', async () => {
    const oldResponse = new Subject<unknown>();
    search.mockReturnValueOnce(oldResponse);
    const fixture = TestBed.createComponent(PublicProducts);
    fixture.detectChanges();
    await fixture.whenStable();
    search.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
    queryParams.next(convertToParamMap({ q: 'new' }));
    oldResponse.next({ result: page });
    fixture.detectChanges();
    expect(fixture.componentInstance.error()).toBe('Không thể tải danh sách sản phẩm.');
    expect(fixture.componentInstance.result()).toBeNull();
    fixture.componentInstance.load();
    expect(search).toHaveBeenLastCalledWith('new', 0);
    expect(fixture.componentInstance.result()?.items).toHaveLength(1);
  });

  it('shows an empty result without failing', async () => {
    search.mockReturnValue(of({ result: { ...page, items: [], totalElements: 0, totalPages: 0, hasNext: false } }));
    const fixture = TestBed.createComponent(PublicProducts);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Không tìm thấy sản phẩm phù hợp');
  });

  it('shows product instructions and named diseases using the public detail API', async () => {
    getDetail.mockReturnValue(of({ result: { product: { ...product, safetyInstruction: 'An toàn khi sử dụng' },
      categoryName: 'Thuốc BVTV', treatments: [{ diseaseName: 'Đạo ôn', treatment: { id: 't1', effectivenessLevel: 'HIGH', dosage: 'Theo nhãn' } }] } }));
    const fixture = TestBed.createComponent(PublicProductDetailPage);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(getDetail).toHaveBeenCalledWith('product-1');
    expect(fixture.nativeElement.querySelector('.back-link img').getAttribute('src'))
      .toBe('/icons/business/left.png');
    expect(fixture.nativeElement.textContent).toContain('An toàn khi sử dụng');
    expect(fixture.nativeElement.textContent).toContain('Đạo ôn');
    expect(fixture.nativeElement.textContent).toContain('Theo nhãn');
    expect(fixture.nativeElement.textContent).not.toContain('Cập nhật sản phẩm');
    expect(fixture.nativeElement.querySelector('.purchase-button')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Liên kết mua hàng');
    expect(fixture.componentInstance.safePurchaseUrl('javascript:alert(1)')).toBeNull();
  });

  it('shows the purchase button beside the public product name when a valid link exists', async () => {
    getDetail.mockReturnValue(of({ result: {
      product: { ...product, purchaseUrl: 'https://shop.example.test/products/product-1' },
      categoryName: 'Thuốc BVTV',
      treatments: [],
    } }));
    const fixture = TestBed.createComponent(PublicProductDetailPage);
    fixture.detectChanges();
    await fixture.whenStable();

    const titleRow = fixture.nativeElement.querySelector('.detail-title-row') as HTMLElement;
    const buyButton = titleRow.querySelector('.purchase-button') as HTMLAnchorElement;
    expect(titleRow.querySelector('h2')?.textContent).toContain('Sản phẩm thử');
    expect(buyButton.textContent?.trim()).toBe('Mua sản phẩm');
    expect(buyButton.href).toBe('https://shop.example.test/products/product-1');
  });

  it('clears the old detail when a new product cannot be loaded', async () => {
    const fixture = TestBed.createComponent(PublicProductDetailPage);
    fixture.detectChanges();
    await fixture.whenStable();
    getDetail.mockReturnValue(throwError(() => new Error('Product unavailable')));
    params.next(convertToParamMap({ productId: 'product-2' }));
    fixture.detectChanges();
    expect(fixture.componentInstance.detail()).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Sản phẩm không còn hiển thị hoặc không thể tải.');
  });
});
