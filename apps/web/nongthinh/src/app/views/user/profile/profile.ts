import { isBrandAccount } from '../../../core/auth/brand-access';
import { UploadPolicyService } from '../../../core/service/upload-policy.service';
import { UploadPolicyHint } from '../../../shared/upload-policy-hint/upload-policy-hint';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, finalize, switchMap, throwError } from 'rxjs';
import {
  AdminProfileResponse,
  BrandProfileResponse,
  FarmerProfileResponse,
  ProfileApiService,
  UpdateAdminProfilePayload,
  UpdateBrandProfilePayload,
} from '../../../core/api/profile-api.service';
import { ProfileField, ProfileRole, ProfileService } from '../../../core/service/profile.service';
import { AuthService } from '../../../core/auth/auth.service';
import { ApiResponse, apiErrorCode, apiErrorMessage } from '../../../core/models/api-response';
import { FileApiService } from '../../../core/api/file-api.service';
import { Commune, LocationService, Province } from '../../../core/service/location.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { BrandVerification } from '../brand-verification/brand-verification';
import { FollowPanel } from '../../../shared/follow-panel/follow-panel';


@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [UploadPolicyHint, CommonModule, ReactiveFormsModule, BrandVerification, FollowPanel],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class UserProfile implements OnInit {
  readonly uploadPolicies = inject(UploadPolicyService).watch(isBrandAccount(inject(AuthService).currentUser()) ? [] : ['AVATAR']);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly profileApi = inject(ProfileApiService);
  private readonly fileApi = inject(FileApiService);
  private readonly profileService = inject(ProfileService);
  private readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly locationService = inject(LocationService);

  form!: FormGroup;
  profileFields: ProfileField[] = [];
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly uploadingAvatar = signal(false);
  readonly avatarPreviewUrl = signal<string | null>(null);
  readonly editing = signal(false);
  readonly profile = signal<FarmerProfileResponse | null>(null);
  readonly provinces = signal<Province[]>([]);
  readonly communes = signal<Commune[]>([]);
  readonly loadingLocations = signal(false);
  role: ProfileRole = 'FARMER';

  get isBrand(): boolean {
    return this.role === 'BRAND';
  }

  get displayName(): string {
    const current = this.profile();
    return current
      ? [current.firstName, current.lastName].filter(Boolean).join(' ') || 'Nông dân Nông Thịnh'
      : 'Nông dân Nông Thịnh';
  }

  get initials(): string {
    return this.displayName
      .split(/\s+/)
      .filter(Boolean)
      .slice(-2)
      .map((part) => part.charAt(0).toLocaleUpperCase('vi'))
      .join('');
  }

  get email(): string {
    return this.authService.currentUser()?.email ?? '';
  }

  get genderLabel(): string {
    switch (this.profile()?.gender) {
      case 'MALE':
        return 'Nam';
      case 'FEMALE':
        return 'Nữ';
      case 'OTHER':
        return 'Khác';
      default:
        return 'Chưa cập nhật';
    }
  }

  get locationLabel(): string {
    const current = this.profile();
    if (!current) return 'Chưa cập nhật';
    return (
      [current.communeName, current.provinceName].filter(Boolean).join(', ') || 'Chưa cập nhật'
    );
  }

  ngOnInit() {
    this.role = this.resolveRole();
    if (this.isBrand) {
      this.loading.set(false);
      return;
    }
    this.profileFields = this.profileService.getProfileFields(this.role);
    this.buildForm(this.profileFields);
    this.loadProfile();
  }

  private resolveRole(): ProfileRole {
    const user = this.authService.currentUser();
    const role = (user?.role ?? '').replace(/^ROLE_/, '');
    if (role === 'ADMIN') return 'ADMIN';
    if (role === 'BRAND' || role === 'BRAND_PENDING') return 'BRAND';
    return 'FARMER';
  }

  private buildForm(fields: ProfileField[]) {
    const controls: Record<string, ReturnType<FormBuilder['control']>> = {};
    for (const field of fields) {
      const validators = field.required ? [Validators.required] : [];
      controls[field.key] = this.fb.control('', validators);
    }
    this.form = this.fb.group(controls);
  }

  private loadProfile() {
    this.loading.set(true);

    const request$ = this.getProfileRequest();
    request$.subscribe({
      next: (res) => {
        const result = res.result ?? null;
        this.patchFormWith(result);
        if (this.role === 'FARMER') {
          this.profile.set(result as FarmerProfileResponse | null);
        }
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        const code = apiErrorCode(err);
        if (code === 'PRO_PROFILE_NOT_FOUND') {
          this.toast.info('Hồ sơ chưa có dữ liệu. Hãy điền và lưu thông tin của bạn.');
          return;
        }
        this.toast.error(apiErrorMessage(err, 'Không thể tải hồ sơ.'));
      },
    });
  }

  private getProfileRequest(): Observable<
    ApiResponse<FarmerProfileResponse | BrandProfileResponse | AdminProfileResponse>
  > {
    switch (this.role) {
      case 'BRAND':
        return this.profileApi.getMyBrandProfile();
      case 'ADMIN':
        return this.profileApi.getMyAdminProfile();
      case 'FARMER':
      default:
        return this.profileApi.getMyFarmerProfile();
    }
  }

  private patchFormWith(
    profile: FarmerProfileResponse | BrandProfileResponse | AdminProfileResponse | null,
  ) {
    if (!profile || !this.form) return;
    const patch: Record<string, string> = {};
    for (const field of this.profileFields) {
      const raw = (profile as unknown as Record<string, unknown>)[field.key];
      if (raw == null) {
        patch[field.key] = '';
      } else {
        patch[field.key] = String(raw);
      }
    }
    this.form.patchValue(patch);
  }

  startEditing(): void {
    const current = this.profile();
    if (current) {
      this.patchFormWith(current);
    }
    this.editing.set(true);
    this.loadLocationOptions();
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (!file) return;

    const validationError = this.uploadPolicies.validate(file, 'AVATAR');
    if (validationError) {
      input.value = '';
      this.toast.error(validationError);
      return;
    }

    this.showAvatarPreview(file);
    this.uploadingAvatar.set(true);
    this.fileApi
      .upload(file, 'AVATAR')
      .pipe(
        switchMap((uploadResponse) => {
          const publicUrl = uploadResponse.result?.publicUrl?.trim();
          if (!publicUrl) {
            return throwError(() => new Error('UPLOAD_URL_MISSING'));
          }
          return this.profileApi.updateMyFarmerAvatar(publicUrl);
        }),
        finalize(() => {
          this.uploadingAvatar.set(false);
          input.value = '';
          this.clearAvatarPreview();
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (response) => {
          const updatedProfile = response.result;
          if (!updatedProfile) {
            this.toast.error('Chưa thể xác nhận ảnh đại diện đã được cập nhật. Vui lòng tải lại trang.');
            return;
          }
          const updatedProfileWithLocation = this.withLocationNames(updatedProfile);
          this.profile.set(updatedProfileWithLocation);
          this.patchFormWith(updatedProfileWithLocation);
          this.authService.loadMe().pipe(takeUntilDestroyed(this.destroyRef)).subscribe();
          this.toast.success('Đã cập nhật ảnh đại diện.');
        },
        error: (err) => {
          if (err instanceof Error && err.message === 'UPLOAD_URL_MISSING') {
          this.toast.error('Chưa thể sử dụng ảnh vừa tải lên. Vui lòng thử lại.');
            return;
          }
          this.toast.error(apiErrorMessage(err, 'Không thể cập nhật ảnh đại diện.'));
        },
      });
  }

  private showAvatarPreview(file: File): void {
    this.clearAvatarPreview();
    if (typeof URL.createObjectURL === 'function') {
      this.avatarPreviewUrl.set(URL.createObjectURL(file));
    }
  }

  private clearAvatarPreview(): void {
    const previewUrl = this.avatarPreviewUrl();
    if (previewUrl && typeof URL.revokeObjectURL === 'function') {
      URL.revokeObjectURL(previewUrl);
    }
    this.avatarPreviewUrl.set(null);
  }

  cancelEditing(): void {
    const current = this.profile();
    if (current) {
      this.patchFormWith(current);
    }
    this.editing.set(false);
  }

  onProvinceChange(event: Event): void {
    const provinceId = (event.target as HTMLSelectElement).value;
    this.form.get('communeId')?.setValue('');
    this.communes.set([]);
    if (provinceId) {
      this.loadCommunes(provinceId);
    }
  }

  private loadLocationOptions(): void {
    this.loadingLocations.set(true);
    this.locationService.getProvinces().subscribe({
      next: (provinces) => {
        this.provinces.set(provinces);
        const provinceId = String(this.form.get('provinceId')?.value ?? '');
        if (provinceId) {
          this.loadCommunes(provinceId, String(this.form.get('communeId')?.value ?? ''));
        } else {
          this.loadingLocations.set(false);
        }
      },
      error: () => {
        this.loadingLocations.set(false);
        this.toast.error('Không thể tải danh sách tỉnh, thành phố.');
      },
    });
  }

  private loadCommunes(provinceId: string, selectedCommuneId = ''): void {
    this.loadingLocations.set(true);
    this.locationService.getCommunesByProvince(provinceId).subscribe({
      next: (communes) => {
        this.communes.set(communes);
        if (selectedCommuneId) {
          this.form.get('communeId')?.setValue(selectedCommuneId);
        }
        this.loadingLocations.set(false);
      },
      error: () => {
        this.communes.set([]);
        this.loadingLocations.set(false);
        this.toast.error('Không thể tải danh sách phường, xã.');
      },
    });
  }

  onSubmit() {
    if (!this.form || this.form.invalid) {
      this.form?.markAllAsTouched();
      return;
    }
    this.saving.set(true);

    const raw = this.form.value as Record<string, string>;
    const payload: Record<string, string | null> = {};
    for (const [key, value] of Object.entries(raw)) {
      const trimmed = value?.trim();
      payload[key] = trimmed || null;
    }
    const request$ = this.getUpdateRequest(payload);
    request$.subscribe({
      next: (res) => {
        this.saving.set(false);
        if (this.role === 'FARMER' && res.result) {
          this.profile.set(this.withLocationNames(res.result as FarmerProfileResponse));
        }
        this.editing.set(false);
        this.toast.success('Đã cập nhật hồ sơ thành công.');
      },
      error: (err) => {
        this.saving.set(false);
        this.toast.error(apiErrorMessage(err, 'Không thể cập nhật hồ sơ.'));
      },
    });
  }

  private withLocationNames(profile: FarmerProfileResponse): FarmerProfileResponse {
    const current = this.profile();
    const provinceName = this.provinces().find((item) => item.id === profile.provinceId)?.name;
    const communeName = this.communes().find((item) => item.id === profile.communeId)?.name;
    return {
      ...profile,
      provinceName:
        profile.provinceName ??
        provinceName ??
        (current?.provinceId === profile.provinceId ? current.provinceName : null),
      communeName:
        profile.communeName ??
        communeName ??
        (current?.communeId === profile.communeId ? current.communeName : null),
    };
  }

  private getUpdateRequest(
    payload: Record<string, string | null>,
  ): Observable<ApiResponse<FarmerProfileResponse | BrandProfileResponse | AdminProfileResponse>> {
    switch (this.role) {
      case 'BRAND':
        return this.profileApi.updateMyBrandProfile(payload as UpdateBrandProfilePayload);
      case 'ADMIN':
        return this.profileApi.updateMyAdminProfile(payload as UpdateAdminProfilePayload);
      case 'FARMER':
      default:
        return this.profileApi.updateMyFarmerProfile({
          firstName: payload['firstName'] ?? '',
          lastName: payload['lastName'] ?? '',
          gender: payload['gender'] ?? '',
          phone: payload['phone'],
          provinceId: payload['provinceId'],
          communeId: payload['communeId'],
          addressDetail: payload['addressDetail'],
          avatarUrl: payload['avatarUrl'],
        });
    }
  }
}
