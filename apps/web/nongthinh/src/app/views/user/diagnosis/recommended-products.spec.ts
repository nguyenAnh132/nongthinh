import { TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { AgriCatalogApiService, DiseaseRecommendationPage } from '../../../core/api/agri-catalog-api.service';
import { RecommendedProducts } from './recommended-products';

describe('RecommendedProducts', () => {
  const list = vi.fn();

  beforeEach(async () => {
    list.mockReset().mockReturnValue(of({ result: page(0, false) }));
    await TestBed.configureTestingModule({
      imports: [RecommendedProducts],
      providers: [{ provide: AgriCatalogApiService, useValue: { listDiseaseRecommendations: list } }],
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
        product: { id: `product-${index * 10 + offset}`, name: 'Sản phẩm', slug: 'product', thumbnailUrl: null },
        treatment: { id: 'treatment', productId: 'product', diseaseId: 'disease', brandId: 'brand',
          effectivenessLevel: 'HIGH', priority: 0, dosage: null, applicationMethod: null,
          applicationTiming: null, frequencyInstruction: null, treatmentNote: null },
        averageRating: 4.5, reviewCount: 2,
      })),
      page: index, size: 10, totalElements: 20, totalPages: 2, hasNext,
    };
  }
});
