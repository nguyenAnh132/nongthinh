import { Component, OnInit, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize, of, switchMap } from 'rxjs';
import { AuthApiService, CompleteRegistrationPayload } from '../../core/api/auth-api.service';
import { AuthService } from '../../core/auth/auth.service';
import { apiErrorMessage } from '../../core/models/api-response';

@Component({
  selector: 'app-complete-registration',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './complete-registration.html',
  styleUrls: ['./auth.scss', './complete-registration.scss'],
})
export class CompleteRegistration implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(AuthApiService);
  private readonly platform = inject(PLATFORM_ID);
  private readonly fb = inject(FormBuilder);
  readonly busy = signal(false);
  readonly checking = signal(true);
  readonly error = signal<string | null>(null);
  readonly isBrand = computed(() => ['ROLE_BRAND', 'ROLE_BRAND_PENDING'].includes(this.auth.registrationRequired()?.role ?? ''));
  readonly isFarmer = computed(() => this.auth.registrationRequired()?.role === 'ROLE_FARMER');
  readonly form = this.fb.nonNullable.group({
    firstName: [''], lastName: [''], gender: ['OTHER'], phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
    brandName: [''], representativeName: [''], representativePhone: [''], representativeEmail: [''],
  });

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platform)) return;
    this.checkSession();
  }

  checkSession(): void {
    this.checking.set(true);
    this.auth.loadMe().pipe(finalize(() => this.checking.set(false))).subscribe((user) => {
      if (user) {
        this.auth.navigateAfterLogin();
        return;
      }
      if (!this.auth.registrationRequired()) return;
      const controls = this.form.controls;
      for (const key of ['firstName', 'lastName', 'brandName', 'representativeName', 'representativePhone', 'representativeEmail'] as const) {
        controls[key].clearValidators();
      }
      if (this.isBrand()) {
        controls.brandName.setValidators([Validators.required, Validators.maxLength(200)]);
        controls.representativeName.setValidators([Validators.required, Validators.maxLength(200)]);
        controls.representativePhone.setValidators([Validators.required, Validators.pattern(/^[0-9]{10}$/)]);
        controls.representativeEmail.setValidators([Validators.required, Validators.email, Validators.maxLength(255)]);
      } else {
        controls.firstName.setValidators([Validators.required, Validators.maxLength(200)]);
        controls.lastName.setValidators([Validators.required, Validators.minLength(1), Validators.maxLength(200)]);
      }
      Object.values(controls).forEach((control) => control.updateValueAndValidity());
    });
  }

  submit(): void {
    if (this.busy() || !this.auth.registrationRequired()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Vui lòng điền đủ thông tin. Số điện thoại cần đúng 10 chữ số; họ và tên cần ít nhất 1 ký tự.');
      return;
    }
    const value = this.form.getRawValue();
    const payload: CompleteRegistrationPayload = this.isBrand()
      ? { phone: value.phone, brandName: value.brandName.trim(), representativeName: value.representativeName.trim(),
          representativePhone: value.representativePhone, representativeEmail: value.representativeEmail.trim() }
      : { phone: value.phone, firstName: value.firstName.trim(), lastName: value.lastName.trim(),
          ...(this.isFarmer() ? { gender: value.gender } : {}) };
    this.busy.set(true);
    this.error.set(null);
    this.api.completeRegistration(payload).pipe(
      switchMap((response) => response.result?.refreshRequired ? this.api.refresh() : of(null)),
      switchMap(() => this.auth.loadMe()),
      finalize(() => this.busy.set(false)),
    ).subscribe({
      next: (user) => {
        if (user) this.auth.navigateAfterLogin();
        else this.error.set('Thông tin đã được lưu nhưng chưa tải được tài khoản. Hãy kiểm tra lại hoặc đăng nhập lại.');
      },
      error: (err) => this.error.set(apiErrorMessage(err, 'Chưa hoàn tất đăng ký. Vui lòng kiểm tra thông tin và thử lại.')),
    });
  }

  logout(): void {
    this.auth.logout().subscribe();
  }
}
