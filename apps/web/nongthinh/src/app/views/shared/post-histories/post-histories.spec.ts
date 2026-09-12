import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { PostApiService, PostHistoryView } from '../../../core/api/post-api.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { PostHistories } from './post-histories';

const POST_ID = 'e62e1e62-d407-44d6-8adf-32b3cd558ca3';
const AUTHOR_ID = '1aff301f-54d3-4ba6-a36d-2b8f1b919aa3';
const ACTOR_ID = '9240b77f-6740-4686-b4a1-50ff1c2b52e5';

describe('PostHistories for administrators', () => {
  let api: Record<string, ReturnType<typeof vi.fn>>;

  beforeEach(async () => {
    api = apiMock();
    await TestBed.configureTestingModule({
      imports: [PostHistories],
      providers: [
        { provide: PostApiService, useValue: api },
        { provide: ToastService, useValue: { error: vi.fn() } },
      ],
    }).compileComponents();
  });

  it('loads the global audit log and applies administrator filters', () => {
    const fixture = TestBed.createComponent(PostHistories);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(api['listAdminPostHistories']).toHaveBeenCalledWith({
      postId: null,
      action: null,
      page: 0,
      size: 20,
      postAuthorUserId: null,
      actorUserId: null,
      actorType: null,
    });

    component.postIdFilter = POST_ID;
    component.postAuthorUserIdFilter = AUTHOR_ID;
    component.actorUserIdFilter = ACTOR_ID;
    component.actorTypeFilter = 'ADMIN';
    component.selectAction('HIDDEN');

    expect(api['listAdminPostHistories']).toHaveBeenLastCalledWith({
      postId: POST_ID,
      action: 'HIDDEN',
      page: 0,
      size: 20,
      postAuthorUserId: AUTHOR_ID,
      actorUserId: ACTOR_ID,
      actorType: 'ADMIN',
    });
  });

  it('loads administrator history detail from the privileged endpoint', () => {
    const fixture = TestBed.createComponent(PostHistories);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.viewDetail(history());

    expect(api['getAdminPostHistory']).toHaveBeenCalledWith('history-1');
    expect(component.detail()?.snapshot.content).toBe('Kinh nghiệm chăm lúa');
  });
});

function apiMock(): Record<string, ReturnType<typeof vi.fn>> {
  const page = {
    items: [history()],
    page: 0,
    size: 20,
    totalElements: 1,
    totalPages: 1,
    hasNext: false,
  };
  return {
    listAdminPostHistories: vi.fn(() => of({ result: page })),
    getAdminPostHistory: vi.fn(() => of({ result: history() })),
  };
}

function history(): PostHistoryView {
  return {
    id: 'history-1',
    postId: POST_ID,
    postAuthorUserId: AUTHOR_ID,
    actorUserId: ACTOR_ID,
    actorType: 'ADMIN',
    action: 'HIDDEN',
    previousStatus: 'PUBLISHED',
    newStatus: 'HIDDEN',
    previousVisibility: 'PUBLIC',
    newVisibility: 'PUBLIC',
    reasonCode: 'MISINFORMATION',
    reasonDetail: 'Nội dung chưa chính xác',
    reportId: null,
    snapshot: {
      content: 'Kinh nghiệm chăm lúa',
      postTypeId: 'type-1',
      postTopicId: null,
      cropTypeIds: [],
      locationText: 'Đồng Tháp',
      media: [],
      status: 'HIDDEN',
      visibility: 'PUBLIC',
    },
    createdAt: '2026-08-26T00:00:00Z',
  };
}
