import { Component, DestroyRef, computed, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of } from 'rxjs';
import {
  BrandProfilePublicResponse,
  FarmerProfilePublicResponse,
  ProfileApiService,
} from '../../../core/api/profile-api.service';
import { apiErrorMessage } from '../../../core/models/api-response';
import { LocationService } from '../../../core/service/location.service';
import { FollowButton } from '../../../shared/follow-button/follow-button';
import { FollowPanel } from '../../../shared/follow-panel/follow-panel';
import { UserAvatarComponent } from '../../../shared/user-avatar/user-avatar.component';
import { Community } from '../community/community';

type PublicProfile =
  | { kind: 'FARMER'; data: FarmerProfilePublicResponse }
  | { kind: 'BRAND'; data: BrandProfilePublicResponse };

@Component({
  selector: 'app-community-profile',
  standalone: true,
  imports: [Community, FollowButton, FollowPanel, RouterLink, UserAvatarComponent],
  templateUrl: './community-profile.html',
  styleUrl: './community-profile.scss',
})
export class CommunityProfile {
  private readonly profileApi = inject(ProfileApiService);
  private readonly locations = inject(LocationService);
  private readonly destroyRef = inject(DestroyRef);
  private requestVersion = 0;

  readonly userId = toSignal(
    inject(ActivatedRoute).paramMap.pipe(map((params) => params.get('userId') ?? '')),
    { initialValue: '' },
  );
  readonly profile = signal<PublicProfile | null>(null);
  readonly locationLabel = signal('');
  readonly profileLoading = signal(true);
  readonly profileError = signal('');

  readonly isBrand = computed(() => this.profile()?.kind === 'BRAND');
  readonly displayName = computed(() => {
    const profile = this.profile();
    if (!profile) return '';
    return profile.kind === 'BRAND'
      ? profile.data.brandName
      : [profile.data.firstName, profile.data.lastName].filter(Boolean).join(' ');
  });
  readonly avatarUrl = computed(() => {
    const profile = this.profile();
    if (!profile) return null;
    return profile.kind === 'BRAND' ? profile.data.logoUrl : profile.data.avatarUrl;
  });
  readonly bannerUrl = computed(() => {
    const profile = this.profile();
    return profile?.kind === 'BRAND' ? profile.data.bannerUrl : null;
  });
  readonly genderLabel = computed(() => {
    const profile = this.profile();
    if (profile?.kind !== 'FARMER') return '';
    const labels: Record<string, string> = { MALE: 'Nam', FEMALE: 'Nữ', OTHER: 'Khác' };
    return labels[profile.data.gender] ?? '';
  });
  readonly websiteHref = computed(() => {
    const profile = this.profile();
    if (profile?.kind !== 'BRAND' || !profile.data.websiteUrl?.trim()) return '';
    const website = profile.data.websiteUrl.trim();
    return /^https?:\/\//i.test(website) ? website : `https://${website}`;
  });

  constructor() {
    effect(() => {
      const userId = this.userId();
      if (userId) this.load(userId);
    });
  }

  retryProfile(): void {
    if (this.userId()) this.loadProfile(this.userId(), this.requestVersion);
  }

  private load(userId: string): void {
    const version = ++this.requestVersion;
    this.profile.set(null);
    this.locationLabel.set('');
    this.loadProfile(userId, version);
  }

  private loadProfile(userId: string, version: number): void {
    this.profileLoading.set(true);
    this.profileError.set('');
    this.profileApi.getPublicFarmerProfileByUserId(userId).pipe(
      map((response) => {
        if (!response.result) throw new Error('PROFILE_NOT_FOUND');
        return { kind: 'FARMER', data: response.result } as PublicProfile;
      }),
      catchError(() => this.profileApi.getPublicBrandProfileByUserId(userId).pipe(
        map((response) => {
          if (!response.result) throw new Error('PROFILE_NOT_FOUND');
          return { kind: 'BRAND', data: response.result } as PublicProfile;
        }),
      )),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (profile) => {
        if (version !== this.requestVersion) return;
        this.profile.set(profile);
        this.profileLoading.set(false);
        this.resolveLocation(profile, version);
      },
      error: (error) => {
        if (version !== this.requestVersion) return;
        this.profileLoading.set(false);
        this.profileError.set(apiErrorMessage(error, 'Không thể tải hồ sơ người dùng này.'));
      },
    });
  }

  private resolveLocation(profile: PublicProfile, version: number): void {
    const provinceId = profile.kind === 'BRAND'
      ? profile.data.officeProvinceId
      : profile.data.provinceId;
    const communeId = profile.kind === 'BRAND'
      ? profile.data.officeCommuneId
      : profile.data.communeId;
    if (!provinceId) return;
    forkJoin({
      provinces: this.locations.getProvinces().pipe(catchError(() => of([]))),
      communes: communeId
        ? this.locations.getCommunesByProvince(provinceId).pipe(catchError(() => of([])))
        : of([]),
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: ({ provinces, communes }) => {
        if (version !== this.requestVersion) return;
        const province = provinces.find((item) => item.id === provinceId)?.name;
        const commune = communes.find((item) => item.id === communeId)?.name;
        this.locationLabel.set([commune, province].filter(Boolean).join(', '));
      },
    });
  }
}
