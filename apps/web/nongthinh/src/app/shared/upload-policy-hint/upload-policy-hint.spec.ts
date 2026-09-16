import { TestBed } from '@angular/core/testing';
import { UploadPolicyService } from '../../core/service/upload-policy.service';
import { provideUploadPolicyFixtures } from '../../core/service/upload-policy.testing';
import { UploadPolicyHint } from './upload-policy-hint';

describe('UploadPolicyHint', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [UploadPolicyHint], providers: [provideUploadPolicyFixtures()],
  }));

  it('does not display size or format hints when uploads are available', () => {
    TestBed.inject(UploadPolicyService).ensure('POST_IMAGE').subscribe();
    const fixture = TestBed.createComponent(UploadPolicyHint);
    fixture.componentRef.setInput('purpose', 'POST_IMAGE');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent.trim()).toBe('');
    expect(fixture.nativeElement.style.display).toBe('none');
  });

  it('keeps configuration loading feedback visible', () => {
    const fixture = TestBed.createComponent(UploadPolicyHint);
    fixture.componentRef.setInput('purpose', 'POST_IMAGE');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Đang tải giới hạn tệp');
  });
});
