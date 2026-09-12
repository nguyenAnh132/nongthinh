import { CommonModule, isPlatformBrowser } from '@angular/common';
import {
  AfterViewInit,
  Component,
  DestroyRef,
  ElementRef,
  HostListener,
  PLATFORM_ID,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzBadgeModule } from 'ng-zorro-antd/badge';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzTooltipModule } from 'ng-zorro-antd/tooltip';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { filter } from 'rxjs';
import gsap from 'gsap';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    CommonModule,
    NzLayoutModule,
    NzMenuModule,
    NzBreadCrumbModule,
    NzAvatarModule,
    NzDropDownModule,
    NzBadgeModule,
    NzInputModule,
    NzButtonModule,
    NzTooltipModule,
    NzIconModule,
    ConfirmDialogComponent,
  ],
  templateUrl: './layout.html',
  styleUrl: './layout.scss',
})
export class Layout implements AfterViewInit {
  private static readonly DEFAULT_SIDEBAR_WIDTH = 256;
  private static readonly COLLAPSED_SIDEBAR_WIDTH = 76;
  private static readonly COLLAPSE_THRESHOLD = 200;
  private static readonly MAX_SIDEBAR_WIDTH = 420;
  private static readonly SIDEBAR_STORAGE_KEY = 'nongthinh.admin.sidebar';

  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly router = inject(Router);
  @ViewChild('adminNavInner') private adminNavInner?: ElementRef<HTMLElement>;
  private resizeStartX = 0;
  private resizeStartWidth = Layout.DEFAULT_SIDEBAR_WIDTH;
  private resizePointerId: number | null = null;
  private resizeHandle: HTMLElement | null = null;
  private activeIndicatorFrame: number | null = null;

  readonly isCollapsed = signal(false);
  readonly isResizing = signal(false);
  readonly sidebarWidth = signal(Layout.DEFAULT_SIDEBAR_WIDTH);
  readonly activeIndicatorTop = signal(0);
  readonly activeIndicatorHeight = signal(0);
  readonly activeIndicatorVisible = signal(false);
  readonly activeIndicatorReady = signal(false);
  readonly logoutDialogOpen = signal(false);
  readonly loggingOut = signal(false);
  readonly renderedSidebarWidth = computed(() =>
    this.isCollapsed() ? Layout.COLLAPSED_SIDEBAR_WIDTH : this.sidebarWidth(),
  );
  readonly currentUser = this.authService.currentUser;

  constructor() {
    this.restoreSidebarState();

    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.scheduleActiveIndicatorSync());

    this.destroyRef.onDestroy(() => {
      if (this.activeIndicatorFrame !== null && isPlatformBrowser(this.platformId)) {
        cancelAnimationFrame(this.activeIndicatorFrame);
      }
    });
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }

  hasAnyPermission(permissions: string[]): boolean {
    return this.authService.hasAnyPermission(permissions);
  }

  adminGroupLabel(): string {
    const group = this.currentUser()?.adminGroup;
    if (group === 'SUPER_ADMIN') return 'Super Admin';
    if (group === 'OPERATION') return 'Vận hành';
    return 'Quản trị viên';
  }

  ngAfterViewInit() {
    this.scheduleActiveIndicatorSync();

    setTimeout(() => {
      const tl = gsap.timeline({
        defaults: { ease: 'power3.out' },
        onComplete: () => {
          // A transformed ancestor becomes the containing block of fixed overlays.
          // Remove GSAP's inline transform so dialogs stay anchored to the viewport.
          gsap.set('.admin-page-content', { clearProps: 'transform' });
        },
      });
      tl.from('.admin-sidebar', { autoAlpha: 0, duration: 0.4 })
        .from('.admin-sidebar-logo-row', { y: -10, autoAlpha: 0, duration: 0.4 }, '-=0.2')
        .from(
          '.admin-nav-link',
          { x: -20, autoAlpha: 0, duration: 0.5, stagger: 0.08, ease: 'back.out(1.2)' },
          '-=0.2',
        )
        .from('.admin-topbar', { y: -10, autoAlpha: 0, duration: 0.5 }, '-=0.4')
        .from('.admin-page-content', { y: 20, autoAlpha: 0, duration: 0.5 }, '-=0.3');
    }, 50);
  }

  toggleCollapsed(): void {
    this.isCollapsed.set(!this.isCollapsed());
    this.persistSidebarState();
    this.scheduleActiveIndicatorSync();
  }

  startSidebarResize(event: PointerEvent): void {
    if (event.button !== 0) return;

    event.preventDefault();
    this.resizeStartX = event.clientX;
    this.resizeStartWidth = this.isCollapsed() ? Layout.COLLAPSE_THRESHOLD : this.sidebarWidth();
    this.resizePointerId = event.pointerId;
    this.resizeHandle = event.currentTarget as HTMLElement;
    this.resizeHandle.setPointerCapture?.(event.pointerId);
    this.isResizing.set(true);
  }

  @HostListener('document:pointermove', ['$event'])
  onSidebarResize(event: PointerEvent): void {
    if (!this.isResizing() || event.pointerId !== this.resizePointerId) return;

    const nextWidth = this.resizeStartWidth + event.clientX - this.resizeStartX;
    if (nextWidth <= Layout.COLLAPSE_THRESHOLD) {
      this.isCollapsed.set(true);
      this.scheduleActiveIndicatorSync();
      return;
    }

    this.isCollapsed.set(false);
    this.sidebarWidth.set(Math.min(Math.round(nextWidth), Layout.MAX_SIDEBAR_WIDTH));
    this.scheduleActiveIndicatorSync();
  }

  @HostListener('document:pointerup', ['$event'])
  @HostListener('document:pointercancel', ['$event'])
  stopSidebarResize(event: PointerEvent): void {
    if (!this.isResizing() || event.pointerId !== this.resizePointerId) return;

    if (this.resizeHandle?.hasPointerCapture?.(event.pointerId)) {
      this.resizeHandle.releasePointerCapture(event.pointerId);
    }
    this.resizePointerId = null;
    this.resizeHandle = null;
    this.isResizing.set(false);
    this.persistSidebarState();
    this.scheduleActiveIndicatorSync();
  }

  resizeSidebarWithKeyboard(event: KeyboardEvent): void {
    const step = event.shiftKey ? 32 : 16;

    switch (event.key) {
      case 'ArrowLeft':
        if (!this.isCollapsed()) {
          this.applyKeyboardWidth(this.sidebarWidth() - step);
        }
        break;
      case 'ArrowRight':
        if (this.isCollapsed()) {
          this.isCollapsed.set(false);
        } else {
          this.applyKeyboardWidth(this.sidebarWidth() + step);
        }
        break;
      case 'Home':
        this.isCollapsed.set(true);
        break;
      case 'End':
        this.isCollapsed.set(false);
        this.sidebarWidth.set(Layout.MAX_SIDEBAR_WIDTH);
        break;
      case 'Enter':
      case ' ':
        this.toggleCollapsed();
        event.preventDefault();
        return;
      default:
        return;
    }

    event.preventDefault();
    this.persistSidebarState();
    this.scheduleActiveIndicatorSync();
  }

  sidebarValueText(): string {
    return this.isCollapsed() ? 'Đã thu gọn' : `${this.sidebarWidth()} pixel`;
  }

  userInitial(): string {
    const email = this.currentUser()?.email ?? 'A';
    return email.charAt(0).toUpperCase();
  }

  logout(): void {
    this.logoutDialogOpen.set(true);
  }

  cancelLogout(): void {
    if (!this.loggingOut()) this.logoutDialogOpen.set(false);
  }

  confirmLogout(): void {
    if (this.loggingOut()) return;
    this.loggingOut.set(true);
    this.authService.logout().subscribe(() => {
      this.loggingOut.set(false);
      this.logoutDialogOpen.set(false);
    });
  }

  private applyKeyboardWidth(nextWidth: number): void {
    if (nextWidth <= Layout.COLLAPSE_THRESHOLD) {
      this.isCollapsed.set(true);
      return;
    }

    this.isCollapsed.set(false);
    this.sidebarWidth.set(Math.min(Math.round(nextWidth), Layout.MAX_SIDEBAR_WIDTH));
  }

  private scheduleActiveIndicatorSync(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    if (this.activeIndicatorFrame !== null) {
      cancelAnimationFrame(this.activeIndicatorFrame);
    }

    this.activeIndicatorFrame = requestAnimationFrame(() => {
      this.activeIndicatorFrame = null;
      const activeLink =
        this.adminNavInner?.nativeElement.querySelector<HTMLElement>('.admin-nav-link.active');
      if (!activeLink) return;

      this.activeIndicatorTop.set(activeLink.offsetTop);
      this.activeIndicatorHeight.set(activeLink.offsetHeight);
      this.activeIndicatorVisible.set(true);

      if (!this.activeIndicatorReady()) {
        requestAnimationFrame(() => this.activeIndicatorReady.set(true));
      }
    });
  }

  private restoreSidebarState(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    try {
      const rawState = localStorage.getItem(Layout.SIDEBAR_STORAGE_KEY);
      if (!rawState) return;

      const state = JSON.parse(rawState) as {
        width?: unknown;
        collapsed?: unknown;
      };
      if (typeof state.width === 'number' && Number.isFinite(state.width)) {
        this.sidebarWidth.set(
          Math.min(
            Math.max(Math.round(state.width), Layout.COLLAPSE_THRESHOLD + 1),
            Layout.MAX_SIDEBAR_WIDTH,
          ),
        );
      }
      this.isCollapsed.set(state.collapsed === true);
    } catch {
      localStorage.removeItem(Layout.SIDEBAR_STORAGE_KEY);
    }
  }

  private persistSidebarState(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    try {
      localStorage.setItem(
        Layout.SIDEBAR_STORAGE_KEY,
        JSON.stringify({
          width: this.sidebarWidth(),
          collapsed: this.isCollapsed(),
        }),
      );
    } catch {
      // The layout remains usable when browser storage is unavailable.
    }
  }
}
