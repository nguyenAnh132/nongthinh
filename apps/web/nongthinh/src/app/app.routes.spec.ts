import { routes } from './app.routes';

describe('post management routing', () => {
  it('groups post administration screens under one content management route', () => {
    const userArea = routes.find((route) => route.path === 'app');
    const adminArea = routes.find((route) => route.path === 'admin');
    const contentArea = adminArea?.children?.find((route) => route.path === 'content');

    expect(userArea?.children?.some((route) => route.path === 'post-histories')).toBe(false);
    expect(contentArea?.children?.map((route) => route.path)).toEqual([
      '',
      'catalog',
      'articles',
      'reports',
    ]);
    expect(contentArea?.children?.find((route) => route.path === '')).toMatchObject({
      redirectTo: 'articles',
      pathMatch: 'full',
    });
  });

  it('redirects the former post administration URLs to the grouped screen', () => {
    const adminArea = routes.find((route) => route.path === 'admin');

    expect(adminArea?.children?.find((route) => route.path === 'articles')).toMatchObject({
      redirectTo: 'content/articles',
      pathMatch: 'full',
    });
    expect(adminArea?.children?.find((route) => route.path === 'post-catalog')).toMatchObject({
      redirectTo: 'content/catalog',
      pathMatch: 'full',
    });
    expect(adminArea?.children?.find((route) => route.path === 'post-reports')).toMatchObject({
      redirectTo: 'content/reports',
      pathMatch: 'full',
    });
    expect(adminArea?.children?.find((route) => route.path === 'post-histories')).toMatchObject({
      redirectTo: 'content/articles',
      pathMatch: 'full',
    });
  });
});
