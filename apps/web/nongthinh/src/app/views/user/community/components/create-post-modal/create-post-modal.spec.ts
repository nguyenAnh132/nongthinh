import { provideUploadPolicyFixtures } from '../../../../../core/service/upload-policy.testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CropTypeView } from '../../../../../core/api/agri-catalog-api.service';
import { PostTopicView, PostTypeView } from '../../../../../core/api/post-api.service';
import { CreatePostModal } from './create-post-modal';
import { ToastService } from '../../../../../shared/toast/toast.service';

describe('CreatePostModal post options', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [CreatePostModal], providers: [provideUploadPolicyFixtures()] }).compileComponents();
  });

  it('reports an oversized image through a popup without persistent limit text', () => {
    const toast = vi.spyOn(TestBed.inject(ToastService), 'error').mockImplementation(() => {});
    const fixture = createFixture();
    const file = new File(['image'], 'large.png', { type: 'image/png' });
    Object.defineProperty(file, 'size', { value: 6 * 1024 * 1024 });
    const input = fixture.nativeElement.querySelector('#post-media') as HTMLInputElement;
    Object.defineProperty(input, 'files', { value: [file] });
    input.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(toast).toHaveBeenCalledExactlyOnceWith('Tệp không được vượt quá 5 MB.');
    expect(fixture.componentInstance.selectedMedia).toEqual([]);
    expect(fixture.nativeElement.textContent).not.toContain('tối đa 5 MB/tệp');
    expect(fixture.nativeElement.textContent).not.toContain('tối đa 50 MB/tệp');
    expect(fixture.nativeElement.querySelector('.media-error')).toBeNull();
    toast.mockRestore();
  });

  it('reports a rejected format through a popup', () => {
    const toast = vi.spyOn(TestBed.inject(ToastService), 'error').mockImplementation(() => {});
    const fixture = createFixture();
    const input = fixture.nativeElement.querySelector('#post-media') as HTMLInputElement;
    Object.defineProperty(input, 'files', { value: [new File(['file'], 'file.pdf', { type: 'application/pdf' })] });
    input.dispatchEvent(new Event('change'));
    expect(toast).toHaveBeenCalledOnce();
    expect(toast).toHaveBeenCalledWith(expect.stringContaining('Định dạng tệp không được chấp nhận'));
    expect(fixture.componentInstance.selectedMedia).toEqual([]);
    toast.mockRestore();
  });

  it('opens only the option selected by the user', () => {
    const fixture = createFixture();
    const fileInput = fixture.nativeElement.querySelector('#post-media') as HTMLInputElement;
    const openFilePicker = vi.spyOn(fileInput, 'click');

    expect(fixture.nativeElement.querySelector('.post-options__content')).toBeNull();

    optionButton(fixture, 'post-topic-panel').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('#post-topic-panel')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('#post-crop-panel')).toBeNull();

    (
      fixture.nativeElement.querySelector('[aria-label="Chọn ảnh hoặc video"]') as HTMLButtonElement
    ).click();
    fixture.detectChanges();
    expect(openFilePicker).toHaveBeenCalledOnce();
    expect(fixture.nativeElement.querySelector('.post-options__content')).toBeNull();

    optionButton(fixture, 'post-crop-panel').click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('#post-topic-panel')).toBeNull();
    expect(fixture.nativeElement.querySelector('#post-crop-panel')).not.toBeNull();
  });

  it('filters topics and crops without requiring Vietnamese diacritics', () => {
    const fixture = createFixture();

    optionButton(fixture, 'post-topic-panel').click();
    fixture.detectChanges();
    enterSearch(fixture, '#post-topic-search', 'lua');
    fixture.detectChanges();
    expect(optionLabels(fixture)).toContain('Kỹ thuật trồng lúa');
    expect(optionLabels(fixture)).not.toContain('Chăn nuôi');

    optionButton(fixture, 'post-crop-panel').click();
    fixture.detectChanges();
    enterSearch(fixture, '#post-crop-search', 'dau');
    fixture.detectChanges();
    expect(optionLabels(fixture)).toContain('Cây đậu');
    expect(optionLabels(fixture)).not.toContain('Cây lúa');
  });

  it('keeps location disabled until the feature is implemented', () => {
    const fixture = createFixture();
    const locationButton = fixture.nativeElement.querySelector(
      '[aria-label="Vị trí (sẽ được cập nhật sau)"]',
    ) as HTMLButtonElement;

    expect(locationButton.disabled).toBe(true);
  });

  it('allows a general post without a post type', () => {
    const fixture = createFixture();
    const component = fixture.componentInstance;
    const select = fixture.nativeElement.querySelector('#post-category') as HTMLSelectElement;
    const emitted = vi.spyOn(component.postCreated, 'emit');

    expect(component.form.controls.postTypeId.value).toBe('');
    expect(component.form.controls.postTypeId.valid).toBe(true);
    expect(select.value).toBe('');
    expect(select.selectedOptions[0]?.textContent?.trim()).toBe('Không chọn loại bài viết');

    component.form.controls.content.setValue('Nội dung bài viết chung');
    component.submit();

    expect(emitted).toHaveBeenCalledWith(
      expect.objectContaining({ postTypeId: null }),
    );
  });

  it('allows posting when there are no active post types', () => {
    const fixture = TestBed.createComponent(CreatePostModal);
    fixture.componentRef.setInput('postTypes', []);
    fixture.detectChanges();
    fixture.componentInstance.form.controls.content.setValue('Nội dung bài viết chung');
    fixture.detectChanges();

    const submit = fixture.nativeElement.querySelector(
      'button[type="submit"]',
    ) as HTMLButtonElement;
    expect(submit.disabled).toBe(false);
    expect(fixture.nativeElement.querySelector('.post-type-error')).toBeNull();
  });

  it('selects the matching post type when opened from a category action', () => {
    const fixture = TestBed.createComponent(CreatePostModal);
    fixture.componentRef.setInput('initialCategory', 'EXPERIENCE');
    fixture.componentRef.setInput('postTypes', [postType()]);
    fixture.componentRef.setInput('postTopics', postTopics());
    fixture.componentRef.setInput('cropTypes', cropTypes());
    fixture.detectChanges();

    expect(fixture.componentInstance.form.controls.postTypeId.value).toBe('type-1');
  });

  it('keeps and emits multiple selected images in display order', () => {
    const createObjectUrl = vi
      .spyOn(URL, 'createObjectURL')
      .mockImplementation((file) => `blob:${(file as File).name}`);
    const revokeObjectUrl = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
    try {
      const fixture = createFixture();
      const component = fixture.componentInstance;
      const input = fixture.nativeElement.querySelector('#post-media') as HTMLInputElement;
      const first = new File(['one'], 'one.jpg', { type: 'image/jpeg', lastModified: 1 });
      const second = new File(['two'], 'two.png', { type: 'image/png', lastModified: 2 });
      Object.defineProperty(input, 'files', {
        configurable: true,
        value: [first, second],
      });

      input.dispatchEvent(new Event('change'));
      fixture.detectChanges();

      expect(component.selectedMedia.map((item) => item.file.name)).toEqual([
        'one.jpg',
        'two.png',
      ]);
      expect(fixture.nativeElement.querySelectorAll('.media-preview-item')).toHaveLength(2);

      const emitted = vi.spyOn(component.postCreated, 'emit');
      component.form.controls.content.setValue('Bài viết có nhiều ảnh');
      component.submit();
      expect(emitted).toHaveBeenCalledWith(
        expect.objectContaining({
          mediaFiles: [
            { file: first, mediaType: 'IMAGE' },
            { file: second, mediaType: 'IMAGE' },
          ],
        }),
      );
    } finally {
      createObjectUrl.mockRestore();
      revokeObjectUrl.mockRestore();
    }
  });

  function createFixture(): ComponentFixture<CreatePostModal> {
    const fixture = TestBed.createComponent(CreatePostModal);
    fixture.componentRef.setInput('userName', 'Nông dân A');
    fixture.componentRef.setInput('postTypes', [postType()]);
    fixture.componentRef.setInput('postTopics', postTopics());
    fixture.componentRef.setInput('cropTypes', cropTypes());
    fixture.detectChanges();
    return fixture;
  }

  function optionButton(
    fixture: ComponentFixture<CreatePostModal>,
    panelId: string,
  ): HTMLButtonElement {
    return fixture.nativeElement.querySelector(`[aria-controls="${panelId}"]`) as HTMLButtonElement;
  }

  function enterSearch(
    fixture: ComponentFixture<CreatePostModal>,
    selector: string,
    value: string,
  ): void {
    const input = fixture.nativeElement.querySelector(selector) as HTMLInputElement;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function optionLabels(fixture: ComponentFixture<CreatePostModal>): string[] {
    return Array.from(
      fixture.nativeElement.querySelectorAll(
        '.selection-options label span',
      ) as NodeListOf<HTMLElement>,
    ).map((element) => element.textContent?.trim() ?? '');
  }

  function postType(): PostTypeView {
    return {
      id: 'type-1',
      code: 'EXPERIENCE',
      name: 'Chia sẻ kinh nghiệm',
      description: null,
      displayOrder: 0,
      active: true,
      createdAt: '2026-09-04T00:00:00Z',
      updatedAt: '2026-09-04T00:00:00Z',
    };
  }

  function postTopics(): PostTopicView[] {
    return [
      {
        id: 'topic-1',
        name: 'Kỹ thuật trồng lúa',
        slug: 'ky-thuat-trong-lua',
        description: null,
        displayOrder: 0,
        active: true,
        createdAt: '2026-09-04T00:00:00Z',
        updatedAt: '2026-09-04T00:00:00Z',
      },
      {
        id: 'topic-2',
        name: 'Chăn nuôi',
        slug: 'chan-nuoi',
        description: null,
        displayOrder: 1,
        active: true,
        createdAt: '2026-09-04T00:00:00Z',
        updatedAt: '2026-09-04T00:00:00Z',
      },
    ];
  }

  function cropTypes(): CropTypeView[] {
    return [
      { id: 'crop-1', code: 'RICE', name: 'Cây lúa', description: null, active: true },
      { id: 'crop-2', code: 'BEAN', name: 'Cây đậu', description: null, active: true },
    ];
  }
});
