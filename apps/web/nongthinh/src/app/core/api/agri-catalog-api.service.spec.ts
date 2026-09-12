import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AgriCatalogApiService } from './agri-catalog-api.service';

describe('AgriCatalogApiService disease review histories', () => {
  let service: AgriCatalogApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AgriCatalogApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('always forces BRAND and serializes only meaningful filters', () => {
    service
      .listDiseaseReviewHistories({
        keyword: '  rice blast  ',
        action: 'APPROVED',
        newStatus: null,
        actorType: undefined,
        from: '',
        page: 0,
        size: 20,
      })
      .subscribe();

    const request = http.expectOne(
      (candidate) =>
        candidate.url === '/api/v1/agri-catalog/disease-review-histories',
    );
    expect(request.request.params.get('createdSource')).toBe('BRAND');
    expect(request.request.params.get('keyword')).toBe('rice blast');
    expect(request.request.params.get('action')).toBe('APPROVED');
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('20');
    expect(request.request.params.has('newStatus')).toBe(false);
    expect(request.request.params.has('actorType')).toBe(false);
    expect(request.request.params.has('from')).toBe(false);
    request.flush({ result: { items: [], page: 0, size: 20, totalElements: 0, totalPages: 0, hasNext: false } });
  });

  it('restores a hidden disease through the restore endpoint', () => {
    service.restoreDisease('disease-1').subscribe();

    const request = http.expectOne(
      '/api/v1/agri-catalog/diseases/disease-1/restore',
    );
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush({ result: null });
  });
});
