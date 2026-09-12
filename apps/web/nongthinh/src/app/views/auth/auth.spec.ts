import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { NEVER, of, throwError } from 'rxjs';

import { AuthApiService, RegisterBrandPayload } from '../../core/api/auth-api.service';
import { AuthService } from '../../core/auth/auth.service';
import { LocationService } from '../../core/service/location.service';
import { ToastService } from '../../shared/toast/toast.service';
import { Auth, REGISTRATION_REQUEST_TIMEOUT_MS } from './auth';

describe('Auth', () => {
  let component: Auth;
  let fixture: ComponentFixture<Auth>;
  let registerBrand: ReturnType<typeof vi.fn>;
  let registerFarmer: ReturnType<typeof vi.fn>;
  let verifyEmailOtp: ReturnType<typeof vi.fn>;
  let authServiceLogin: ReturnType<typeof vi.fn>;
  let toastError: ReturnType<typeof vi.fn>;
  let toastSuccess: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    registerBrand = vi.fn(() => of(void 0));
    registerFarmer = vi.fn(() => of(void 0));
    verifyEmailOtp = vi.fn(() => of(void 0));
    authServiceLogin = vi.fn();
    toastError = vi.fn();
    toastSuccess = vi.fn();

    await TestBed.configureTestingModule({
      imports: [Auth],
      providers: [
        provideRouter([]),
        {
          provide: AuthApiService,
          useValue: {
            registerBrand,
            registerFarmer,
            verifyEmailOtp,
            getOtpConfig: vi.fn(() =>
              of({ result: { otpLength: 6, resendCooldownSeconds: 60 } }),
            ),
            resendEmailOtp: vi.fn(() =>
              of({ result: { resendCooldownSeconds: 60 } }),
            ),
          },
        },
        {
          provide: AuthService,
          useValue: {
            ensureMeLoaded: vi.fn(() => of(false)),
            navigateAfterLogin: vi.fn(),
            login: authServiceLogin,
          },
        },
        {
          provide: LocationService,
          useValue: {
            getProvinces: vi.fn(() => of([])),
            getCommunesByProvince: vi.fn(() => of([])),
          },
        },
        {
          provide: ToastService,
          useValue: {
            error: toastError,
            success: toastSuccess,
            info: vi.fn(),
            warning: vi.fn(),
            dismiss: vi.fn(),
            clear: vi.fn(),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Auth);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('includes the selected office location in a brand registration request', () => {
    prepareBrandProfile();
    component.profileFormGroup.patchValue({
      brandName: 'Nông sản xanh',
      phone: '0912345678',
      officeProvinceId: 'HN',
      officeCommuneId: '4dbdf87a-d09c-4f40-b884-dbb06455e13c',
      officeAddressDetail: '12 Tràng Tiền',
      representativeName: 'Nguyễn Văn A',
      representativePhone: '0987654321',
      representativeEmail: 'representative@example.com',
    });

    component.onProfileSubmit();

    expect(registerBrand).toHaveBeenCalledWith(
      expect.objectContaining<Partial<RegisterBrandPayload>>({
        officeProvinceId: 'HN',
        officeCommuneId: '4dbdf87a-d09c-4f40-b884-dbb06455e13c',
      }),
    );
  });

  it('preserves the selected location in a farmer registration request', () => {
    component.selectedRole = 'FARMER';
    component.registerFormGroup.setValue({
      email: 'farmer@example.com',
      password: 'Password123!',
      confirmPassword: 'Password123!',
    });
    component.onRegisterSubmit();
    component.profileFormGroup.patchValue({
      firstName: 'Nguyễn',
      lastName: 'Văn A',
      phone: '0912345678',
      provinceId: 'HN',
      communeId: '4dbdf87a-d09c-4f40-b884-dbb06455e13c',
    });

    component.onProfileSubmit();

    expect(registerFarmer).toHaveBeenCalledWith(
      expect.objectContaining({
        provinceId: 'HN',
        communeId: '4dbdf87a-d09c-4f40-b884-dbb06455e13c',
      }),
    );
  });

  it('does not register when only a province is selected', () => {
    prepareBrandProfile();
    component.profileFormGroup.patchValue({
      brandName: 'Nông sản xanh',
      phone: '0912345678',
      officeProvinceId: 'HN',
      representativeName: 'Nguyễn Văn A',
      representativePhone: '0987654321',
      representativeEmail: 'representative@example.com',
    });

    component.onProfileSubmit();

    expect(registerBrand).not.toHaveBeenCalled();
    expect(toastError).toHaveBeenCalledWith(expect.stringContaining('phường/xã'));
  });

  it('stops submitting and reports an error when registration times out', () => {
    vi.useFakeTimers();
    try {
      registerBrand.mockReturnValue(NEVER);
      prepareBrandProfile();
      component.profileFormGroup.patchValue({
        brandName: 'Nông sản xanh',
        phone: '0912345678',
        representativeName: 'Nguyễn Văn A',
        representativePhone: '0987654321',
        representativeEmail: 'representative@example.com',
      });

      component.onProfileSubmit();
      expect(component.isSubmitting).toBe(true);

      vi.advanceTimersByTime(REGISTRATION_REQUEST_TIMEOUT_MS + 1);

      expect(component.isSubmitting).toBe(false);
      expect(toastError).toHaveBeenCalledWith(expect.stringContaining('quá thời gian'));
    } finally {
      vi.useRealTimers();
    }
  });

  it('shows a Vietnamese message and unlocks the button when email already exists', () => {
    registerFarmer.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: {
              code: 'BUS_EMAIL_ALREADY_EXISTS',
              message: 'Email already exists',
            },
          }),
      ),
    );
    prepareFarmerProfile();
    component.profileFormGroup.patchValue({
      firstName: 'Nguyễn',
      lastName: 'Văn A',
      phone: '0912345678',
    });

    component.onProfileSubmit();

    expect(component.isSubmitting).toBe(false);
    expect(toastError).toHaveBeenCalledWith('Email đã được sử dụng.');
  });

  it('keeps the brand success message after OTP verify navigates to pending', () => {
    prepareBrandProfile();
    component.otpFormGroup.setValue({
      d0: '1',
      d1: '2',
      d2: '3',
      d3: '4',
      d4: '5',
      d5: '6',
    });
    // Skip GSAP view transitions in unit tests
    vi.spyOn(component, 'goToView').mockImplementation((view) => {
      component.currentView = view;
    });

    component.onOtpSubmit();

    expect(component.currentView).toBe('registration-pending');
    expect(toastSuccess).toHaveBeenCalledWith(
      expect.stringContaining('Đăng ký thương hiệu thành công'),
    );
    expect(component.isSubmitting).toBe(false);
  });

  function prepareBrandProfile(): void {
    component.selectedRole = 'BRAND';
    component.registerFormGroup.setValue({
      email: 'brand@example.com',
      password: 'Password123!',
      confirmPassword: 'Password123!',
    });
    component.onRegisterSubmit();
  }

  function prepareFarmerProfile(): void {
    component.selectedRole = 'FARMER';
    component.registerFormGroup.setValue({
      email: 'farmer@example.com',
      password: 'Password123!',
      confirmPassword: 'Password123!',
    });
    component.onRegisterSubmit();
  }
});
