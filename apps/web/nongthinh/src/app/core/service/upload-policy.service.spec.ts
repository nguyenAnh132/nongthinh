import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { UploadPolicyService } from './upload-policy.service';
import { uploadPolicyFixture } from './upload-policy.testing';

describe('UploadPolicyService', () => {
  let service: UploadPolicyService;
  let http: HttpTestingController;
  const url = '/api/v1/bo-portal/upload-policies/AVATAR';

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(UploadPolicyService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => { http.verify(); vi.restoreAllMocks(); });

  it('deduplicates concurrent loads and refreshes after one minute', () => {
    const now = vi.spyOn(Date, 'now').mockReturnValue(1000);
    service.ensure('AVATAR').subscribe();
    service.ensure('AVATAR').subscribe();
    http.expectOne(url).flush({ result: uploadPolicyFixture('AVATAR') });
    now.mockReturnValue(60999);
    service.ensure('AVATAR').subscribe();
    http.expectNone(url);
    now.mockReturnValue(61000);
    service.ensure('AVATAR').subscribe();
    http.expectOne(url).flush({ result: uploadPolicyFixture('AVATAR') });
  });

  it('uses the size and formats returned by BO instead of fixed UI values', () => {
    service.ensure('AVATAR').subscribe();
    http.expectOne(url).flush({ result: {
      purpose: 'AVATAR', maxSizeBytes: 3, allowedContentTypes: ['image/png'], allowedExtensions: ['png'],
    } });
    expect(service.accept('AVATAR')).toBe('image/png,.png');
    expect(service.hint('AVATAR')).toContain('3 byte');
    expect(service.validate(new File(['123'], 'a.png', { type: 'image/png' }), 'AVATAR')).toBeNull();
    expect(service.validate(new File(['1234'], 'a.png', { type: 'image/png' }), 'AVATAR')).toContain('3 byte');
    expect(service.validate(new File(['1'], 'a.jpg', { type: 'image/jpeg' }), 'AVATAR')).not.toBeNull();
  });

  it('blocks uploads when all formats are disabled without restoring defaults', () => {
    service.ensure('AVATAR').subscribe();
    http.expectOne(url).flush({ result: {
      ...uploadPolicyFixture('AVATAR'), allowedContentTypes: [], allowedExtensions: [],
    } });
    expect(service.unavailable('AVATAR')).toBe(true);
    expect(service.hint('AVATAR')).toContain('Đã tắt');
    expect(service.validate(new File(['1'], 'a.png', { type: 'image/png' }), 'AVATAR')).not.toBeNull();
  });

  it('blocks on failed or incomplete configuration and supports retry', () => {
    const error = vi.fn();
    service.ensure('AVATAR').subscribe({ error });
    http.expectOne(url).flush({}, { status: 503, statusText: 'Unavailable' });
    expect(error).toHaveBeenCalledOnce();
    expect(service.unavailable('AVATAR')).toBe(true);
    service.refresh('AVATAR');
    http.expectOne(url).flush({ result: { ...uploadPolicyFixture('AVATAR'), allowedContentTypes: null } });
    expect(service.error('AVATAR')).toBeTruthy();
    service.refresh('AVATAR');
    http.expectOne(url).flush({ result: uploadPolicyFixture('AVATAR') });
    expect(service.unavailable('AVATAR')).toBe(false);
    expect(service.error('AVATAR')).toBeUndefined();
  });

  it('invalidates locally after settings are saved', () => {
    service.ensure('AVATAR').subscribe();
    http.expectOne(url).flush({ result: uploadPolicyFixture('AVATAR') });
    service.invalidate('AVATAR');
    service.ensure('AVATAR').subscribe();
    http.expectOne(url).flush({ result: uploadPolicyFixture('AVATAR') });
  });

  it('normalizes ONNX based on extension while honoring the enabled type', () => {
    const modelUrl = '/api/v1/bo-portal/upload-policies/MODEL_ARTIFACT';
    service.ensure('MODEL_ARTIFACT').subscribe();
    http.expectOne(modelUrl).flush({ result: uploadPolicyFixture('MODEL_ARTIFACT') });
    expect(service.accept('MODEL_ARTIFACT')).toBe('.onnx');
    expect(service.validate(new File(['model'], 'model.ONNX'), 'MODEL_ARTIFACT')).toBeNull();
    expect(service.validate(new File(['model'], 'model.exe', { type: 'application/octet-stream' }), 'MODEL_ARTIFACT')).not.toBeNull();
    service.refresh('MODEL_ARTIFACT');
    http.expectOne(modelUrl).flush({ result: {
      ...uploadPolicyFixture('MODEL_ARTIFACT'), allowedContentTypes: [], allowedExtensions: [],
    } });
    expect(service.validate(new File(['model'], 'model.onnx'), 'MODEL_ARTIFACT')).not.toBeNull();
  });
});
