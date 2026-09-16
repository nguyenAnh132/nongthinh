import {
  Component,
  OnInit,
  AfterViewInit,
  ViewChild,
  ElementRef,
  inject,
  PLATFORM_ID,
  ChangeDetectorRef,
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import gsap from 'gsap';
import {
  AuthApiService,
  RegisterFarmerPayload,
  RegisterBrandPayload,
  RegistrationType,
} from '../../core/api/auth-api.service';
import { ProfileService, ProfileField } from '../../core/service/profile.service';
import { AuthService } from '../../core/auth/auth.service';
import { apiErrorMessage } from '../../core/models/api-response';
import { LocationService } from '../../core/service/location.service';
import { ToastService } from '../../shared/toast/toast.service';
import { finalize, timeout, TimeoutError } from 'rxjs';

export const REGISTRATION_REQUEST_TIMEOUT_MS = 30_000;
type AuthView =
  | 'login'
  | 'role'
  | 'register'
  | 'profile'
  | 'registration-success';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './auth.html',
  styleUrl: './auth.scss',
})
export class Auth implements OnInit, AfterViewInit {
  currentView: AuthView = 'login';
  isSubmitting = false;
  isRedirecting = false;

  selectedRole: RegistrationType = 'FARMER';
  profileFields: ProfileField[] = [];

  registerFormGroup!: FormGroup;
  profileFormGroup!: FormGroup;

  registrationEmail = '';
  private pendingEmail = '';
  private pendingPassword = '';

  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly authApi = inject(AuthApiService);
  private readonly profileService = inject(ProfileService);
  private readonly authService = inject(AuthService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly locationService = inject(LocationService);
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly toast = inject(ToastService);

  provinces: any[] = [];
  communes: any[] = [];

  @ViewChild('container') containerRef!: ElementRef;
  @ViewChild('loginForm') loginFormRef!: ElementRef;
  @ViewChild('roleForm') roleFormRef!: ElementRef;
  @ViewChild('registerForm') registerFormRef!: ElementRef;
  @ViewChild('profileForm') profileFormRef!: ElementRef;
  @ViewChild('successForm') successFormRef!: ElementRef;

  constructor() {
    this.registerFormGroup = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
    });
    this.profileFormGroup = this.fb.group({});
  }

  ngOnInit() {
    const isRegisterRoute = this.route.snapshot.url[0]?.path === 'register';
    this.currentView = isRegisterRoute ? 'role' : 'login';

    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    this.authService.ensureMeLoaded().subscribe((isAuthenticated) => {
      if (isAuthenticated) {
        this.authService.navigateAfterLogin();
      } else if (!isRegisterRoute) {
        this.redirectToKeycloak();
      }
    });

    this.locationService.getProvinces().subscribe((res) => {
      this.provinces = res;
      this.changeDetectorRef.markForCheck();
    });
  }

  ngAfterViewInit() {
    this.initFormVisibility();
    if (this.currentView !== 'login') {
      this.animateEntrance();
    }
  }

  private initFormVisibility() {
    const allForms = [
      this.loginFormRef?.nativeElement,
      this.roleFormRef?.nativeElement,
      this.registerFormRef?.nativeElement,
      this.profileFormRef?.nativeElement,
      this.successFormRef?.nativeElement,
    ].filter(Boolean);

    allForms.forEach((form) => gsap.set(form, { display: 'none' }));
    const active = this.getFormRef(this.currentView);
    if (active) {
      gsap.set(active, { display: 'block' });
    }
  }

  private animateEntrance() {
    const activeForm = this.getFormRef(this.currentView);
    if (!activeForm || !this.containerRef) return;

    const tl = gsap.timeline({ defaults: { ease: 'power3.out' } });
    tl.from(this.containerRef.nativeElement, {
      y: 40,
      opacity: 0,
      duration: 0.8,
    }).from(
      activeForm.children,
      { y: 20, opacity: 0, duration: 0.5, stagger: 0.1 },
      '-=0.4'
    );
  }

  private getFormRef(view: AuthView): HTMLElement | null {
    switch (view) {
      case 'login':
        return this.loginFormRef?.nativeElement ?? null;
      case 'role':
        return this.roleFormRef?.nativeElement ?? null;
      case 'register':
        return this.registerFormRef?.nativeElement ?? null;
      case 'profile':
        return this.profileFormRef?.nativeElement ?? null;
      case 'registration-success':
        return this.successFormRef?.nativeElement ?? null;
      default:
        return null;
    }
  }

  goToView(targetView: AuthView, direction: 'forward' | 'backward' = 'forward') {
    if (this.currentView === targetView) return;

    const outForm = this.getFormRef(this.currentView);
    const inForm = this.getFormRef(targetView);
    if (!outForm || !inForm) return;

    const outX = direction === 'forward' ? -30 : 30;
    const inX = direction === 'forward' ? 30 : -30;

    this.currentView = targetView;

    if (targetView === 'login') {
      void this.router.navigate(['/login'], { replaceUrl: true });
    } else if (targetView === 'role') {
      void this.router.navigate(['/register'], { replaceUrl: true });
    }

    const tl = gsap.timeline();
    tl.to(outForm.children, {
      opacity: 0,
      x: outX,
      duration: 0.3,
      stagger: 0.05,
      ease: 'power2.in',
    })
      .add(() => {
        gsap.set(outForm, { display: 'none' });
        gsap.set(inForm, { display: 'block' });
      })
      .fromTo(
        inForm.children,
        { opacity: 0, x: inX },
        {
          opacity: 1,
          x: 0,
          duration: 0.4,
          stagger: 0.05,
          ease: 'power3.out',
        }
      );
  }

  onRoleSelect(role: RegistrationType) {
    this.selectedRole = role;
    this.goToView('register');
  }

  onLoginSubmit() {
    this.redirectToKeycloak();
  }

  onProvinceChange(event: Event) {
    const provinceId = (event.target as HTMLSelectElement).value;
    if (provinceId) {
      this.locationService.getCommunesByProvince(provinceId).subscribe((res) => {
        this.communes = res;
        this.changeDetectorRef.markForCheck();
      });
    } else {
      this.communes = [];
      this.changeDetectorRef.markForCheck();
    }
    
    if (this.profileFormGroup.contains('communeId')) {
      this.profileFormGroup.get('communeId')?.setValue('');
    }
    if (this.profileFormGroup.contains('officeCommuneId')) {
      this.profileFormGroup.get('officeCommuneId')?.setValue('');
    }
  }

  getOptionsForField(field: ProfileField): { value: string | number; label: string }[] {
    if (field.key === 'gender' && field.options) {
      return field.options;
    }
    if (field.key === 'provinceId' || field.key === 'officeProvinceId') {
      return this.provinces.map(p => ({ value: p.id, label: p.name }));
    }
    if (field.key === 'communeId' || field.key === 'officeCommuneId') {
      return this.communes.map(c => ({ value: c.id, label: c.name }));
    }
    return [];
  }

  private redirectToKeycloak() {
    if (this.isRedirecting) {
      return;
    }
    this.isRedirecting = true;
    this.authService.login();
  }

  onRegisterSubmit() {
    if (this.registerFormGroup.invalid) {
      this.registerFormGroup.markAllAsTouched();
      return;
    }
    const { email, password, confirmPassword } = this.registerFormGroup.value;
    if (password !== confirmPassword) {
      this.toast.error('Mật khẩu xác nhận không khớp.');
      return;
    }

    this.pendingEmail = email;
    this.registrationEmail = email;
    this.pendingPassword = password;
    this.profileFields = this.profileService.getProfileFields(this.selectedRole);
    this.buildProfileForm(this.profileFields);
    this.goToView('profile');
  }

  private buildProfileForm(fields: ProfileField[]) {
    const controls: Record<string, ReturnType<FormBuilder['control']>> = {};
    for (const field of fields) {
      const validators = field.required ? [Validators.required] : [];
      controls[field.key] = this.fb.control('', validators);
    }
    this.profileFormGroup = this.fb.group(controls);
  }

  /**
   * Flow mới: Submit profile → POST /farmers hoặc /brands
   * (gộp email + password + profile fields trong 1 request)
   * Đăng ký thành công → hiển thị nút đăng nhập
   */
  onProfileSubmit() {
    if (this.profileFields.length === 0) {
      this.toast.error('Không có trường hồ sơ. Vui lòng quay lại chọn vai trò.');
      return;
    }
    if (this.profileFormGroup.invalid) {
      this.profileFormGroup.markAllAsTouched();
      return;
    }

    const profileData = this.collectProfilePayload();
    if (!this.hasCompleteLocation(profileData)) {
      return;
    }
    this.isSubmitting = true;

    if (this.selectedRole === 'BRAND') {
      const payload: RegisterBrandPayload = {
        email: this.pendingEmail,
        password: this.pendingPassword,
        temporary: false,
        enabled: true,
        brandName: profileData['brandName'] ?? '',
        taxCode: profileData['taxCode'] || undefined,
        description: profileData['description'] || undefined,
        phone: profileData['phone'] ?? '',
        officeProvinceId: profileData['officeProvinceId'] || undefined,
        officeCommuneId: profileData['officeCommuneId'] || undefined,
        officeAddressDetail: profileData['officeAddressDetail'] || undefined,
        representativeName: profileData['representativeName'] ?? '',
        representativePhone: profileData['representativePhone'] ?? '',
        representativeEmail: profileData['representativeEmail'] ?? '',
        websiteUrl: profileData['websiteUrl'] || undefined,
      };

      this.authApi.registerBrand(payload).pipe(
        timeout(REGISTRATION_REQUEST_TIMEOUT_MS),
        finalize(() => this.finishSubmitting()),
      ).subscribe({
        next: () => {
          this.showRegistrationSuccess();
        },
        error: (err) => {
          this.toast.error(
            this.registrationErrorMessage(err, 'Không thể đăng ký thương hiệu.'),
          );
        },
      });
    } else {
      const payload: RegisterFarmerPayload = {
        email: this.pendingEmail,
        password: this.pendingPassword,
        temporary: false,
        enabled: true,
        firstName: profileData['firstName'] ?? '',
        lastName: profileData['lastName'] ?? '',
        gender: profileData['gender'] || 'OTHER',
        phone: profileData['phone'] ?? '',
        provinceId: profileData['provinceId'] || undefined,
        communeId: profileData['communeId'] || undefined,
        addressDetail: profileData['addressDetail'] || undefined,
      };

      this.authApi.registerFarmer(payload).pipe(
        timeout(REGISTRATION_REQUEST_TIMEOUT_MS),
        finalize(() => this.finishSubmitting()),
      ).subscribe({
        next: () => {
          this.showRegistrationSuccess();
        },
        error: (err) => {
          this.toast.error(
            this.registrationErrorMessage(err, 'Không thể đăng ký tài khoản.'),
          );
        },
      });
    }
  }

  /** Angular 21 zoneless: HTTP/async không tự CD — phải báo sau khi đổi state. */
  private finishSubmitting(): void {
    this.isSubmitting = false;
    this.changeDetectorRef.markForCheck();
  }

  private collectProfilePayload(): Record<string, string> {
    const raw = this.profileFormGroup.value as Record<string, string>;
    const payload: Record<string, string> = {};
    for (const [key, value] of Object.entries(raw)) {
      const trimmed = value?.trim();
      if (trimmed) {
        payload[key] = trimmed;
      }
    }
    return payload;
  }

  private registrationErrorMessage(err: unknown, fallback: string): string {
    if (err instanceof TimeoutError) {
      return 'Yêu cầu đăng ký quá thời gian. Vui lòng thử đăng nhập để kiểm tra tài khoản trước khi đăng ký lại.';
    }
    return apiErrorMessage(err, fallback);
  }

  private hasCompleteLocation(profileData: Record<string, string>): boolean {
    const provinceKey =
      this.selectedRole === 'BRAND' ? 'officeProvinceId' : 'provinceId';
    const communeKey =
      this.selectedRole === 'BRAND' ? 'officeCommuneId' : 'communeId';
    const hasProvince = Boolean(profileData[provinceKey]);
    const hasCommune = Boolean(profileData[communeKey]);

    if (hasProvince === hasCommune) {
      return true;
    }

    this.profileFormGroup.get(provinceKey)?.markAsTouched();
    this.profileFormGroup.get(communeKey)?.markAsTouched();
    this.toast.error('Vui lòng chọn đầy đủ tỉnh/thành phố và phường/xã.');
    return false;
  }

  private showRegistrationSuccess(): void {
    this.pendingPassword = '';
    this.registerFormGroup.reset();
    this.goToView('registration-success');
    this.changeDetectorRef.markForCheck();
  }

  backToRegister() {
    this.goToView('register', 'backward');
  }

  backToRoleSelect() {
    this.goToView('role', 'backward');
  }

}
