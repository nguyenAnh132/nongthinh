import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { PostApiService, PostStatisticsView, PostView } from '../../../core/api/post-api.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { Articles } from './articles';

const AUTHOR_ID = '83b339ee-23b8-47bd-81d6-d37f81529789';

describe('Articles management', () => {
  let postApi: Record<string, ReturnType<typeof vi.fn>>;

  beforeEach(async () => {
    postApi = {
      listAdminPosts: vi.fn(() => of({ result: page() })),
      getPostStatistics: vi.fn(() => of({ result: statistics() })),
      getAdminPost: vi.fn(() => of({ result: post() })),
    };
    await TestBed.configureTestingModule({
      imports: [Articles],
      providers: [
        { provide: PostApiService, useValue: postApi },
        {
          provide: ToastService,
          useValue: { error: vi.fn(), warning: vi.fn(), info: vi.fn(), success: vi.fn() },
        },
      ],
    }).compileComponents();
  });

  it('loads real administrator posts and summary statistics', () => {
    const fixture = TestBed.createComponent(Articles);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(component.posts()).toHaveLength(1);
    expect(component.statistics()?.totalPosts).toBe(12);
    expect(postApi['listAdminPosts']).toHaveBeenCalledWith({
      authorUserId: null,
      status: null,
      keyword: null,
      page: 0,
      size: 12,
    });
  });

  it('filters posts directly by author UUID without loading an email directory', () => {
    const fixture = TestBed.createComponent(Articles);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.searchUserQuery = AUTHOR_ID;
    component.applyFilters();

    expect(postApi['listAdminPosts']).toHaveBeenLastCalledWith({
      authorUserId: AUTHOR_ID,
      status: null,
      keyword: null,
      page: 0,
      size: 12,
    });
  });

  it('rejects an email value instead of sending an invalid author filter', () => {
    const fixture = TestBed.createComponent(Articles);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    postApi['listAdminPosts'].mockClear();

    component.searchUserQuery = 'farmer@example.com';
    component.applyFilters();

    expect(component.filterError).toBe('UUID người đăng không hợp lệ.');
    expect(postApi['listAdminPosts']).not.toHaveBeenCalled();
  });

  it('opens complete post content and media in detail', () => {
    const fixture = TestBed.createComponent(Articles);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.viewDetail(post());
    fixture.detectChanges();

    expect(postApi['getAdminPost']).toHaveBeenCalledWith('post-1');
    expect(component.detail()?.content).toBe('Kinh nghiệm chăm lúa');
    expect(component.detail()?.media).toHaveLength(1);
    expect(fixture.nativeElement.textContent).toContain('Hình ảnh và tệp đính kèm');
  });
});

function page() {
  return {
    items: [post()],
    page: 0,
    size: 12,
    totalElements: 1,
    totalPages: 1,
    hasNext: false,
  };
}

function statistics(): PostStatisticsView {
  return {
    totalPosts: 12,
    postsInRange: 4,
    from: '2026-08-21T17:00:00Z',
    to: '2026-08-28T17:00:00Z',
    bucket: 'DAY',
    timeZone: 'Asia/Ho_Chi_Minh',
    statusCounts: { DRAFT: 1, PUBLISHED: 10, HIDDEN: 1 },
    timeline: [
      {
        bucketStart: '2026-08-27T17:00:00Z',
        bucketEnd: '2026-08-28T17:00:00Z',
        count: 4,
      },
    ],
  };
}

function post(): PostView {
  return {
    id: 'post-1',
    authorUserId: AUTHOR_ID,
    postTypeId: 'type-1',
    topicId: null,
    content: 'Kinh nghiệm chăm lúa',
    locationText: 'Đồng Tháp',
    visibility: 'PUBLIC',
    status: 'PUBLISHED',
    media: [
      {
        id: 'media-1',
        fileId: 'file-1',
        mediaType: 'IMAGE',
        mediaUrl: 'https://files.example.test/rice.jpg',
        contentType: 'image/jpeg',
        width: 1200,
        height: 800,
        sizeBytes: 2048,
        displayOrder: 0,
        caption: 'Ruộng lúa',
        createdAt: '2026-08-28T00:00:00Z',
      },
    ],
    cropTypeIds: ['crop-1'],
    publishedAt: '2026-08-28T00:00:00Z',
    createdAt: '2026-08-28T00:00:00Z',
    updatedAt: '2026-08-28T00:00:00Z',
  };
}
