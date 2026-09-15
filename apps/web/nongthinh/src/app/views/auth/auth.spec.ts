import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
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
  let authServiceLogin: ReturnType<typeof vi.fn>;
  let toastError: ReturnType<typeof vi.fn>;
  let toastSuccess: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    registerBrand = vi.fn(() => of(void 0));
    registerFarmer = vi.fn(() => of(void 0));
    authServiceLogin = vi.fn();
    toastError = vi.fn();
    toastSuccess = vi.fn();

    await TestBed.configureTestingModule({
      imports: [Auth],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { url: [{ path: 'register' }] } } },
        {
          provide: AuthApiService,
          useValue: {
            registerBrand,
            registerFarmer,
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
      gender: 'MALE',
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
      gender: 'MALE',
      phone: '0912345678',
    });

    component.onProfileSubmit();

    expect(component.isSubmitting).toBe(false);
    expect(toastError).toHaveBeenCalledWith('Email đã được sử dụng.');
  });

  it.each(['FARMER', 'BRAND'] as const)('shows registration success and waits for login for %s', (role) => {
    fixture.detectChanges();
    if (role === 'BRAND') {
      prepareBrandProfile();
      component.profileFormGroup.patchValue({
        brandName: 'Thương hiệu', phone: '0912345678',
        representativeName: 'Nguyễn Văn A', representativePhone: '0987654321',
        representativeEmail: 'representative@example.com',
      });
    } else {
      prepareFarmerProfile();
      component.profileFormGroup.patchValue({
        firstName: 'Nguyễn', lastName: 'Văn A', gender: 'MALE', phone: '0912345678',
      });
    }
    vi.spyOn(component, 'goToView').mockImplementation((view) => {
      component.currentView = view;
    });

    component.onProfileSubmit();
    fixture.detectChanges();

    expect(component.currentView).toBe('registration-success');
    expect(component.isSubmitting).toBe(false);
    expect(component.registerFormGroup.value.password).toBeNull();
    expect(authServiceLogin).not.toHaveBeenCalled();
    const success = fixture.nativeElement.querySelector('[role="status"]');
    expect(success.textContent).toContain('Đăng ký thành công');
    expect(success.textContent).toContain(role === 'BRAND' ? 'brand@example.com' : 'farmer@example.com');
    success.querySelector('button').click();
    expect(authServiceLogin).toHaveBeenCalledOnce();
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
