import {
  Component,
  OnInit,
  OnDestroy,
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
  OtpConfigView,
  OtpResendCooldownView,
} from '../../core/api/auth-api.service';
import { ProfileService, ProfileField } from '../../core/service/profile.service';
import { AuthService } from '../../core/auth/auth.service';
import { apiErrorMessage, unwrapApiResult } from '../../core/models/api-response';
import { LocationService } from '../../core/service/location.service';
import { ToastService } from '../../shared/toast/toast.service';
import { finalize, timeout, TimeoutError } from 'rxjs';

export const REGISTRATION_REQUEST_TIMEOUT_MS = 30_000;
/** Khớp DefaultParamValueConstant.DEFAULT_OTP_RESEND_COOLDOWN_SECONDS */
export const DEFAULT_OTP_RESEND_COOLDOWN_SECONDS = 60;
/** Khớp DefaultParamValueConstant.DEFAULT_OTP_LENGTH + VerifyEmailOtpRequest (4–8) */
export const DEFAULT_OTP_LENGTH = 6;
export const MIN_OTP_LENGTH = 4;
export const MAX_OTP_LENGTH = 8;

type AuthView =
  | 'login'
  | 'role'
  | 'register'
  | 'profile'
  | 'otp'
  | 'registration-pending';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './auth.html',
  styleUrl: './auth.scss',
})
export class Auth implements OnInit, OnDestroy, AfterViewInit {
  currentView: AuthView = 'login';
  isSubmitting = false;
  isRedirecting = false;

  selectedRole: RegistrationType = 'FARMER';
  profileFields: ProfileField[] = [];

  registerFormGroup!: FormGroup;
  profileFormGroup!: FormGroup;
  otpFormGroup!: FormGroup;

  registrationEmail = '';
  private pendingEmail = '';
  private pendingPassword = '';

  /** Thời gian chờ gửi lại OTP (giây), lấy từ system param. */
  resendCooldownSeconds = DEFAULT_OTP_RESEND_COOLDOWN_SECONDS;
  /** Số giây còn lại trước khi được gửi lại OTP. */
  resendRemainingSeconds = 0;
  private resendTimerId: ReturnType<typeof setInterval> | null = null;

  /** Độ dài OTP (số ô), lấy từ system param OTP_LENGTH. */
  otpLength = DEFAULT_OTP_LENGTH;
  otpDigitKeys: string[] = [];

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
  @ViewChild('otpForm') otpFormRef!: ElementRef;
  @ViewChild('pendingForm') pendingFormRef!: ElementRef;

  constructor() {
    this.registerFormGroup = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
    });
    this.profileFormGroup = this.fb.group({});
    this.rebuildOtpForm(DEFAULT_OTP_LENGTH);
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

  ngOnDestroy() {
    this.clearResendTimer();
  }

  private initFormVisibility() {
    const allForms = [
      this.loginFormRef?.nativeElement,
      this.roleFormRef?.nativeElement,
      this.registerFormRef?.nativeElement,
      this.profileFormRef?.nativeElement,
      this.otpFormRef?.nativeElement,
      this.pendingFormRef?.nativeElement,
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
      case 'otp':
        return this.otpFormRef?.nativeElement ?? null;
      case 'registration-pending':
        return this.pendingFormRef?.nativeElement ?? null;
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
   * Backend tự gửi OTP email → chuyển sang OTP view
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
          this.otpFormGroup.reset();
          this.enterOtpView();
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
          this.otpFormGroup.reset();
          this.enterOtpView();
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
      return 'Yêu cầu đăng ký quá thời gian. Vui lòng kiểm tra email OTP trước khi thử lại.';
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

  /**
   * Flow mới: OTP verify → POST /otp/verify (chỉ email + otp)
   * Farmer: thành công → redirect Keycloak login
   * Brand: thành công → hiện "chờ duyệt"
   */
  onOtpSubmit() {
    const otp = this.collectOtpFromForm();
    if (otp.length !== this.otpLength) {
      this.toast.error(`Vui lòng nhập đủ ${this.otpLength} chữ số OTP.`);
      return;
    }

    this.isSubmitting = true;

    this.authApi
      .verifyEmailOtp({ email: this.pendingEmail, otp })
      .pipe(finalize(() => this.finishSubmitting()))
      .subscribe({
        next: () => {
          if (this.selectedRole === 'BRAND') {
            this.goToView('registration-pending');
            this.toast.success(
              'Đăng ký thương hiệu thành công. Tài khoản đang chờ quản trị viên duyệt.',
            );
            this.changeDetectorRef.markForCheck();
          } else {
            this.toast.success('Xác thực email thành công. Đang chuyển đến đăng nhập...');
            this.changeDetectorRef.markForCheck();
            this.authService.login();
          }
        },
        error: (err) => {
          this.toast.error(apiErrorMessage(err, 'Mã OTP không hợp lệ hoặc đã hết hạn.'));
        },
      });
  }

  get canResendOtp(): boolean {
    return this.resendRemainingSeconds <= 0 && !this.isSubmitting;
  }

  get isResendCooldownActive(): boolean {
    return this.resendRemainingSeconds > 0;
  }

  get resendCountdownLabel(): string {
    const total = Math.max(0, this.resendRemainingSeconds);
    const minutes = Math.floor(total / 60);
    const seconds = total % 60;
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }

  /**
   * Flow mới: Resend OTP → POST /otp/resend (chỉ email)
   */
  resendOtp(event: Event) {
    event.preventDefault();
    if (!this.pendingEmail || !this.canResendOtp) {
      return;
    }

    this.isSubmitting = true;

    this.authApi.resendEmailOtp({ email: this.pendingEmail }).pipe(
      finalize(() => this.finishSubmitting()),
    ).subscribe({
      next: (res) => {
        const cooldown = this.resolveCooldownSeconds(unwrapApiResult<OtpResendCooldownView>(res));
        this.resendCooldownSeconds = cooldown;
        this.startResendCountdown(cooldown);
        this.toast.success('Mã OTP đã được gửi lại.');
      },
      error: (err) => {
        this.toast.error(apiErrorMessage(err, 'Không thể gửi lại mã OTP.'));
      },
    });
  }

  private enterOtpView() {
    this.goToView('otp');
    this.loadOtpConfigAndStart();
  }

  private loadOtpConfigAndStart() {
    if (!isPlatformBrowser(this.platformId)) {
      this.applyOtpConfig(this.otpLength, this.resendCooldownSeconds);
      return;
    }

    this.authApi.getOtpConfig().subscribe({
      next: (res) => {
        const config = unwrapApiResult<OtpConfigView>(res);
        this.applyOtpConfig(
          this.resolveOtpLength(config),
          this.resolveCooldownSeconds(config),
        );
      },
      error: () => {
        this.applyOtpConfig(DEFAULT_OTP_LENGTH, DEFAULT_OTP_RESEND_COOLDOWN_SECONDS);
      },
    });
  }

  private applyOtpConfig(otpLength: number, cooldownSeconds: number) {
    this.rebuildOtpForm(otpLength);
    this.resendCooldownSeconds = cooldownSeconds;
    this.startResendCountdown(cooldownSeconds);
  }

  private rebuildOtpForm(length: number) {
    const otpLength = this.clampOtpLength(length);
    this.otpLength = otpLength;
    this.otpDigitKeys = Array.from({ length: otpLength }, (_, i) => `d${i}`);

    const controls: Record<string, ReturnType<FormBuilder['control']>> = {};
    for (const key of this.otpDigitKeys) {
      controls[key] = this.fb.control('');
    }
    this.otpFormGroup = this.fb.group(controls);
    this.changeDetectorRef.markForCheck();
  }

  private resolveOtpLength(view: OtpConfigView | null): number {
    const length = view?.otpLength;
    if (typeof length === 'number' && Number.isFinite(length)) {
      return this.clampOtpLength(length);
    }
    return DEFAULT_OTP_LENGTH;
  }

  private clampOtpLength(length: number): number {
    return Math.min(MAX_OTP_LENGTH, Math.max(MIN_OTP_LENGTH, Math.floor(length)));
  }

  private resolveCooldownSeconds(
    view: Pick<OtpResendCooldownView, 'resendCooldownSeconds'> | null,
  ): number {
    const seconds = view?.resendCooldownSeconds;
    if (typeof seconds === 'number' && Number.isFinite(seconds) && seconds > 0) {
      return Math.floor(seconds);
    }
    return DEFAULT_OTP_RESEND_COOLDOWN_SECONDS;
  }

  private startResendCountdown(seconds: number) {
    this.clearResendTimer();
    this.resendRemainingSeconds = Math.max(0, Math.floor(seconds));
    this.changeDetectorRef.markForCheck();

    if (this.resendRemainingSeconds <= 0 || !isPlatformBrowser(this.platformId)) {
      return;
    }

    this.resendTimerId = setInterval(() => {
      this.resendRemainingSeconds = Math.max(0, this.resendRemainingSeconds - 1);
      if (this.resendRemainingSeconds <= 0) {
        this.clearResendTimer();
      }
      this.changeDetectorRef.markForCheck();
    }, 1000);
  }

  private clearResendTimer() {
    if (this.resendTimerId != null) {
      clearInterval(this.resendTimerId);
      this.resendTimerId = null;
    }
  }

  backToRegister() {
    this.goToView('register', 'backward');
  }

  backToRoleSelect() {
    this.goToView('role', 'backward');
  }

  backToProfile() {
    this.goToView('profile', 'backward');
  }

  onOtpInput(event: Event) {
    const input = event.target as HTMLInputElement;
    const digit = input.value.replace(/\D/g, '').slice(0, 1);
    if (digit !== input.value) {
      input.value = digit;
      const controlName = input.getAttribute('formcontrolname');
      if (controlName) {
        this.otpFormGroup.get(controlName)?.setValue(digit, { emitEvent: false });
      }
    }
    if (digit.length === 1) {
      const nextInput = input.nextElementSibling as HTMLInputElement | null;
      if (nextInput) {
        nextInput.focus();
      }
    }
  }

  onOtpKeydown(event: KeyboardEvent) {
    const input = event.target as HTMLInputElement;
    if (event.key === 'Backspace' && input.value === '') {
      const prevInput = input.previousElementSibling as HTMLInputElement | null;
      if (prevInput) {
        prevInput.focus();
      }
    }
  }

  private collectOtpFromForm(): string {
    const v = this.otpFormGroup.value as Record<string, string>;
    return this.otpDigitKeys.map((k) => (v[k] ?? '').trim()).join('');
  }
}
