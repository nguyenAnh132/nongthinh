import { provideUploadPolicyFixtures } from '../../../core/service/upload-policy.testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { BrandProfileView } from '../../../core/api/brand-profile-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { FollowApiService } from '../../../core/api/follow-api.service';
import { FileApiService } from '../../../core/api/file-api.service';
import {
  MyBrandProfileApiService,
  MyBrandProfileUpdatePayload,
} from '../../../core/api/my-brand-profile-api.service';
import { LocationService } from '../../../core/service/location.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { BrandVerification } from './brand-verification';

describe('BrandVerification', () => {
  let profile: BrandProfileView;
  let updateMe: ReturnType<typeof vi.fn>;
  let updateLogo: ReturnType<typeof vi.fn>;
  let uploadFile: ReturnType<typeof vi.fn>;
  let listMyFiles: ReturnType<typeof vi.fn>;
  let refreshMe: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    profile = brandProfile();
    updateMe = vi.fn((payload: MyBrandProfileUpdatePayload) =>
      of({ result: { ...profile, ...payload } }),
    );
    updateLogo = vi.fn((logoUrl: string) => of({ result: { ...profile, logoUrl } }));
    uploadFile = vi.fn(() =>
      of({
        result: {
          id: 'brand-logo-file',
          ownerUserId: profile.userId,
          purpose: 'BRAND_LOGO',
          originalFileName: 'brand-logo.webp',
          contentType: 'image/webp',
          sizeBytes: 4,
          publicUrl: 'https://files.example.test/brand-logo-updated.webp',
          status: 'ACTIVE',
          createdAt: null,
          updatedAt: null,
        },
      }),
    );
    listMyFiles = vi.fn(() => of({ result: [] }));
    refreshMe = vi.fn(() => of(null));
    await TestBed.configureTestingModule({
      imports: [BrandVerification],
      providers: [
        provideUploadPolicyFixtures(),
        {
          provide: FollowApiService,
          useValue: { revision: signal(0), profile: vi.fn(() => of({ result: null })) },
        },
        {
          provide: AuthService,
          useValue: {
            currentUser: signal({
              role: 'ROLE_BRAND',
              email: 'hello@nongthinh.vn',
            }).asReadonly(),
            loadMe: refreshMe,
          },
        },
        {
          provide: MyBrandProfileApiService,
          useValue: {
            getMe: vi.fn(() => of({ result: profile })),
            getMyDocument: vi.fn(() => of({ result: null })),
            updateMe,
            updateLogo,
            updateBanner: vi.fn(() => of({ result: profile })),
            uploadDocument: vi.fn(),
            submitDocuments: vi.fn(),
          },
        },
        {
          provide: FileApiService,
          useValue: { upload: uploadFile, listMine: listMyFiles },
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
        {
          provide: ToastService,
          useValue: {
            error: vi.fn(),
            success: vi.fn(),
          },
        },
      ],
    }).compileComponents();
  });

  it('renders a brand profile with the same hero and about-card structure as farmer', async () => {
    const fixture = TestBed.createComponent(BrandVerification);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.profile-hero')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.profile-cover')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.avatar')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.about-card')).not.toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Nông Thịnh Xanh');
    expect(fixture.nativeElement.textContent).toContain('Phường Ba Đình, Thành phố Hà Nội');
    expect(fixture.nativeElement.textContent).toContain('hello@nongthinh.vn');
  });

  it('opens the farmer-style edit dialog with current brand values', async () => {
    const fixture = TestBed.createComponent(BrandVerification);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('.btn-edit') as HTMLButtonElement).click();
    fixture.detectChanges();

    const dialog = fixture.nativeElement.querySelector('.edit-dialog');
    const brandName = fixture.nativeElement.querySelector('#brand-name') as HTMLInputElement;
    const commune = fixture.nativeElement.querySelector('#brand-commune') as HTMLSelectElement;

    expect(dialog).not.toBeNull();
    expect(brandName.value).toBe('Nông Thịnh Xanh');
    expect(commune.value).toBe('00000000-0000-0000-0000-000000000001');
  });

  it('replaces the active-status guidance with verified marks on the cover and name', async () => {
    profile = { ...profile, status: 'ACTIVE' };
    const fixture = TestBed.createComponent(BrandVerification);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.verified-cover-badge')?.textContent).toContain(
      'Đã xác minh',
    );
    expect(fixture.nativeElement.querySelector('.verified-name-icon')).not.toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Thương hiệu đã được kích hoạt');
    expect(fixture.nativeElement.textContent).not.toContain(
      'Tài khoản thương hiệu đã sẵn sàng sử dụng.',
    );
  });

  it('allows an active brand to update its profile fields', async () => {
    profile = { ...profile, status: 'ACTIVE' };
    const fixture = TestBed.createComponent(BrandVerification);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('.btn-edit') as HTMLButtonElement).click();
    fixture.componentInstance.profileForm.patchValue({
      brandName: 'Nông Thịnh Việt',
      description: 'Thông tin thương hiệu đã cập nhật',
      websiteUrl: 'https://nongthinhviet.vn',
    });
    fixture.componentInstance.saveProfile();

    expect(updateMe).toHaveBeenCalledWith(
      expect.objectContaining({
        brandName: 'Nông Thịnh Việt',
        description: 'Thông tin thương hiệu đã cập nhật',
        phone: '0912345678',
        officeProvinceId: '01',
        officeCommuneId: '00000000-0000-0000-0000-000000000001',
        representativeName: 'Nguyễn Văn An',
        representativePhone: '0987654321',
        representativeEmail: 'an@nongthinh.vn',
        websiteUrl: 'https://nongthinhviet.vn',
      }),
    );
    expect(fixture.componentInstance.profile?.brandName).toBe('Nông Thịnh Việt');
    expect(fixture.componentInstance.editing).toBe(false);
    expect(refreshMe).toHaveBeenCalledOnce();
  });

  it('uploads and persists a new logo for an active brand', async () => {
    profile = { ...profile, status: 'ACTIVE' };
    const fixture = TestBed.createComponent(BrandVerification);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const input = fixture.nativeElement.querySelector(
      '.avatar-shell .image-file-input',
    ) as HTMLInputElement;
    const file = new File(['logo'], 'brand-logo.webp', { type: 'image/webp' });
    Object.defineProperty(input, 'files', { configurable: true, value: [file] });
    input.dispatchEvent(new Event('change'));

    expect(uploadFile).toHaveBeenCalledWith(file, 'BRAND_LOGO');
    expect(updateLogo).toHaveBeenCalledWith('https://files.example.test/brand-logo-updated.webp');
    expect(fixture.componentInstance.profile?.logoUrl).toBe(
      'https://files.example.test/brand-logo-updated.webp',
    );
    expect(refreshMe).toHaveBeenCalledOnce();
  });

  it('renders the linked business license as a Gmail-style attachment with its file name', async () => {
    const licenseUrl = 'https://files.example.test/business-license.pdf';
    const licenseName = 'Giay-phep-kinh-doanh-rat-dai-de-kiem-tra-ellipsis.pdf';
    const getMyDocument = vi.fn(() =>
      of({
        result: {
          id: 'document-1',
          brandProfileId: profile.id,
          businessLicenseUrl: licenseUrl,
          reviewStatus: 'PENDING_REVIEW',
          reviewedBy: null,
          reviewedAt: null,
          createdAt: null,
          updatedAt: null,
        },
      }),
    );
    listMyFiles.mockReturnValue(
      of({
        result: [
          {
            id: 'license-file-1',
            ownerUserId: profile.userId,
            purpose: 'BUSINESS_LICENSE',
            originalFileName: licenseName,
            contentType: 'application/pdf',
            sizeBytes: 1024,
            publicUrl: licenseUrl,
            status: 'UPLOADED',
            createdAt: null,
            updatedAt: null,
          },
        ],
      }),
    );
    TestBed.overrideProvider(MyBrandProfileApiService, {
      useValue: {
        getMe: vi.fn(() => of({ result: profile })),
        getMyDocument,
        updateMe,
        updateLogo,
        updateBanner: vi.fn(() => of({ result: profile })),
        uploadDocument: vi.fn(),
        submitDocuments: vi.fn(),
      },
    });

    const fixture = TestBed.createComponent(BrandVerification);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const attachment = fixture.nativeElement.querySelector(
      '.document-current',
    ) as HTMLAnchorElement;
    expect(attachment.href).toBe(licenseUrl);
    expect(attachment.title).toBe(licenseName);
    expect(attachment.querySelector('strong')?.textContent?.trim()).toBe(licenseName);
    expect(attachment.querySelector('img')?.getAttribute('src')).toBe('/icons/business/doc.png');
    expect(fixture.nativeElement.textContent).not.toContain(
      'Ảnh hoặc PDF, tối đa 10 MB (JPEG, PNG, WebP, PDF).',
    );
    expect(fixture.nativeElement.textContent).not.toContain('Giấy phép đã gắn với hồ sơ');
  });
});

function brandProfile(): BrandProfileView {
  return {
    id: 'brand-profile-1',
    userId: 'brand-user-1',
    brandName: 'Nông Thịnh Xanh',
    taxCode: '0101234567',
    description: 'Giải pháp nông nghiệp bền vững.',
    phone: '0912345678',
    officeProvinceId: '01',
    officeCommuneId: '00000000-0000-0000-0000-000000000001',
    officeAddressDetail: 'Số 1 đường Nông Nghiệp',
    representativeName: 'Nguyễn Văn An',
    representativePhone: '0987654321',
    representativeEmail: 'an@nongthinh.vn',
    logoUrl: 'https://files.example.test/brand-logo.webp',
    bannerUrl: 'https://files.example.test/brand-banner.webp',
    websiteUrl: 'https://nongthinh.vn',
    status: 'UNDER_REVIEW',
    rejectionReason: null,
    scheduledDeletionAt: null,
    rejectedAt: null,
    approvedAt: null,
    approvedBy: null,
    rejectedBy: null,
    createdAt: null,
    updatedAt: null,
  };
}
