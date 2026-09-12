import { ChangeDetectorRef, Component, NgZone, afterNextRender, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize, map, switchMap } from 'rxjs/operators';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { BrandDocumentView, BrandProfileView } from '../../../core/api/brand-profile-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { FileApiService, FileView } from '../../../core/api/file-api.service';
import { MyBrandProfileApiService } from '../../../core/api/my-brand-profile-api.service';
import { Commune, LocationService, Province } from '../../../core/service/location.service';
import {
  apiErrorMessage,
  apiResponseErrorMessage,
  unwrapApiResult,
} from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';
import {
  brandUserStatusLabel,
  brandUserStatusTone,
  canUploadBrandDocuments,
  docReviewStatusLabel,
} from './brand-status.util';

const LICENSE_ACCEPT = 'image/jpeg,image/png,image/webp,application/pdf';
const LICENSE_MAX_BYTES = 10 * 1024 * 1024;
const BRAND_IMAGE_ACCEPT = 'image/jpeg,image/png,image/webp';
const BRAND_IMAGE_TYPES = new Set(BRAND_IMAGE_ACCEPT.split(','));
const BRAND_LOGO_MAX_BYTES = 2 * 1024 * 1024;
const BRAND_BANNER_MAX_BYTES = 5 * 1024 * 1024;
const BRAND_PROFILE_READ_ONLY_STATUSES = new Set(['LOCKED', 'DISABLED', 'DELETED']);

@Component({
  selector: 'app-brand-verification',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NzAlertModule],
  templateUrl: './brand-verification.html',
  styleUrl: './brand-verification.scss',
})
export class BrandVerification {
  private readonly fb = inject(FormBuilder);
  private readonly brandApi = inject(MyBrandProfileApiService);
  private readonly fileApi = inject(FileApiService);
  private readonly locationService = inject(LocationService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly zone = inject(NgZone);

  readonly licenseAccept = LICENSE_ACCEPT;
  readonly brandImageAccept = BRAND_IMAGE_ACCEPT;

  loading = true;
  savingProfile = false;
  uploading = false;
  uploadingLogo = false;
  uploadingBanner = false;
  submitting = false;
  editing = false;
  loadError: string | null = null;

  profile: BrandProfileView | null = null;
  document: BrandDocumentView | null = null;
  licenseFile: FileView | null = null;
  selectedFile: File | null = null;
  selectedFilePreviewUrl: string | null = null;
  logoPreviewUrl: string | null = null;
  bannerPreviewUrl: string | null = null;
  submittedThisSession = false;

  provinces: Province[] = [];
  communes: Commune[] = [];
  loadingCommunes = false;

  readonly profileForm = this.fb.nonNullable.group({
    brandName: ['', [Validators.required, Validators.maxLength(200)]],
    taxCode: [''],
    description: ['', Validators.maxLength(1000)],
    phone: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    officeProvinceId: [''],
    officeCommuneId: [''],
    officeAddressDetail: ['', Validators.maxLength(500)],
    representativeName: ['', [Validators.required, Validators.maxLength(255)]],
    representativePhone: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    representativeEmail: ['', [Validators.required, Validators.email]],
    websiteUrl: [''],
  });

  constructor() {
    afterNextRender(() => {
      this.reload();
    });
  }

  get status(): string {
    return this.profile?.status ?? '';
  }

  get displayName(): string {
    return this.profile?.brandName?.trim() || 'Thương hiệu Nông Thịnh';
  }

  get initials(): string {
    return this.displayName
      .split(/\s+/)
      .filter(Boolean)
      .slice(-2)
      .map((part) => part.charAt(0).toLocaleUpperCase('vi'))
      .join('');
  }

  get officeLocationLabel(): string {
    if (!this.profile) return 'Chưa cập nhật';
    const provinceName = this.provinces.find(
      (province) => province.id === this.profile?.officeProvinceId,
    )?.name;
    const communeName = this.communes.find(
      (commune) => commune.id === this.profile?.officeCommuneId,
    )?.name;
    return [communeName, provinceName].filter(Boolean).join(', ') || 'Chưa cập nhật';
  }

  get accountEmail(): string {
    return this.authService.currentUser()?.email ?? '';
  }

  get canEditAndUpload(): boolean {
    return canUploadBrandDocuments(this.status);
  }

  get canEditProfile(): boolean {
    return !!this.profile && !BRAND_PROFILE_READ_ONLY_STATUSES.has(this.status);
  }

  get canSubmit(): boolean {
    return (
      this.canEditAndUpload &&
      !!this.document?.businessLicenseUrl &&
      this.document.reviewStatus === 'PENDING_REVIEW' &&
      !this.submittedThisSession
    );
  }

  get statusLabel(): string {
    return brandUserStatusLabel(this.status);
  }

  get statusTone(): string {
    return brandUserStatusTone(this.status);
  }

  get docStatusLabel(): string {
    return this.document ? docReviewStatusLabel(this.document.reviewStatus) : '';
  }

  get documentFileName(): string {
    const originalFileName = this.licenseFile?.originalFileName?.trim();
    if (originalFileName) return originalFileName;

    const url = this.document?.businessLicenseUrl?.trim();
    if (!url) return 'Giấy phép kinh doanh';

    try {
      const pathname = new URL(url, 'http://localhost').pathname;
      const encodedFileName = pathname.split('/').filter(Boolean).at(-1);
      return encodedFileName ? decodeURIComponent(encodedFileName) : 'Giấy phép kinh doanh';
    } catch {
      return 'Giấy phép kinh doanh';
    }
  }

  get guidanceTitle(): string {
    switch (this.status) {
      case 'PENDING_APPROVAL':
        return 'Hồ sơ đang chờ tiếp nhận';
      case 'UNDER_REVIEW':
        return 'Xác minh thông tin & nộp giấy phép';
      case 'NEEDS_REVISION':
        return 'Cần bổ sung hồ sơ';
      case 'READY_FOR_FINAL_REVIEW':
        return 'Đã nộp — chờ duyệt cuối';
      case 'ACTIVE':
        return 'Thương hiệu đã được kích hoạt';
      case 'REJECTED':
        return 'Hồ sơ bị từ chối';
      default:
        return 'Trạng thái hồ sơ thương hiệu';
    }
  }

  get guidanceMessage(): string {
    switch (this.status) {
      case 'PENDING_APPROVAL':
        return 'Hệ thống đã tiếp nhận đăng ký. Quản trị viên sẽ sớm liên hệ để xác minh điện thoại.';
      case 'UNDER_REVIEW':
        return 'Vui lòng kiểm tra lại thông tin đăng ký cho khớp thực tế, tải lên giấy phép kinh doanh (ảnh hoặc PDF), rồi nộp hồ sơ để tiếp tục xét duyệt.';
      case 'NEEDS_REVISION':
        return (
          this.profile?.rejectionReason ||
          'Hồ sơ cần bổ sung. Vui lòng cập nhật thông tin và/hoặc tải lại giấy phép kinh doanh, rồi nộp lại.'
        );
      case 'READY_FOR_FINAL_REVIEW':
        return 'Giấy tờ đã được tiếp nhận. Đội ngũ đang thực hiện bước duyệt cuối.';
      case 'ACTIVE':
        return 'Tài khoản thương hiệu đã sẵn sàng sử dụng.';
      case 'REJECTED':
        return this.profile?.rejectionReason || 'Hồ sơ không được duyệt.';
      default:
        return 'Theo dõi tiến trình xét duyệt thương hiệu tại đây.';
    }
  }

  reload(): void {
    this.syncView(() => {
      this.loading = true;
      this.loadError = null;
    });

    forkJoin({
      profile: this.brandApi.getMe(),
      document: this.brandApi
        .getMyDocument()
        .pipe(catchError(() => of({ code: '1000', result: null }))),
      licenseFiles: this.fileApi
        .listMine('BUSINESS_LICENSE')
        .pipe(catchError(() => of({ code: '1000', result: [] as FileView[] }))),
      provinces: this.locationService.getProvinces().pipe(catchError(() => of([] as Province[]))),
    })
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.loading = false;
          });
        }),
      )
      .subscribe({
        next: ({ profile, document, licenseFiles, provinces }) => {
          const p = unwrapApiResult<BrandProfileView>(profile);
          const doc = unwrapApiResult<BrandDocumentView>(document);
          const files = unwrapApiResult<FileView[]>(licenseFiles) ?? [];

          this.syncView(() => {
            this.provinces = provinces;
            this.profile = p;
            this.document = doc;
            this.licenseFile = this.findLinkedLicenseFile(files, doc);
            this.submittedThisSession = false;

            if (!p) {
              this.loadError = apiResponseErrorMessage(
                typeof profile === 'object' && profile !== null && !Array.isArray(profile)
                  ? (profile as { message?: string })
                  : null,
                'Không thể tải hồ sơ thương hiệu.',
              );
              return;
            }

            this.patchProfileForm(p);
            if (BRAND_PROFILE_READ_ONLY_STATUSES.has(p.status)) {
              this.profileForm.disable({ emitEvent: false });
            } else {
              this.profileForm.enable({ emitEvent: false });
            }
          });

          if (p?.officeProvinceId) {
            this.loadCommunes(p.officeProvinceId, p.officeCommuneId ?? '');
          } else {
            this.syncView(() => {
              this.communes = [];
            });
          }
        },
        error: (err) => {
          this.syncView(() => {
            this.profile = null;
            this.document = null;
            this.licenseFile = null;
            this.loadError = apiErrorMessage(err, 'Không thể tải hồ sơ thương hiệu.');
          });
          this.toast.error(this.loadError ?? 'Không thể tải hồ sơ thương hiệu.');
        },
      });
  }

  onProvinceChange(provinceId: string): void {
    this.profileForm.patchValue({ officeCommuneId: '' });
    if (!provinceId) {
      this.syncView(() => {
        this.communes = [];
      });
      return;
    }
    this.loadCommunes(provinceId);
  }

  private loadCommunes(provinceId: string, selectedCommuneId = ''): void {
    this.syncView(() => {
      this.loadingCommunes = true;
    });
    this.locationService
      .getCommunesByProvince(provinceId)
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.loadingCommunes = false;
          });
        }),
      )
      .subscribe({
        next: (communes) => {
          this.syncView(() => {
            this.communes = communes;
            if (selectedCommuneId) {
              this.profileForm.patchValue({ officeCommuneId: selectedCommuneId });
            }
          });
        },
        error: () => {
          this.syncView(() => {
            this.communes = [];
          });
          this.toast.error('Không thể tải danh sách phường/xã.');
        },
      });
  }

  private patchProfileForm(p: BrandProfileView): void {
    this.profileForm.patchValue({
      brandName: p.brandName ?? '',
      taxCode: p.taxCode ?? '',
      description: p.description ?? '',
      phone: p.phone ?? '',
      officeProvinceId: p.officeProvinceId ?? '',
      officeCommuneId: p.officeCommuneId ?? '',
      officeAddressDetail: p.officeAddressDetail ?? '',
      representativeName: p.representativeName ?? '',
      representativePhone: p.representativePhone ?? '',
      representativeEmail: p.representativeEmail ?? '',
      websiteUrl: p.websiteUrl ?? '',
    });
  }

  startEditing(): void {
    if (!this.canEditProfile || !this.profile) return;
    this.patchProfileForm(this.profile);
    this.editing = true;
  }

  cancelEditing(): void {
    if (this.profile) {
      this.patchProfileForm(this.profile);
    }
    this.editing = false;
  }

  saveProfile(): void {
    if (!this.canEditProfile) return;
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.toast.error('Vui lòng kiểm tra lại các trường thông tin.');
      return;
    }

    const v = this.profileForm.getRawValue();
    const provinceId = v.officeProvinceId.trim();
    const communeId = v.officeCommuneId.trim();
    if ((provinceId && !communeId) || (!provinceId && communeId)) {
      this.toast.error('Vui lòng chọn đủ Tỉnh/Thành và Phường/Xã.');
      return;
    }

    this.syncView(() => {
      this.savingProfile = true;
    });

    this.brandApi
      .updateMe({
        brandName: v.brandName.trim(),
        taxCode: v.taxCode.trim() || null,
        description: v.description.trim() || null,
        phone: v.phone.trim(),
        officeProvinceId: provinceId || null,
        officeCommuneId: communeId || null,
        officeAddressDetail: v.officeAddressDetail.trim() || null,
        representativeName: v.representativeName.trim(),
        representativePhone: v.representativePhone.trim(),
        representativeEmail: v.representativeEmail.trim(),
        logoUrl: this.profile?.logoUrl ?? null,
        bannerUrl: this.profile?.bannerUrl ?? null,
        websiteUrl: v.websiteUrl.trim() || null,
      })
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.savingProfile = false;
          });
        }),
      )
      .subscribe({
        next: (res) => {
          const updated = unwrapApiResult<BrandProfileView>(res);
          this.syncView(() => {
            if (updated) {
              this.profile = updated;
              this.patchProfileForm(updated);
            }
            this.editing = false;
          });
          this.authService.loadMe().subscribe();
          this.toast.success('Đã cập nhật thông tin thương hiệu.');
        },
        error: (err) => {
          this.toast.error(apiErrorMessage(err, 'Không thể cập nhật thông tin.'));
        },
      });
  }

  onLogoSelected(event: Event): void {
    this.uploadBrandImage(event, 'logo');
  }

  onBannerSelected(event: Event): void {
    this.uploadBrandImage(event, 'banner');
  }

  private uploadBrandImage(event: Event, target: 'logo' | 'banner'): void {
    const input = event.target as HTMLInputElement;
    if (!this.canEditProfile) {
      input.value = '';
      return;
    }
    const file = input.files?.[0] ?? null;
    if (!file) return;

    if (!BRAND_IMAGE_TYPES.has(file.type.toLowerCase())) {
      input.value = '';
      this.toast.error('Chỉ chấp nhận ảnh JPEG, PNG hoặc WebP.');
      return;
    }

    const maxBytes = target === 'logo' ? BRAND_LOGO_MAX_BYTES : BRAND_BANNER_MAX_BYTES;
    if (file.size > maxBytes) {
      input.value = '';
      this.toast.error(
        target === 'logo' ? 'Logo không được vượt quá 2 MB.' : 'Ảnh bìa không được vượt quá 5 MB.',
      );
      return;
    }

    this.setBrandImagePreview(file, target);
    this.syncView(() => {
      if (target === 'logo') {
        this.uploadingLogo = true;
      } else {
        this.uploadingBanner = true;
      }
    });

    this.fileApi
      .upload(file, target === 'logo' ? 'BRAND_LOGO' : 'BRAND_BANNER')
      .pipe(
        switchMap((uploadResponse) => {
          const publicUrl = unwrapApiResult<FileView>(uploadResponse)?.publicUrl?.trim();
          if (!publicUrl) {
            throw new Error('UPLOAD_URL_MISSING');
          }
          return target === 'logo'
            ? this.brandApi.updateLogo(publicUrl)
            : this.brandApi.updateBanner(publicUrl);
        }),
        finalize(() => {
          this.syncView(() => {
            if (target === 'logo') {
              this.uploadingLogo = false;
            } else {
              this.uploadingBanner = false;
            }
            input.value = '';
            this.clearBrandImagePreview(target);
          });
        }),
      )
      .subscribe({
        next: (response) => {
          const updated = unwrapApiResult<BrandProfileView>(response);
          this.syncView(() => {
            if (updated) {
              this.profile = updated;
              this.patchProfileForm(updated);
            }
          });
          this.authService.loadMe().subscribe();
          this.toast.success(
            target === 'logo' ? 'Đã cập nhật logo thương hiệu.' : 'Đã cập nhật ảnh bìa.',
          );
        },
        error: (err) => {
          if (err instanceof Error && err.message === 'UPLOAD_URL_MISSING') {
            this.toast.error('Upload ảnh thành công nhưng file-service không trả về publicUrl.');
            return;
          }
          this.toast.error(
            apiErrorMessage(
              err,
              target === 'logo'
                ? 'Không thể cập nhật logo thương hiệu.'
                : 'Không thể cập nhật ảnh bìa.',
            ),
          );
        },
      });
  }

  private setBrandImagePreview(file: File, target: 'logo' | 'banner'): void {
    this.clearBrandImagePreview(target);
    if (typeof URL.createObjectURL !== 'function') return;
    const previewUrl = URL.createObjectURL(file);
    if (target === 'logo') {
      this.logoPreviewUrl = previewUrl;
    } else {
      this.bannerPreviewUrl = previewUrl;
    }
  }

  private clearBrandImagePreview(target: 'logo' | 'banner'): void {
    const previewUrl = target === 'logo' ? this.logoPreviewUrl : this.bannerPreviewUrl;
    if (previewUrl && typeof URL.revokeObjectURL === 'function') {
      URL.revokeObjectURL(previewUrl);
    }
    if (target === 'logo') {
      this.logoPreviewUrl = null;
    } else {
      this.bannerPreviewUrl = null;
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    input.value = '';
    this.clearSelectedFile();

    if (!file) return;

    if (!LICENSE_ACCEPT.split(',').includes(file.type)) {
      this.toast.error('Chỉ chấp nhận ảnh JPEG/PNG/WebP hoặc PDF.');
      return;
    }
    if (file.size > LICENSE_MAX_BYTES) {
      this.toast.error('File vượt quá 10MB.');
      return;
    }

    this.syncView(() => {
      this.selectedFile = file;
      if (file.type.startsWith('image/')) {
        this.selectedFilePreviewUrl = URL.createObjectURL(file);
      }
    });
  }

  clearSelectedFile(): void {
    if (this.selectedFilePreviewUrl) {
      URL.revokeObjectURL(this.selectedFilePreviewUrl);
    }
    this.syncView(() => {
      this.selectedFile = null;
      this.selectedFilePreviewUrl = null;
    });
  }

  /**
   * Flow chuẩn:
   * 1) POST /api/v1/files/upload (purpose=BUSINESS_LICENSE) → publicUrl
   * 2) POST /api/v1/profile/brand-profiles/me/documents { businessLicenseUrl }
   */
  uploadLicense(): void {
    if (!this.canEditAndUpload || !this.selectedFile) {
      this.toast.error('Vui lòng chọn file giấy phép kinh doanh.');
      return;
    }

    const file = this.selectedFile;
    this.syncView(() => {
      this.uploading = true;
    });

    this.fileApi
      .upload(file, 'BUSINESS_LICENSE')
      .pipe(
        switchMap((uploadRes) => {
          const uploaded = unwrapApiResult<FileView>(uploadRes);
          const publicUrl = uploaded?.publicUrl?.trim();
          if (!publicUrl) {
            throw new Error('UPLOAD_URL_MISSING');
          }
          return this.brandApi
            .uploadDocument(publicUrl)
            .pipe(map((documentResponse) => ({ documentResponse, uploaded })));
        }),
        finalize(() => {
          this.syncView(() => {
            this.uploading = false;
          });
        }),
      )
      .subscribe({
        next: ({ documentResponse, uploaded }) => {
          const doc = unwrapApiResult<BrandDocumentView>(documentResponse);
          this.syncView(() => {
            this.document = doc;
            this.licenseFile = uploaded;
            this.submittedThisSession = false;
          });
          this.clearSelectedFile();
          this.toast.success('Đã tải lên giấy phép kinh doanh.');
        },
        error: (err) => {
          if (err?.message === 'UPLOAD_URL_MISSING') {
            this.toast.error('Upload file thành công nhưng thiếu publicUrl.');
            return;
          }
          this.toast.error(
            apiErrorMessage(
              err,
              'Không thể tải lên giấy phép. Kiểm tra file-service và đăng nhập lại nếu phiên hết hạn.',
            ),
          );
        },
      });
  }

  submitDocuments(): void {
    if (!this.canSubmit) return;

    this.syncView(() => {
      this.submitting = true;
    });

    this.brandApi
      .submitDocuments()
      .pipe(
        finalize(() => {
          this.syncView(() => {
            this.submitting = false;
          });
        }),
      )
      .subscribe({
        next: () => {
          this.syncView(() => {
            this.submittedThisSession = true;
          });
          this.toast.success('Đã nộp hồ sơ xét duyệt.');
          this.reload();
        },
        error: (err) => {
          this.toast.error(apiErrorMessage(err, 'Không thể nộp hồ sơ.'));
        },
      });
  }

  private syncView(update?: () => void): void {
    this.zone.run(() => {
      update?.();
      this.cdr.detectChanges();
    });
  }

  private findLinkedLicenseFile(
    files: FileView[],
    document: BrandDocumentView | null,
  ): FileView | null {
    const documentUrl = document?.businessLicenseUrl?.trim();
    if (!documentUrl) return null;
    return files.find((file) => file.publicUrl?.trim() === documentUrl) ?? files[0] ?? null;
  }
}
