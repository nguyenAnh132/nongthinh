import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { FileApiService } from './file-api.service';

describe('FileApiService', () => {
  let service: FileApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(FileApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads authenticated private file content as a blob', () => {
    service.getContent('file-1').subscribe();

    const request = http.expectOne('/api/v1/files/file-1/content');
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('blob');
    request.flush(new Blob(['image'], { type: 'image/png' }));
  });

  it('uploads a community image with the POST_IMAGE purpose', () => {
    const file = new File(['image'], 'rice.webp', { type: 'image/webp' });
    service.upload(file, 'POST_IMAGE').subscribe();

    const request = http.expectOne('/api/v1/files/upload');
    expect(request.request.method).toBe('POST');
    const body = request.request.body as FormData;
    const uploaded = body.get('file') as File;
    expect(uploaded.name).toBe('rice.webp');
    expect(uploaded.type).toBe('image/webp');
    expect(uploaded.size).toBe(file.size);
    expect(body.get('purpose')).toBe('POST_IMAGE');
    request.flush({ result: { id: 'file-1' } });
  });
});
