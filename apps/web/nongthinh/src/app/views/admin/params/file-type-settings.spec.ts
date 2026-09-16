import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { FileConfigurationApiService, FILE_PURPOSES } from '../../../core/api/file-configuration-api.service';
import type { FilePurpose } from '../../../core/api/file-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UploadPolicyService } from '../../../core/service/upload-policy.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { FileTypeSettings } from './file-type-settings';

describe('FileTypeSettings', () => {
  const hasPermission = vi.fn();
  const updateFileType = vi.fn();
  const invalidate = vi.fn();
  const success = vi.fn();
  const getConfiguration = vi.fn();

  beforeEach(() => {
    vi.resetAllMocks();
    hasPermission.mockReturnValue(true);
    getConfiguration.mockImplementation((purpose: FilePurpose) => of({ result: {
      purpose, maxSizeBytes: 1024, allowedContentTypes: ['image/png'],
      fileTypes: [{ code: 'PNG', contentType: 'image/png', extension: 'png', enabled: true }],
    } }));
    updateFileType.mockImplementation((_purpose, _code, enabled) => of({ result: { enabled } }));
    TestBed.configureTestingModule({
      imports: [FileTypeSettings],
      providers: [
        { provide: FileConfigurationApiService, useValue: { getConfiguration, updateFileType } },
        { provide: AuthService, useValue: { hasPermission } },
        { provide: UploadPolicyService, useValue: { invalidate } },
        { provide: ToastService, useValue: { success } },
      ],
    });
  });

  it('renders every purpose and saves only changed toggles', async () => {
    const fixture = TestBed.createComponent(FileTypeSettings);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(getConfiguration).toHaveBeenCalledTimes(FILE_PURPOSES.length);
    expect(fixture.nativeElement.querySelectorAll('fieldset')).toHaveLength(FILE_PURPOSES.length);
    const checkbox = fixture.nativeElement.querySelector('[aria-label="Ảnh đại diện: PNG"]') as HTMLInputElement;
    checkbox.click();
    await fixture.whenStable();
    expect(fixture.componentInstance.changedCount).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('Đã tắt tất cả định dạng');
    (fixture.nativeElement.querySelector('.save-types') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(updateFileType).toHaveBeenCalledExactlyOnceWith('AVATAR', 'PNG', false);
    expect(invalidate).toHaveBeenCalledWith('AVATAR');
    expect(fixture.componentInstance.changedCount).toBe(0);
  });

  it('keeps failed changes pending without resending successful changes', async () => {
    const fixture = TestBed.createComponent(FileTypeSettings);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;
    component.setEnabled('AVATAR', 'PNG', false);
    component.setEnabled('POST_IMAGE', 'PNG', false);
    updateFileType.mockImplementation((purpose, _code, enabled) => purpose === 'AVATAR'
      ? of({ result: { enabled } }) : throwError(() => new Error('Unavailable')));
    component.save();
    expect(component.changedCount).toBe(1);
    expect(component.error()).toContain('1 thay đổi');
    updateFileType.mockClear().mockImplementation((_purpose, _code, enabled) => of({ result: { enabled } }));
    component.save();
    expect(updateFileType).toHaveBeenCalledExactlyOnceWith('POST_IMAGE', 'PNG', false);
    expect(component.changedCount).toBe(0);
  });

  it('renders read-only controls without granting write access', async () => {
    hasPermission.mockReturnValue(false);
    const fixture = TestBed.createComponent(FileTypeSettings);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.save-types')).toBeNull();
    expect([...fixture.nativeElement.querySelectorAll('fieldset')].every((element: any) => element.disabled)).toBe(true);
    fixture.componentInstance.setEnabled('AVATAR', 'PNG', false);
    fixture.componentInstance.save();
    expect(updateFileType).not.toHaveBeenCalled();
  });

  it('offers retry when configuration cannot be loaded', async () => {
    getConfiguration.mockReturnValue(throwError(() => new Error('Unavailable')));
    const fixture = TestBed.createComponent(FileTypeSettings);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('Thử lại');
    expect(fixture.componentInstance.configurations()).toEqual([]);
  });
});
