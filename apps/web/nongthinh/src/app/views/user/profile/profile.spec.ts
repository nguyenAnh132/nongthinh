import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Subject, of } from 'rxjs';
import { FarmerProfileResponse, ProfileApiService } from '../../../core/api/profile-api.service';
import { FileApiService, FileView } from '../../../core/api/file-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ApiResponse } from '../../../core/models/api-response';
import { LocationService } from '../../../core/service/location.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { UserProfile } from './profile';
import { FollowApiService } from '../../../core/api/follow-api.service';

describe('UserProfile', () => {
  let profileResponse: Subject<ApiResponse<FarmerProfileResponse>>;
  let avatarUploadResponse: Subject<ApiResponse<FileView>>;
  let avatarUpdateResponse: Subject<ApiResponse<FarmerProfileResponse>>;
  let uploadAvatar: ReturnType<typeof vi.fn>;
  let updateMyFarmerAvatar: ReturnType<typeof vi.fn>;
  let refreshMe: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    profileResponse = new Subject<ApiResponse<FarmerProfileResponse>>();
    avatarUploadResponse = new Subject<ApiResponse<FileView>>();
    avatarUpdateResponse = new Subject<ApiResponse<FarmerProfileResponse>>();
    uploadAvatar = vi.fn(() => avatarUploadResponse);
    updateMyFarmerAvatar = vi.fn(() => avatarUpdateResponse);
    refreshMe = vi.fn(() => of(null));

    await TestBed.configureTestingModule({
      imports: [UserProfile],
      providers: [
        { provide: FollowApiService, useValue: {
          revision: signal(0), profile: vi.fn(() => of({ result: null })),
        } },
        {
          provide: AuthService,
          useValue: {
            currentUser: signal({ role: 'ROLE_FARMER' }).asReadonly(),
            loadMe: refreshMe,
          },
        },
        {
          provide: ProfileApiService,
          useValue: {
            getMyFarmerProfile: vi.fn(() => profileResponse),
            updateMyFarmerProfile: vi.fn(),
            updateMyFarmerAvatar,
          },
        },
        {
          provide: FileApiService,
          useValue: {
            upload: uploadAvatar,
          },
        },
        {
          provide: ToastService,
          useValue: {
            info: vi.fn(),
            error: vi.fn(),
            success: vi.fn(),
          },
        },
        {
          provide: LocationService,
          useValue: {
            getProvinces: vi.fn(() => of([{ id: '01', code: 'HN', name: 'Thành phố Hà Nội' }])),
            getCommunesByProvince: vi.fn(() =>
              of([
                {
                  id: '00000000-0000-0000-0000-000000000001',
                  provinceId: '01',
                  code: '00001',
                  name: 'Phường Ba Đình',
                },
              ]),
            ),
          },
        },
      ],
    }).compileComponents();
  });

  it('replaces the loading state when the profile response arrives asynchronously', async () => {
    const fixture = TestBed.createComponent(UserProfile);
    fixture.detectChanges();

    const loadingState = fixture.nativeElement.querySelector('.profile-loading');
    expect(loadingState).not.toBeNull();
    expect(loadingState.getAttribute('aria-busy')).toBe('true');
    expect(loadingState.querySelector('.profile-loading__hero')).not.toBeNull();
    expect(loadingState.querySelector('.profile-loading__about')).not.toBeNull();
    expect(loadingState.querySelectorAll('.profile-loading__info-row')).toHaveLength(4);
    expect(fixture.nativeElement.textContent).toContain('Đang tải hồ sơ...');

    profileResponse.next({ result: farmerProfile() });
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).not.toContain('Đang tải hồ sơ...');
    expect(fixture.nativeElement.querySelector('.profile-loading')).toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Nguyễn Văn An');
    expect(fixture.nativeElement.textContent).toContain('Phường Ba Đình, Thành phố Hà Nội');
    expect(fixture.nativeElement.textContent).not.toContain('00000000-0000-0000-0000-000000000001');
  });

  it('shows location names in the edit selects while retaining ids as values', async () => {
    const fixture = TestBed.createComponent(UserProfile);
    fixture.detectChanges();
    profileResponse.next({ result: farmerProfile() });
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('.btn-edit') as HTMLButtonElement).click();
    await fixture.whenStable();

    const province = fixture.nativeElement.querySelector(
      '#profile-provinceId',
    ) as HTMLSelectElement;
    const commune = fixture.nativeElement.querySelector('#profile-communeId') as HTMLSelectElement;
    expect(province.value).toBe('01');
    expect(province.textContent).toContain('Thành phố Hà Nội');
    expect(commune.value).toBe('00000000-0000-0000-0000-000000000001');
    expect(commune.textContent).toContain('Phường Ba Đình');
  });

  it('uploads an avatar and persists its public URL through profile-service', async () => {
    const fixture = TestBed.createComponent(UserProfile);
    fixture.detectChanges();
    profileResponse.next({ result: farmerProfile() });
    await fixture.whenStable();

    const input = fixture.nativeElement.querySelector('.avatar-file-input') as HTMLInputElement;
    const file = new File(['avatar'], 'farmer.webp', { type: 'image/webp' });
    Object.defineProperty(input, 'files', { configurable: true, value: [file] });
    input.dispatchEvent(new Event('change'));

    expect(uploadAvatar).toHaveBeenCalledWith(file, 'AVATAR');
    expect(fixture.componentInstance.uploadingAvatar()).toBe(true);

    const avatarUrl = 'https://files.example.test/avatars/farmer.webp';
    avatarUploadResponse.next({
      result: {
        id: 'file-1',
        ownerUserId: 'user-1',
        purpose: 'AVATAR',
        originalFileName: file.name,
        contentType: file.type,
        sizeBytes: file.size,
        publicUrl: avatarUrl,
        status: 'ACTIVE',
        createdAt: null,
        updatedAt: null,
      },
    });
    avatarUploadResponse.complete();

    expect(updateMyFarmerAvatar).toHaveBeenCalledWith(avatarUrl);
    avatarUpdateResponse.next({
      result: {
        ...farmerProfile(),
        avatarUrl,
        provinceName: null,
        communeName: null,
      },
    });
    avatarUpdateResponse.complete();
    await fixture.whenStable();
    fixture.detectChanges();

    const avatar = fixture.nativeElement.querySelector('.avatar img') as HTMLImageElement;
    expect(avatar.getAttribute('src')).toBe(avatarUrl);
    expect(fixture.nativeElement.textContent).toContain('Phường Ba Đình, Thành phố Hà Nội');
    expect(fixture.componentInstance.uploadingAvatar()).toBe(false);
    expect(refreshMe).toHaveBeenCalledOnce();
  });
});

function farmerProfile(): FarmerProfileResponse {
  return {
    id: 'profile-1',
    userId: 'user-1',
    firstName: 'Nguyễn',
    lastName: 'Văn An',
    gender: 'MALE',
    phone: '0912345678',
    provinceId: '01',
    provinceName: 'Thành phố Hà Nội',
    communeId: '00000000-0000-0000-0000-000000000001',
    communeName: 'Phường Ba Đình',
    addressDetail: 'Thôn 1',
    avatarUrl: null,
    status: 'ACTIVE',
  };
}
