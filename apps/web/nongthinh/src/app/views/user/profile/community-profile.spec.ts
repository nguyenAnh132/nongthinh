import { Component, input, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FollowApiService } from '../../../core/api/follow-api.service';
import { ProfileApiService } from '../../../core/api/profile-api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { LocationService } from '../../../core/service/location.service';
import { Community } from '../community/community';
import { CommunityProfile } from './community-profile';

@Component({
  selector: 'app-community',
  standalone: true,
  template: '<div class="community-stub"></div>',
})
class CommunityStub {
  readonly embedded = input(false);
  readonly authorUserId = input<string | null>(null);
}

describe('CommunityProfile', () => {
  const userId = '10000000-0000-0000-0000-000000000001';

  function configure(profileApi: object) {
    TestBed.configureTestingModule({
      imports: [CommunityProfile],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ userId })) } },
        { provide: ProfileApiService, useValue: profileApi },
        { provide: LocationService, useValue: {
          getProvinces: () => of([{ id: '01', code: 'HN', name: 'Hà Nội' }]),
          getCommunesByProvince: () => of([{ id: 'commune-1', provinceId: '01', code: '001', name: 'Ba Đình' }]),
        } },
        { provide: AuthService, useValue: { currentUser: signal({ userId: 'viewer' }) } },
        { provide: FollowApiService, useValue: {
          revision: signal(0), following: signal({ [userId]: true }), pending: signal({}),
          profile: () => of({ result: {
            userId, displayName: 'Hồ sơ', role: 'FARMER', avatarUrl: null,
            followerCount: 0, followingCount: 0, following: false,
          } }),
          list: () => of({ result: { items: [], page: 0, size: 20, hasNext: false } }),
        } },
      ],
    });
    TestBed.overrideComponent(CommunityProfile, {
      remove: { imports: [Community] },
      add: { imports: [CommunityStub] },
    });
  }

  it('shows public farmer information and filters the embedded community by author', async () => {
    configure({
      getPublicFarmerProfileByUserId: () => of({ result: {
        id: 'farmer-1', firstName: 'Nguyễn', lastName: 'Văn An', gender: 'MALE',
        provinceId: '01', communeId: 'commune-1', avatarUrl: null,
      } }),
      getPublicBrandProfileByUserId: vi.fn(),
    });
    const fixture = TestBed.createComponent(CommunityProfile);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Nguyễn Văn An');
    expect(fixture.nativeElement.textContent).toContain('Nông dân');
    expect(fixture.nativeElement.textContent).toContain('Ba Đình, Hà Nội');
    expect(fixture.nativeElement.querySelector('.verified-cover-badge')).toBeNull();
    expect(fixture.nativeElement.querySelector('.back-link img').getAttribute('src'))
      .toBe('/icons/business/left.png');
    expect(fixture.nativeElement.querySelector('app-follow-button button').textContent.trim())
      .toBe('Đang theo dõi');
    const community = fixture.debugElement.query(By.directive(CommunityStub)).componentInstance as CommunityStub;
    expect(community.embedded()).toBe(true);
    expect(community.authorUserId()).toBe(userId);
  });

  it('uses the distinct brand presentation and displays verified status', async () => {
    configure({
      getPublicFarmerProfileByUserId: () => throwError(() => new Error('not farmer')),
      getPublicBrandProfileByUserId: () => of({ result: {
        id: 'brand-1', brandName: 'Nông sản Xanh', description: 'Nông sản sạch từ nông trại.',
        officeProvinceId: '01', officeCommuneId: 'commune-1', phone: '0901234567',
        logoUrl: null, bannerUrl: 'https://cdn.test/banner.jpg', websiteUrl: 'nongsanxanh.vn',
        verified: true,
      } }),
    });
    const fixture = TestBed.createComponent(CommunityProfile);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.public-profile--brand')).not.toBeNull();
    expect(fixture.nativeElement.textContent).toContain('Nông sản Xanh');
    expect(fixture.nativeElement.textContent).toContain('Đã xác minh');
    expect(fixture.nativeElement.textContent).not.toContain('Cập nhật từ thương hiệu');
    expect(fixture.nativeElement.textContent).toContain('Nông sản sạch từ nông trại.');
    expect(fixture.nativeElement.textContent).toContain('Văn phòng');
    expect(fixture.nativeElement.textContent).toContain('Liên hệ');
    expect(fixture.nativeElement.textContent).not.toContain('Đại diện');
    expect((fixture.nativeElement.querySelector('a[href="tel:0901234567"]') as HTMLAnchorElement).textContent).toContain('0901234567');
    expect((fixture.nativeElement.querySelector('.identity .user-avatar') as HTMLElement).style.width).toBe('148px');
    expect(fixture.nativeElement.querySelector('.follow-counts')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.community-stub')).not.toBeNull();
    expect((fixture.nativeElement.querySelector('a[target="_blank"]') as HTMLAnchorElement).href).toBe('https://nongsanxanh.vn/');
  });
});
