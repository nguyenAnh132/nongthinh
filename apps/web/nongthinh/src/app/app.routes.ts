import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { permissionGuard } from './core/guards/permission.guard';
import { activeBrandGuard } from './core/guards/active-brand.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./views/landing/landing.component').then((m) => m.LandingComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./views/auth/auth').then((m) => m.Auth),
  },
  {
    path: 'register',
    loadComponent: () => import('./views/auth/auth').then((m) => m.Auth),
  },
  {
    path: 'app',
    canActivate: [authGuard, roleGuard(['FARMER', 'BRAND'])],
    loadComponent: () => import('./layouts/user-layout/user-layout').then((m) => m.UserLayout),
    children: [
      { path: '', redirectTo: 'community', pathMatch: 'full' },
      {
        path: 'community/:postId',
        canActivate: [roleGuard(['FARMER', 'BRAND'])],
        loadComponent: () => import('./views/user/community/community').then((m) => m.Community),
      },
      {
        path: 'community',
        canActivate: [roleGuard(['FARMER', 'BRAND'])],
        loadComponent: () => import('./views/user/community/community').then((m) => m.Community),
      },
      {
        path: 'diagnosis',
        canActivate: [roleGuard(['FARMER'])],
        loadComponent: () =>
          import('./views/user/diagnosis/diagnosis').then((m) => m.FarmerDiagnosis),
      },
      {
        path: 'people/:userId',
        loadComponent: () =>
          import('./views/user/profile/community-profile').then((m) => m.CommunityProfile),
      },
      {
        path: 'profile',
        loadComponent: () => import('./views/user/profile/profile').then((m) => m.UserProfile),
      },
      {
        path: 'operations',
        canActivate: [roleGuard(['BRAND']), activeBrandGuard],
        loadComponent: () =>
          import('./views/user/brand-operations/brand-operations').then((m) => m.BrandOperations),
      },
    ],
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard(['ADMIN'])],
    loadComponent: () => import('./layouts/admin-layout/layout').then((m) => m.Layout),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () => import('./views/admin/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'catalog',
        loadComponent: () => import('./views/admin/catalog/catalog').then((m) => m.AdminCatalog),
      },
      {
        path: 'crop-types',
        loadComponent: () =>
          import('./views/admin/crop-types/crop-types').then((m) => m.AdminCropTypes),
      },
      {
        path: 'ai-models',
        loadComponent: () =>
          import('./views/admin/ai-models/ai-models').then((m) => m.AdminAiModels),
      },
      {
        path: 'users',
        canActivate: [
          permissionGuard(['admin:user:read', 'admin:user:write', 'admin:role:manage']),
        ],
        loadComponent: () => import('./views/admin/users/users').then((m) => m.Users),
      },
      {
        path: 'brands',
        canActivate: [permissionGuard(['brand:approve'])],
        loadComponent: () => import('./views/admin/brands/brands').then((m) => m.Brands),
      },
      {
        path: 'brands/:id',
        canActivate: [permissionGuard(['brand:approve'])],
        loadComponent: () => import('./views/admin/brands/brand-detail').then((m) => m.BrandDetail),
      },
      {
        path: 'content',
        canActivate: [permissionGuard(['content:moderate'])],
        loadComponent: () =>
          import('./views/admin/content-management/content-management').then(
            (m) => m.ContentManagement,
          ),
        children: [
          { path: '', redirectTo: 'articles', pathMatch: 'full' },
          {
            path: 'catalog',
            loadComponent: () =>
              import('./views/admin/post-catalog/post-catalog').then(
                (m) => m.AdminPostCatalog,
              ),
          },
          {
            path: 'articles',
            loadComponent: () => import('./views/admin/articles/articles').then((m) => m.Articles),
          },
          {
            path: 'reports',
            loadComponent: () =>
              import('./views/admin/post-reports/post-reports').then(
                (m) => m.AdminPostReports,
              ),
          },
        ],
      },
      {
        path: 'articles',
        redirectTo: 'content/articles',
        pathMatch: 'full',
      },
      {
        path: 'post-catalog',
        redirectTo: 'content/catalog',
        pathMatch: 'full',
      },
      {
        path: 'post-reports',
        redirectTo: 'content/reports',
        pathMatch: 'full',
      },
      {
        path: 'post-histories',
        redirectTo: 'content/articles',
        pathMatch: 'full',
      },
      {
        path: 'params',
        canActivate: [permissionGuard(['system:config:read', 'system:config:write'])],
        loadComponent: () => import('./views/admin/params/params').then((m) => m.Params),
      },
      {
        path: 'email-config',
        canActivate: [permissionGuard(['notification:email:manage'])],
        loadComponent: () =>
          import('./views/admin/email-config/email-config').then((m) => m.EmailConfig),
      },
      {
        path: 'email-config/:purposeCode',
        canActivate: [permissionGuard(['notification:email:manage'])],
        loadComponent: () =>
          import('./views/admin/email-config/email-purpose-detail').then(
            (m) => m.EmailPurposeDetail,
          ),
      },
      {
        path: 'email-config/:purposeCode/templates/new',
        canActivate: [permissionGuard(['notification:email:manage'])],
        loadComponent: () =>
          import('./views/admin/email-config/email-template-editor').then(
            (m) => m.EmailTemplateEditor,
          ),
      },
      {
        path: 'email-config/:purposeCode/templates/:templateId',
        canActivate: [permissionGuard(['notification:email:manage'])],
        loadComponent: () =>
          import('./views/admin/email-config/email-template-editor').then(
            (m) => m.EmailTemplateEditor,
          ),
      },
      {
        path: 'logs',
        canActivate: [permissionGuard(['incident:manage'])],
        loadComponent: () => import('./views/admin/logs/logs').then((m) => m.Logs),
      },
    ],
  },
];
