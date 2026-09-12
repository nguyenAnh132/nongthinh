import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideAppInitializer,
  inject,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { vi_VN, provideNzI18n } from 'ng-zorro-antd/i18n';
import { registerLocaleData } from '@angular/common';
import vi from '@angular/common/locales/vi';

import { provideNzIcons } from 'ng-zorro-antd/icon';
import { DashboardOutline, UserOutline, FileTextOutline, SettingOutline, ProfileOutline, SearchOutline, BellOutline, DownOutline, LogoutOutline } from '@ant-design/icons-angular/icons';

import { AuthService } from './core/auth/auth.service';

registerLocaleData(vi);

const icons = [DashboardOutline, UserOutline, FileTextOutline, SettingOutline, ProfileOutline, SearchOutline, BellOutline, DownOutline, LogoutOutline];

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    provideAnimationsAsync(),
    provideNzI18n(vi_VN),
    provideNzIcons(icons),
    provideAppInitializer(() => inject(AuthService).initializeSession()),
  ]
};
