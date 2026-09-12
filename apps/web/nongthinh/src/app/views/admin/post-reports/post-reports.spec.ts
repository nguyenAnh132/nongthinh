import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { PostApiService, PostReportView, PostView } from '../../../core/api/post-api.service';
import { ProfileApiService } from '../../../core/api/profile-api.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { AdminPostReports } from './post-reports';

describe('AdminPostReports', () => {
  let api: Record<string, ReturnType<typeof vi.fn>>;
  let toast: { success: ReturnType<typeof vi.fn>; error: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    api = {
      listPostReports: vi.fn(() => of({ result: reportPage([report()]) })),
      getPostReport: vi.fn(() => of({ result: report() })),
      getAdminPost: vi.fn(() => of({ result: post() })),
      listPostTypes: vi.fn(() => of({ result: [] })),
      listPostTopics: vi.fn(() => of({ result: [] })),
      startPostReportReview: vi.fn(() =>
        of({ result: { ...report(), status: 'UNDER_REVIEW' as const } }),
      ),
      resolvePostReport: vi.fn(() =>
        of({ result: { ...report(), status: 'RESOLVED' as const, resolutionNote: 'Đã xử lý' } }),
      ),
      rejectPostReport: vi.fn(() => of({ result: { ...report(), status: 'REJECTED' as const } })),
    };
    const profileApi = {
      getPublicFarmerProfileByUserId: vi.fn((userId: string) =>
        of({
          result: {
            id: `profile-${userId}`,
            firstName: userId === 'farmer-1' ? 'An' : 'Bình',
            lastName: 'Nguyễn',
            gender: 'OTHER',
            provinceId: null,
            communeId: null,
            avatarUrl: null,
          },
        }),
      ),
      getPublicBrandProfileByUserId: vi.fn(),
    };
    toast = { success: vi.fn(), error: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [AdminPostReports],
      providers: [
        { provide: PostApiService, useValue: api },
        { provide: ProfileApiService, useValue: profileApi },
        { provide: ToastService, useValue: toast },
      ],
    }).compileComponents();
  });

  it('loads and enriches pending reports, then changes the server-side status filter', () => {
    const fixture = TestBed.createComponent(AdminPostReports);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(api['listPostReports']).toHaveBeenCalledWith({ status: 'PENDING', page: 0, size: 20 });
    expect(api['getAdminPost']).toHaveBeenCalledWith('post-1');
    expect(component.reports()).toEqual([report()]);
    expect(component.authorFor(report()).name).toBe('Bình Nguyễn');
    expect(component.reporterFor(report()).name).toBe('An Nguyễn');

    component.selectStatus('UNDER_REVIEW');
    expect(api['listPostReports']).toHaveBeenLastCalledWith({
      status: 'UNDER_REVIEW',
      page: 0,
      size: 20,
    });
  });

  it('starts review and reloads the active queue', () => {
    const fixture = TestBed.createComponent(AdminPostReports);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.startReview(report());

    expect(api['startPostReportReview']).toHaveBeenCalledWith('report-1');
    expect(api['listPostReports']).toHaveBeenCalledTimes(2);
    expect(toast.success).toHaveBeenCalled();
  });

  it('submits the selected moderation action with the trimmed resolution note', () => {
    const fixture = TestBed.createComponent(AdminPostReports);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.openCompletion(report(), 'RESOLVE');
    component.moderationAction = 'DELETE';
    component.resolutionNote = '  Đã xử lý  ';

    component.submitCompletion();

    expect(api['resolvePostReport']).toHaveBeenCalledWith('report-1', {
      resolutionNote: 'Đã xử lý',
      moderationAction: 'DELETE',
    });
    expect(component.completionTarget()).toBeNull();
    expect(toast.success).toHaveBeenCalledWith('Đã giải quyết báo cáo và xóa bài viết.');
  });

  function report(): PostReportView {
    return {
      id: 'report-1',
      postId: 'post-1',
      reporterId: 'farmer-1',
      reason: 'MISINFORMATION',
      reasonDetail: 'Thông tin chưa chính xác',
      status: 'PENDING',
      resolvedBy: null,
      resolvedAt: null,
      resolutionNote: null,
      createdAt: '2026-08-26T00:00:00Z',
      updatedAt: '2026-08-26T00:00:00Z',
    };
  }

  function post(): PostView {
    return {
      id: 'post-1',
      authorUserId: 'author-1',
      postTypeId: null,
      topicId: null,
      content: 'Nội dung bài viết được báo cáo',
      locationText: 'Đồng Tháp',
      visibility: 'PUBLIC',
      status: 'PUBLISHED',
      media: [],
      cropTypeIds: [],
      publishedAt: '2026-08-25T00:00:00Z',
      createdAt: '2026-08-25T00:00:00Z',
      updatedAt: '2026-08-25T00:00:00Z',
      deletedAt: null,
    };
  }

  function reportPage(items: PostReportView[]) {
    return {
      items,
      page: 0,
      size: 20,
      totalElements: items.length,
      totalPages: items.length ? 1 : 0,
      hasNext: false,
    };
  }
});
