import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, Subject } from 'rxjs';
import { AgriCatalogApiService, DiseaseRecommendationPage } from '../../../core/api/agri-catalog-api.service';
import { ProfileApiService } from '../../../core/api/profile-api.service';
import { RecommendedProducts } from './recommended-products';

describe('RecommendedProducts', () => {
  const list = vi.fn();
  const getBrandProfile = vi.fn();

  beforeEach(async () => {
    list.mockReset().mockReturnValue(of({ result: page(0, false) }));
    getBrandProfile.mockReset().mockReturnValue(of({ result: { brandName: 'Nông Thịnh' } }));
    await TestBed.configureTestingModule({
      imports: [RecommendedProducts],
      providers: [
        provideRouter([]),
        { provide: AgriCatalogApiService, useValue: { listDiseaseRecommendations: list } },
        { provide: ProfileApiService, useValue: {
          getPublicBrandProfileByUserId: getBrandProfile,
        } },
      ],
    }).compileComponents();
  });

  async function setup() {
    const fixture = TestBed.createComponent(RecommendedProducts);
    fixture.componentRef.setInput('groups', ['disease-1', 'disease-2'].map(id => ({
      status: 'DISEASED', disease: { id, displayName: id },
    })));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture;
  }

  it('loads ten cards on open and requests the next page at 70%, once while pending', async () => {
    list.mockReturnValueOnce(of({ result: page(0, true) }));
    const fixture = await setup();
    const component = fixture.componentInstance;
    expect(list).toHaveBeenCalledWith('disease-1', 0);
    expect(fixture.nativeElement.querySelectorAll('.product-card')).toHaveLength(10);
    expect(fixture.nativeElement.textContent).not.toContain('Ưu tiên mức hiệu quả điều trị');
    expect(fixture.nativeElement.querySelector('.product-card').getAttribute('href'))
      .toBe('/app/products/product-0');
    expect(fixture.nativeElement.querySelector('.product-placeholder-icon').getAttribute('src'))
      .toBe('/icons/business/box-open.png');
    expect(fixture.nativeElement.querySelector('.brand-name').textContent).toContain('Nông Thịnh');
    expect(fixture.nativeElement.textContent).not.toContain('Liều lượng:');
    const viewport = fixture.nativeElement.querySelector('.recommendations-viewport');
    Object.defineProperties(viewport, {
      clientHeight: { value: 200 }, scrollHeight: { value: 1000 },
      scrollTop: { value: 499, writable: true },
    });
    component.onScroll();
    expect(list).toHaveBeenCalledTimes(1);
    const pending = new Subject<{ result: DiseaseRecommendationPage }>();
    list.mockReturnValue(pending);
    viewport.scrollTop = 500;
    component.onScroll();
    component.onScroll();
    expect(list).toHaveBeenCalledTimes(2);
    expect(list).toHaveBeenLastCalledWith('disease-1', 1);
    pending.next({ result: page(1, false) });
    pending.complete();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('.product-card')).toHaveLength(20);
    component.onScroll();
    expect(list).toHaveBeenCalledTimes(2);
    fixture.destroy();
  });

  it('keeps loaded cards on failure and retries the same page', async () => {
    list.mockReturnValueOnce(of({ result: page(0, true) }));
    const fixture = await setup();
    const component = fixture.componentInstance;
    const pending = new Subject<{ result: DiseaseRecommendationPage }>();
    list.mockReturnValue(pending);
    component.loadMore();
    pending.error(new Error('Unavailable'));
    expect(component.products).toHaveLength(10);
    expect(component.nextPage).toBe(1);
    component.onScroll();
    expect(list).toHaveBeenCalledTimes(2);
    list.mockReturnValue(of({ result: page(1, false) }));
    component.loadMore();
    expect(list).toHaveBeenLastCalledWith('disease-1', 1);
    expect(component.products).toHaveLength(20);
    fixture.destroy();
  });

  it('cancels the previous disease request and resets pagination on disease change', async () => {
    const pending = new Subject<{ result: DiseaseRecommendationPage }>();
    list.mockReturnValueOnce(pending);
    const fixture = await setup();
    const component = fixture.componentInstance;
    component.selectDisease('disease-2');
    pending.next({ result: page(1, true) });
    expect(list).toHaveBeenLastCalledWith('disease-2', 0);
    expect(component.products[0].product.id).toBe('product-0');
    expect(component.nextPage).toBe(1);
    expect(component.hasNext).toBe(false);
    fixture.destroy();
  });

  it('does not request products for a healthy or unmapped diagnosis', async () => {
    const fixture = TestBed.createComponent(RecommendedProducts);
    fixture.componentRef.setInput('groups', [{ status: 'HEALTHY', disease: null }]);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(list).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('chưa xác định bệnh');
    fixture.destroy();
  });

  function page(index: number, hasNext: boolean): DiseaseRecommendationPage {
    return {
      items: Array.from({ length: 10 }, (_, offset) => ({
        product: { id: `product-${index * 10 + offset}`, brandId: 'brand', name: 'Sản phẩm',
          slug: 'product', thumbnailUrl: null, shortDescription: 'Mô tả sản phẩm',
          manufacturerName: 'Nhà sản xuất' },
        treatment: { id: 'treatment', productId: 'product', diseaseId: 'disease', brandId: 'brand',
          effectivenessLevel: 'HIGH', priority: 0, dosage: null, applicationMethod: null,
          applicationTiming: null, frequencyInstruction: null, treatmentNote: null },
        averageRating: 4.5, reviewCount: 2,
      })),
      page: index, size: 10, totalElements: 20, totalPages: 2, hasNext,
    };
  }
});
