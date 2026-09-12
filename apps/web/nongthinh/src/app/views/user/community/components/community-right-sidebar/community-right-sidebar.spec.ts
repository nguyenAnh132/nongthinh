import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { PostApiService } from '../../../../../core/api/post-api.service';
import { CommunityRightSidebar } from './community-right-sidebar';

describe('CommunityRightSidebar', () => {
  it('renders ranked topics and post counts returned by the backend', async () => {
    await TestBed.configureTestingModule({
      imports: [CommunityRightSidebar],
      providers: [
        {
          provide: PostApiService,
          useValue: {
            listTrendingPostTopics: vi.fn(() =>
              of({
                result: [
                  {
                    rank: 1,
                    id: 'topic-1',
                    name: 'Sâu bệnh trên lúa',
                    slug: 'sau-benh-tren-lua',
                    postCount: 128,
                  },
                ],
              }),
            ),
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(CommunityRightSidebar);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.topic-rank').textContent.trim()).toBe('01');
    expect(fixture.nativeElement.querySelector('.topic-copy').textContent).toContain(
      '128 bài viết',
    );
    expect(fixture.nativeElement.querySelector('.topic-trend')).toBeNull();
  });

  it('shows an error state when trending topics cannot be loaded', async () => {
    await TestBed.configureTestingModule({
      imports: [CommunityRightSidebar],
      providers: [
        {
          provide: PostApiService,
          useValue: {
            listTrendingPostTopics: vi.fn(() => throwError(() => new Error('unavailable'))),
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(CommunityRightSidebar);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.topic-state-error').textContent).toContain(
      'Không thể tải chủ đề',
    );
  });
});
