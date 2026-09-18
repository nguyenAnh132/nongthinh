import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { SystemParamApiService } from '../../../core/api/system-param-api.service';
import { FileConfigurationApiService } from '../../../core/api/file-configuration-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UploadPolicyService } from '../../../core/service/upload-policy.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { Params } from './params';

describe('Params upload sizes', () => {
  const updateSystemParam = vi.fn();
  const invalidate = vi.fn();
  const error = vi.fn();
  const hasPermission = vi.fn();

  beforeEach(() => {
    vi.resetAllMocks();
    hasPermission.mockReturnValue(true);
    updateSystemParam.mockReturnValue(of({ result: {} }));
    TestBed.configureTestingModule({
      imports: [Params],
      providers: [
        { provide: SystemParamApiService, useValue: {
          getGroupedSystemParamTypes: () => of({ result: [{ id: 1, name: 'Cấu hình tệp',
            description: 'Giới hạn kích thước tệp tải lên theo mục đích sử dụng', params: [{
            id: 1, name: 'FILE_UPLOAD_MAX_BYTES_AVATAR', value: '2097152', dataType: 'INTEGER', description: null,
          }] }, { id: 2, name: 'Khác', description: 'Các tham số chưa được phân loại', params: [] }] }), updateSystemParam,
        } },
        { provide: FileConfigurationApiService, useValue: {
          getConfiguration: (purpose: string) => of({ result: { purpose, fileTypes: [] } }),
        } },
        { provide: AuthService, useValue: { hasPermission } },
        { provide: UploadPolicyService, useValue: { invalidate } },
        { provide: ToastService, useValue: { error, success: vi.fn() } },
      ],
    });
  });

  it('retains the size parameter, shows human-readable units and saves numeric input as a string', async () => {
    const fixture = TestBed.createComponent(Params);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('2 MB');
    expect(fixture.nativeElement.querySelector('app-file-type-settings')).not.toBeNull();
    const input = fixture.nativeElement.querySelector('#FILE_UPLOAD_MAX_BYTES_AVATAR') as HTMLInputElement;
    input.value = '3145728';
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('3 MB');
    fixture.componentInstance.saveAll();
    expect(updateSystemParam).toHaveBeenCalledExactlyOnceWith('FILE_UPLOAD_MAX_BYTES_AVATAR', {
      value: '3145728', description: null,
    });
    expect(invalidate).toHaveBeenCalledWith('AVATAR');
  });

  it('hides the removed descriptions while retaining settings headings', async () => {
    const fixture = TestBed.createComponent(Params);
    fixture.detectChanges();
    await fixture.whenStable();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Cấu hình tệp');
    expect(text).toContain('Khác');
    expect(text).toContain('Định dạng tệp được chấp nhận');
    expect(text).not.toContain('Bật hoặc tắt từng định dạng theo mục đích sử dụng.');
    expect(text).not.toContain('Giới hạn size theo mục đích vẫn chịu mức trần multipart');
    expect(text).not.toContain('Giới hạn kích thước tệp tải lên theo mục đích sử dụng');
    expect(text).not.toContain('Các tham số chưa được phân loại');
  });

  it('rejects invalid size limits before saving', async () => {
    const fixture = TestBed.createComponent(Params);
    fixture.detectChanges();
    await fixture.whenStable();
    for (const invalid of ['0', '-1', '1.5', '2147483648', '']) {
      fixture.componentInstance.paramGroups[0].params[0].editValue = invalid;
      fixture.componentInstance.saveAll();
    }
    expect(updateSystemParam).not.toHaveBeenCalled();
    expect(error).toHaveBeenCalledTimes(5);
  });

  it('disables size editing for a read-only administrator', async () => {
    hasPermission.mockReturnValue(false);
    const fixture = TestBed.createComponent(Params);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#FILE_UPLOAD_MAX_BYTES_AVATAR').disabled).toBe(true);
    fixture.componentInstance.paramGroups[0].params[0].editValue = '1024';
    fixture.componentInstance.saveAll();
    expect(updateSystemParam).not.toHaveBeenCalled();
  });
});
