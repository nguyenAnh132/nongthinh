import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DiagnosisApiService } from './diagnosis-api.service';

describe('DiagnosisApiService', () => {
  let service: DiagnosisApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DiagnosisApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends only cropTypeId and fileIds when requesting a diagnosis', () => {
    service.create({ cropTypeId: 'crop-1', fileIds: ['file-1', 'file-2'] }).subscribe();

    const request = http.expectOne('/api/v1/diagnoses');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      cropTypeId: 'crop-1',
      fileIds: ['file-1', 'file-2'],
    });
    expect(request.request.body).not.toHaveProperty('modelId');
    expect(request.request.body).not.toHaveProperty('modelVersionId');
    expect(request.request.body).not.toHaveProperty('artifactFileId');
    request.flush({ result: null });
  });

  it('uses farmer-only history endpoints', () => {
    service.listHistory().subscribe();
    const listRequest = http.expectOne('/api/v1/diagnoses/history');
    expect(listRequest.request.method).toBe('GET');
    listRequest.flush({ result: [] });

    service.getHistory('diagnosis-1').subscribe();
    const detailRequest = http.expectOne('/api/v1/diagnoses/history/diagnosis-1');
    expect(detailRequest.request.method).toBe('GET');
    detailRequest.flush({ result: null });
  });
});
