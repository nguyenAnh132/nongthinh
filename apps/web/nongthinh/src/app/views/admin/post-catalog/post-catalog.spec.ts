import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { PostApiService, PostTopicView, PostTypeView } from '../../../core/api/post-api.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { AdminPostCatalog } from './post-catalog';

describe('AdminPostCatalog', () => {
  let api: Record<string, ReturnType<typeof vi.fn>>;

  beforeEach(async () => {
    api = {
      listPostTypes: vi.fn(() => of({ result: [postType()] })),
      listPostTopics: vi.fn(() => of({ result: [postTopic()] })),
      createPostType: vi.fn(() => of({ result: postType() })),
      updatePostType: vi.fn(() => of({ result: postType() })),
      deletePostType: vi.fn(() => of({ result: null })),
      createPostTopic: vi.fn(() => of({ result: postTopic() })),
      updatePostTopic: vi.fn(() => of({ result: postTopic() })),
      deletePostTopic: vi.fn(() => of({ result: null })),
    };

    await TestBed.configureTestingModule({
      imports: [AdminPostCatalog],
      providers: [
        { provide: PostApiService, useValue: api },
        { provide: ToastService, useValue: { success: vi.fn(), error: vi.fn() } },
      ],
    }).compileComponents();
  });

  it('loads both catalogs for the dashboard', async () => {
    const fixture = TestBed.createComponent(AdminPostCatalog);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(api['listPostTypes']).toHaveBeenCalledTimes(1);
    expect(api['listPostTopics']).toHaveBeenCalledTimes(1);
    expect(fixture.componentInstance.postTypes).toEqual([postType()]);
    expect(fixture.componentInstance.postTopics).toEqual([postTopic()]);
    expect(fixture.nativeElement.querySelector('.crop-type-visual')).toBeNull();
  });

  it('keeps the tag icon for topic records', () => {
    const fixture = TestBed.createComponent(AdminPostCatalog);
    fixture.componentInstance.activeKind = 'topic';
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.crop-type-visual')?.textContent.trim()).toBe('tag');
  });

  it('normalizes and creates an uppercase post type code', () => {
    const fixture = TestBed.createComponent(AdminPostCatalog);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.openCreate();
    component.catalogForm.patchValue({
      identifier: 'technical question',
      name: 'Hỏi đáp kỹ thuật',
      description: '',
      displayOrder: 10,
    });
    component.normalizeIdentifier();
    component.submit();

    expect(api['createPostType']).toHaveBeenCalledWith({
      code: 'TECHNICAL_QUESTION',
      name: 'Hỏi đáp kỹ thuật',
      description: null,
      displayOrder: 10,
    });
  });

  it('normalizes Vietnamese text into a topic slug', () => {
    const fixture = TestBed.createComponent(AdminPostCatalog);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.selectKind('topic');
    component.openCreate();
    component.catalogForm.patchValue({
      identifier: 'Sâu bệnh & Dịch hại',
      name: 'Sâu bệnh',
      displayOrder: 20,
    });
    component.normalizeIdentifier();
    component.submit();

    expect(api['createPostTopic']).toHaveBeenCalledWith({
      slug: 'sau-benh-dich-hai',
      name: 'Sâu bệnh',
      description: null,
      displayOrder: 20,
    });
  });

  function postType(): PostTypeView {
    return {
      id: 'type-1', code: 'QUESTION', name: 'Hỏi đáp', description: null,
      displayOrder: 10, active: true, createdAt: '2026-08-22T00:00:00Z', updatedAt: '2026-08-22T00:00:00Z',
    };
  }

  function postTopic(): PostTopicView {
    return {
      id: 'topic-1', slug: 'sau-benh', name: 'Sâu bệnh', description: null,
      displayOrder: 10, active: true, createdAt: '2026-08-22T00:00:00Z', updatedAt: '2026-08-22T00:00:00Z',
    };
  }
});
