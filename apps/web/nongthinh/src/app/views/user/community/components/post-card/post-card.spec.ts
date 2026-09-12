import { TestBed } from '@angular/core/testing';
import { CommunityPost } from '../../models/community.models';
import { PostCard } from './post-card';

describe('PostCard reaction gesture', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [PostCard] }).compileComponents();
  });

  it('uses LIKE for a normal click and toggles it off on the next click', () => {
    const fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', communityPost(null));
    fixture.componentRef.setInput('interactionsEnabled', true);
    fixture.detectChanges();
    const reactions: Array<string | null> = [];
    fixture.componentInstance.reactionChanged.subscribe((reaction) => reactions.push(reaction));
    const trigger = fixture.nativeElement.querySelector('.reaction-trigger') as HTMLButtonElement;

    trigger.click();
    expect(reactions).toEqual(['LIKE']);

    fixture.componentRef.setInput('post', communityPost('LIKE'));
    fixture.detectChanges();
    trigger.click();
    expect(reactions).toEqual(['LIKE', null]);
  });

  it('opens the full picker only after a long press and suppresses the synthetic click', () => {
    vi.useFakeTimers();
    try {
      const fixture = TestBed.createComponent(PostCard);
      fixture.componentRef.setInput('post', communityPost(null));
      fixture.componentRef.setInput('interactionsEnabled', true);
      fixture.detectChanges();
      const reactions: Array<string | null> = [];
      fixture.componentInstance.reactionChanged.subscribe((reaction) => reactions.push(reaction));
      const trigger = fixture.nativeElement.querySelector('.reaction-trigger') as HTMLButtonElement;

      trigger.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true, button: 0 }));
      vi.advanceTimersByTime(449);
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('.reaction-picker')).toBeNull();

      vi.advanceTimersByTime(1);
      fixture.detectChanges();
      expect(fixture.nativeElement.querySelector('.reaction-picker')).not.toBeNull();
      expect(reactions).toEqual([]);

      vi.advanceTimersByTime(2000);
      trigger.dispatchEvent(new MouseEvent('pointerup', { bubbles: true, button: 0 }));
      trigger.click();
      expect(reactions).toEqual([]);

      const loveButton = Array.from(
        fixture.nativeElement.querySelectorAll(
          '.reaction-picker button',
        ) as NodeListOf<HTMLButtonElement>,
      ).find((button) => button.title === 'Yêu thích');
      loveButton?.click();
      expect(reactions).toEqual(['LOVE']);
    } finally {
      vi.useRealTimers();
    }
  });

  it('emits bookmark and share actions when interactions are enabled', () => {
    const fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', communityPost(null));
    fixture.componentRef.setInput('interactionsEnabled', true);
    fixture.detectChanges();
    const actions: string[] = [];
    fixture.componentInstance.bookmarkToggled.subscribe(() => actions.push('bookmark'));
    fixture.componentInstance.shareRequested.subscribe(() => actions.push('share'));

    (fixture.nativeElement.querySelector('.save-action') as HTMLButtonElement).click();
    (fixture.nativeElement.querySelector('.share-action') as HTMLButtonElement).click();

    expect(actions).toEqual(['bookmark', 'share']);
  });

  it('shows author location only when author metadata is available', () => {
    const fixture = TestBed.createComponent(PostCard);
    const postWithoutMetadata = communityPost(null);
    postWithoutMetadata.author.location = '';
    fixture.componentRef.setInput('post', postWithoutMetadata);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.author-meta')).toBeNull();

    fixture.componentRef.setInput('post', {
      ...postWithoutMetadata,
      author: { ...postWithoutMetadata.author, location: 'Đồng Tháp' },
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.author-meta')?.textContent.trim()).toBe(
      'Đồng Tháp',
    );
  });

  it('renders a verification mark after a verified brand author name', () => {
    const fixture = TestBed.createComponent(PostCard);
    const post = communityPost(null);
    post.author.verified = true;
    fixture.componentRef.setInput('post', post);
    fixture.detectChanges();

    const verified = fixture.nativeElement.querySelector('.verified');
    expect(verified).not.toBeNull();
    expect(verified.getAttribute('aria-label')).toBe('Thương hiệu đã xác minh');
    expect(verified.querySelector('svg')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.author-name-row').textContent).toContain(
      'Nguyễn Văn A',
    );
  });

  it('does not render a verification mark for a regular author', () => {
    const fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', communityPost(null));
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.verified')).toBeNull();
  });

  it('renders a text-only category tab at the top level of a categorized card', () => {
    const fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', communityPost(null));
    fixture.detectChanges();

    const tag = fixture.nativeElement.querySelector('.post-card > .category-tag') as HTMLElement;
    expect(tag.textContent?.trim()).toBe('Kinh nghiệm');
    expect(tag.querySelector('img, svg, .community-icon')).toBeNull();
    expect(fixture.nativeElement.querySelector('.post-card').classList).toContain(
      'post-card--categorized',
    );
  });

  it('does not render a category tab when the post has no type', () => {
    const fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', {
      ...communityPost(null),
      category: null,
      categoryLabel: undefined,
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.category-tag')).toBeNull();
    expect(fixture.nativeElement.querySelector('.post-card').classList).not.toContain(
      'post-card--categorized',
    );
  });

  it('places the topic immediately before the post content', () => {
    const fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', communityPost(null));
    fixture.detectChanges();

    const paragraph = fixture.nativeElement.querySelector('.post-content p') as HTMLElement;
    const topic = paragraph.querySelector(':scope > .topic-tag') as HTMLElement;
    const text = paragraph.textContent ?? '';
    expect(topic.textContent?.trim()).toBe('# Cây lúa');
    expect(text.indexOf('Cây lúa')).toBeLessThan(text.indexOf('Kinh nghiệm chăm lúa'));
  });

  it('uses a four-cell gallery and shows the remaining image count', () => {
    const fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', {
      ...communityPost(null),
      imageUrls: ['one.jpg', 'two.jpg', 'three.jpg', 'four.jpg', 'five.jpg', 'six.jpg'],
    });
    fixture.detectChanges();

    const grid = fixture.nativeElement.querySelector('.post-image-grid') as HTMLElement;
    expect(grid.getAttribute('data-count')).toBe('4');
    expect(grid.querySelectorAll('.post-image-cell')).toHaveLength(4);
    expect(grid.querySelector('.post-image-more')?.textContent?.trim()).toBe('+2');
  });

  it('opens the image viewer, zooms, and browses every post image', async () => {
    const fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', {
      ...communityPost(null),
      imageUrls: ['one.jpg', 'two.jpg', 'three.jpg', 'four.jpg', 'five.jpg'],
    });
    fixture.detectChanges();

    (fixture.nativeElement.querySelector('.post-image-cell') as HTMLButtonElement).click();
    await fixture.whenStable();

    const lightbox = fixture.nativeElement.querySelector('.image-lightbox') as HTMLElement;
    expect(lightbox).not.toBeNull();
    expect(lightbox.querySelector('.image-lightbox__stage img')?.getAttribute('src')).toBe(
      'one.jpg',
    );

    (lightbox.querySelector('[aria-label="Phóng to ảnh"]') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(fixture.componentInstance.imageZoom).toBe(1.25);

    (lightbox.querySelector('[aria-label="Xem ảnh trước"]') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(
      fixture.nativeElement.querySelector('.image-lightbox__stage img')?.getAttribute('src'),
    ).toBe('five.jpg');
    expect(fixture.componentInstance.imageZoom).toBe(1);

    (fixture.nativeElement.querySelector('[aria-label="Đóng ảnh"]') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.image-lightbox')).toBeNull();
  });

  it('collects a reason and emits a report only for another author post', () => {
    const fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', communityPost(null));
    fixture.componentRef.setInput('currentUserId', 'viewer-1');
    fixture.componentRef.setInput('interactionsEnabled', true);
    fixture.detectChanges();
    const reports: unknown[] = [];
    fixture.componentInstance.reportRequested.subscribe((event) => reports.push(event));

    const postMenu = fixture.nativeElement.querySelector('.post-menu');
    expect(postMenu).not.toBeNull();
    expect(postMenu.querySelector('.post-menu__panel')).toBeNull();
    (postMenu.querySelector('.post-menu-trigger') as HTMLButtonElement).click();
    fixture.detectChanges();

    const reportButton = postMenu.querySelector('.report-post-button') as HTMLButtonElement;
    expect(reportButton.textContent).toContain('Báo cáo');
    expect(reportButton.querySelector('img')?.getAttribute('src')).toBe(
      '/icons/community/hexagon-exclamation.png',
    );
    expect(fixture.nativeElement.textContent).not.toContain('Ẩn bài viết');

    reportButton.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.report-dialog')).not.toBeNull();

    fixture.componentInstance.reportReason = 'MISINFORMATION';
    fixture.componentInstance.reportDetail = 'Thông tin có thể gây hại';
    fixture.componentInstance.submitReport();

    expect(reports).toEqual([
      { reason: 'MISINFORMATION', reasonDetail: 'Thông tin có thể gây hại' },
    ]);
  });

  it('shows a confirmation dialog and emits delete only for the post owner', () => {
    const fixture = TestBed.createComponent(PostCard);
    fixture.componentRef.setInput('post', communityPost(null));
    fixture.componentRef.setInput('currentUserId', 'user-1');
    fixture.componentRef.setInput('interactionsEnabled', true);
    fixture.detectChanges();
    const deletions: string[] = [];
    fixture.componentInstance.deleteRequested.subscribe(() => deletions.push('post-1'));

    const postMenu = fixture.nativeElement.querySelector('.post-menu');
    expect(postMenu.querySelector('.post-menu__panel')).toBeNull();
    (postMenu.querySelector('.post-menu-trigger') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(postMenu.querySelector('.report-post-button')).toBeNull();
    const deleteButton = postMenu.querySelector('.delete-post-button') as HTMLButtonElement;
    expect(deleteButton).not.toBeNull();
    expect(deleteButton.querySelector('img')?.getAttribute('src')).toBe(
      '/icons/community/trash.png',
    );

    deleteButton.click();
    fixture.detectChanges();
    const dialog = fixture.nativeElement.querySelector('.confirm-dialog');
    expect(dialog).not.toBeNull();
    expect(dialog.getAttribute('role')).toBe('alertdialog');
    expect(dialog.textContent).toContain('Bài viết sẽ bị xóa. Bạn sẽ không thể hoàn tác.');
    expect(deletions).toEqual([]);

    (dialog.querySelector('.confirm-dialog__submit') as HTMLButtonElement).click();
    expect(deletions).toEqual(['post-1']);
  });

  function communityPost(currentReaction: CommunityPost['currentReaction']): CommunityPost {
    const reactionCounts = { LIKE: 0, LOVE: 0, HAHA: 0, WOW: 0, SAD: 0, ANGRY: 0 };
    if (currentReaction) reactionCounts[currentReaction] = 1;
    return {
      id: 'post-1',
      author: {
        id: 'user-1',
        name: 'Nguyễn Văn A',
        avatarUrl: null,
        location: 'Đồng Tháp',
      },
      content: 'Kinh nghiệm chăm lúa',
      category: 'EXPERIENCE',
      topic: 'Cây lúa',
      createdAt: 'Vừa xong',
      imageUrls: [],
      reactionCounts,
      reactionTotal: currentReaction ? 1 : 0,
      currentReaction,
      reactionPending: false,
      reactionError: '',
      reactionMutationId: 0,
      reactionVersion: 0,
      commentVersion: 0,
      shares: 0,
      sharePending: false,
      shareError: '',
      shareMutationId: 0,
      reported: false,
      reportPending: false,
      reportError: '',
      deletePending: false,
      deleteError: '',
      saved: false,
      bookmarkPending: false,
      bookmarkError: '',
      bookmarkMutationId: 0,
      comments: [],
      commentRootTotal: 0,
      commentsPage: 0,
      commentsHasNext: false,
      commentsLoaded: false,
      commentsLoading: false,
      commentsError: '',
      commentMutationPending: false,
      commentMutationError: '',
      commentCreateVersion: 0,
      commentEditVersion: 0,
    };
  }
});
