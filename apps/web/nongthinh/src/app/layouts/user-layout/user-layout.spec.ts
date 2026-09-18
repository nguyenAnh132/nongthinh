import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { UserLayout } from './user-layout';

describe('UserLayout community access', () => {
  for (const role of ['ROLE_BRAND', 'ROLE_BRAND_PENDING']) {
    it('hides features and stops realtime for a rejected ' + role, async () => {
      await TestBed.configureTestingModule({
        imports: [UserLayout],
        providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting(), {
          provide: AuthService, useValue: {
            currentUser: signal({ userId: 'brand-1', email: 'brand@example.com', role,
              profile: { displayName: 'Brand', status: 'REJECTED' } }), logout: vi.fn(),
          },
        }],
      }).compileComponents();
      const fixture = TestBed.createComponent(UserLayout);
      fixture.detectChanges();
      expect(fixture.componentInstance.canUseCommunity()).toBe(false);
      expect(fixture.componentInstance.canUseDiagnosis()).toBe(false);
      expect(fixture.nativeElement.querySelector('.community-search')).toBeNull();
      expect(fixture.nativeElement.querySelector('app-notification-popover')).toBeNull();
      expect(fixture.nativeElement.querySelector('a[href="/app/products"]')).toBeNull();
      expect(fixture.nativeElement.querySelector('a[href="/app/operations"]')).toBeNull();
      expect(fixture.nativeElement.querySelector('a[href="/app/profile"]')).not.toBeNull();
      TestBed.inject(HttpTestingController).expectNone('/api/v1/notification/events/config');
      fixture.destroy();
    });
  }

  it('shows Community, diagnosis and search for a Brand', async () => {
    const logout = vi.fn(() => of(undefined));
    await TestBed.configureTestingModule({
      imports: [UserLayout],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: AuthService,
          useValue: {
            currentUser: signal({
              userId: 'brand-1',
              email: 'brand@example.com',
              role: 'ROLE_BRAND',
              profile: { displayName: 'Thương hiệu xanh', avatarUrl: null, status: 'ACTIVE' },
            }).asReadonly(),
            logout,
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(UserLayout);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/v1/notification/events/config')
      .flush({ result: { notifications: false, postEngagement: false } });
    const links = Array.from(
      fixture.nativeElement.querySelectorAll('.user-nav a') as NodeListOf<HTMLAnchorElement>,
    ).map((link) => link.textContent?.trim());

    expect(fixture.componentInstance.canUseCommunity()).toBe(true);
    expect(fixture.nativeElement.querySelector('.community-search')).not.toBeNull();
    expect(links).toContain('Cộng đồng');
    expect(fixture.nativeElement.querySelector('a[href="/app/post-histories"]')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Lịch sử bài viết');
    expect(links).toContain('Quét bệnh cây trồng');
    expect(fixture.componentInstance.canUseDiagnosis()).toBe(true);
    expect(fixture.nativeElement.querySelector('a[href="/app/diagnosis"]')).not.toBeNull();
    expect(links).toContain('Vận hành');
    expect(fixture.nativeElement.querySelector('.user-nav a[href="/app/profile"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('.account-dropdown a[href="/app/profile"]')).not.toBeNull();
    expect(
      fixture.nativeElement
        .querySelector('a[href="/app/products"] .nav-icon')
        ?.getAttribute('style'),
    ).toContain('/icons/business/box-open.png');
    expect(
      fixture.nativeElement
        .querySelector('a[href="/app/operations"] .nav-icon')
        ?.getAttribute('style'),
    ).toContain('/icons/business/vanhanh.png');

    (fixture.nativeElement.querySelector('.account-dropdown button') as HTMLButtonElement).click();
    fixture.detectChanges();
    const dialog = fixture.nativeElement.querySelector('.confirm-dialog');
    expect(dialog).not.toBeNull();
    expect(dialog.textContent).toContain(
      'Bạn có chắc chắn muốn đăng xuất khỏi tài khoản hiện tại?',
    );
    expect(logout).not.toHaveBeenCalled();

    (dialog.querySelector('.confirm-dialog__submit') as HTMLButtonElement).click();
    expect(logout).toHaveBeenCalledOnce();
  });

  it('keeps the Farmer notification bell visible when the config endpoint fails', async () => {
    await TestBed.configureTestingModule({
      imports: [UserLayout],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting(), {
        provide: AuthService,
        useValue: { currentUser: signal({ userId: 'farmer-1', role: 'ROLE_FARMER',
          profile: { displayName: 'Nông dân', avatarUrl: null, status: 'ACTIVE' } }), logout: vi.fn() },
      }],
    }).compileComponents();
    const fixture = TestBed.createComponent(UserLayout);
    fixture.detectChanges();
    TestBed.inject(HttpTestingController).expectOne('/api/v1/notification/events/config')
      .flush({}, { status: 503, statusText: 'Unavailable' });
    fixture.detectChanges();
    const bell = fixture.nativeElement.querySelector('.notification-button') as HTMLButtonElement;
    expect(fixture.nativeElement.querySelector('.user-nav a[href="/app/profile"]')).not.toBeNull();
    expect(bell).not.toBeNull();
    bell.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Không thể kết nối dịch vụ thông báo');
    fixture.destroy();
    TestBed.inject(HttpTestingController).verify();
  });
});
