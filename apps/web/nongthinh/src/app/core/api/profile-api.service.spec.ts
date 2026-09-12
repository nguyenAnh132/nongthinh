import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProfileApiService, UpdateFarmerProfilePayload } from './profile-api.service';

describe('ProfileApiService', () => {
  let service: ProfileApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProfileApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads the current farmer from the farmer profile endpoint', () => {
    service.getMyFarmerProfile().subscribe();

    const request = http.expectOne('/api/v1/profile/farmer-profiles/me');
    expect(request.request.method).toBe('GET');
    request.flush({ result: farmerProfile() });
  });

  it('updates the current farmer using the backend farmer contract', () => {
    const payload: UpdateFarmerProfilePayload = {
      firstName: 'Nguyễn',
      lastName: 'Văn An',
      gender: 'MALE',
      phone: '0912345678',
      provinceId: '01',
      communeId: '00000000-0000-0000-0000-000000000001',
      addressDetail: 'Thôn 1',
    };

    service.updateMyFarmerProfile(payload).subscribe();

    const request = http.expectOne('/api/v1/profile/farmer-profiles/me');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(payload);
    request.flush({ result: farmerProfile() });
  });

  it('updates the current farmer avatar through the dedicated profile endpoint', () => {
    const avatarUrl = 'https://files.example.test/avatars/farmer.webp';

    service.updateMyFarmerAvatar(avatarUrl).subscribe();

    const request = http.expectOne('/api/v1/profile/farmer-profiles/me/avatar');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ url: avatarUrl });
    request.flush({ result: { ...farmerProfile(), avatarUrl } });
  });
});

function farmerProfile() {
  return {
    id: 'profile-1',
    userId: 'user-1',
    firstName: 'Nguyễn',
    lastName: 'Văn An',
    gender: 'MALE',
    phone: '0912345678',
    provinceId: '01',
    provinceName: 'Thành phố Hà Nội',
    communeId: '00000000-0000-0000-0000-000000000001',
    communeName: 'Phường Ba Đình',
    addressDetail: 'Thôn 1',
    avatarUrl: null,
    status: 'ACTIVE',
  };
}
