import { provideLocationMocks } from '@angular/common/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ContentManagement } from './content-management';

describe('ContentManagement', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContentManagement],
      providers: [provideRouter([]), provideLocationMocks()],
    }).compileComponents();
  });

  it('shows all post administration areas as tabs', () => {
    const fixture = TestBed.createComponent(ContentManagement);
    fixture.detectChanges();

    const links = Array.from(
      fixture.nativeElement.querySelectorAll('.content-tabs a'),
    ) as HTMLAnchorElement[];

    expect(links.map((link) => link.textContent?.trim())).toEqual([
      'Danh mục bài viết',
      'Quản lý bài viết',
      'Báo cáo bài viết',
    ]);
    expect(links.every((link) => link.querySelector('.material-symbols-outlined') === null)).toBe(
      true,
    );
    expect(links.map((link) => link.getAttribute('href'))).toEqual([
      '/catalog',
      '/articles',
      '/reports',
    ]);
  });
});
