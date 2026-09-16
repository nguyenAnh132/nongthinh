import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import {
  AgriCatalogApiService,
  CropTypeView,
  DiseaseReviewHistoryListItemView,
  DiseaseView,
} from '../../../core/api/agri-catalog-api.service';
import { FileApiService } from '../../../core/api/file-api.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { DiseaseManagement } from './disease-management';

describe('DiseaseManagement', () => {
  const historyPage = {
    items: [] as DiseaseReviewHistoryListItemView[],
    page: 0,
    size: 5,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
  };
  let listDiseaseReviewHistories: ReturnType<typeof vi.fn>;
  let listDiseases: ReturnType<typeof vi.fn>;
  let listActiveCropTypes: ReturnType<typeof vi.fn>;
  let approveDisease: ReturnType<typeof vi.fn>;
  let restoreDisease: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    listDiseaseReviewHistories = vi.fn(() => of({ result: historyPage }));
    listDiseases = vi.fn(() => of({ result: [] }));
    listActiveCropTypes = vi.fn(() => of({ result: [cropType()] }));
    approveDisease = vi.fn(() =>
      of({ result: { ...disease(), reviewStatus: 'APPROVED' as const } }),
    );
    restoreDisease = vi.fn(() =>
      of({ result: { ...disease(), reviewStatus: 'APPROVED' as const } }),
    );
    await TestBed.configureTestingModule({
      imports: [DiseaseManagement],
      providers: [
        {
          provide: AgriCatalogApiService,
          useValue: {
            listDiseases,
            listActiveCropTypes,
            listDiseaseReviewHistories,
            approveDisease,
            restoreDisease,
          },
        },
        {
          provide: FileApiService,
          useValue: { upload: vi.fn() },
        },
        {
          provide: ToastService,
          useValue: {
            error: vi.fn(),
            success: vi.fn(),
            warning: vi.fn(),
          },
        },
      ],
    }).compileComponents();
  });

  it('loads the recent history independently only in admin mode', async () => {
    const adminFixture = TestBed.createComponent(DiseaseManagement);
    adminFixture.componentRef.setInput('adminMode', true);
    adminFixture.detectChanges();
    await adminFixture.whenStable();

    expect(listDiseaseReviewHistories).toHaveBeenCalledWith({ page: 0, size: 5 });
    adminFixture.destroy();

    listDiseaseReviewHistories.mockClear();
    const brandFixture = TestBed.createComponent(DiseaseManagement);
    brandFixture.componentRef.setInput('adminMode', false);
    brandFixture.detectChanges();
    await brandFixture.whenStable();

    expect(listDiseaseReviewHistories).not.toHaveBeenCalled();
    expect(brandFixture.nativeElement.querySelector('.disease-list-panel').classList).not.toContain(
      'panel',
    );
  });

  it('opens disease details from an accessible compact disease item', async () => {
    listDiseases.mockReturnValue(of({ result: [disease()] }));
    const fixture = TestBed.createComponent(DiseaseManagement);
    fixture.detectChanges();
    await fixture.whenStable();

    const card = fixture.nativeElement.querySelector('.disease-card') as HTMLElement;
    const trigger = card.querySelector('.action-menu-trigger') as HTMLButtonElement;
    expect(card.textContent).not.toContain('Tên bệnh:');
    expect(card.textContent).toContain('Magnaporthe oryzae');
    expect(card.textContent).toContain('Lúa (RICE)');
    expect(card.textContent).not.toContain('Bộ phận ảnh hưởng:');
    expect(card.textContent).not.toContain('Loại tác nhân:');
    expect(card.textContent).not.toContain('Mô tả:');
    expect(card.textContent).not.toContain('Cập nhật:');
    expect(trigger.textContent?.trim()).toBe('more_horiz');
    expect(trigger.getAttribute('aria-label')).toContain('Rice Blast');
    expect(trigger.getAttribute('aria-haspopup')).toBe('menu');
    expect(trigger.title).toBe('Mở danh sách hành động');
    expect(card.getAttribute('role')).toBe('button');
    expect(card.tabIndex).toBe(0);
    expect(card.querySelector('.record-action-button--edit')).toBeNull();
    expect(card.querySelector('.record-action-button--delete')).toBeNull();

    card.click();
    fixture.detectChanges();
    const detail = fixture.nativeElement.querySelector('.disease-detail-view') as HTMLElement;
    const editButton = detail.querySelector('.primary-button') as HTMLButtonElement;
    const deleteButton = detail.querySelector('.detail-delete-button') as HTMLButtonElement;
    expect(detail.textContent).toContain('Chi tiết bệnh cây trồng');
    expect(detail.textContent).toContain('Rice Blast');
    expect(editButton).not.toBeNull();
    expect(deleteButton).not.toBeNull();
    expect(editButton.disabled).toBe(false);
    expect(deleteButton.disabled).toBe(false);
    expect(editButton.title).toBe('Cập nhật bệnh');
  });

  it('offers update and delete actions inside editable disease details', async () => {
    listDiseases.mockReturnValue(of({
      result: [{ ...disease(), reviewStatus: 'DRAFT' as const }],
    }));
    const fixture = TestBed.createComponent(DiseaseManagement);
    fixture.detectChanges();
    await fixture.whenStable();

    const card = fixture.nativeElement.querySelector('.disease-card') as HTMLElement;
    card.click();
    fixture.detectChanges();
    const editButton = fixture.nativeElement.querySelector(
      '.disease-detail-view .primary-button',
    ) as HTMLButtonElement;
    const deleteButton = fixture.nativeElement.querySelector(
      '.detail-delete-button',
    ) as HTMLButtonElement;
    expect(editButton.disabled).toBe(false);
    expect(deleteButton.disabled).toBe(false);

    editButton.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.disease-editor')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.disease-editor--page')).not.toBeNull();

    fixture.componentInstance.cancelEdit();
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('.detail-delete-button') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Xoá “Rice Blast”?');
  });

  it('debounces keyword changes for 350 ms and resets the explorer to page zero', () => {
    vi.useFakeTimers();
    try {
      const fixture = TestBed.createComponent(DiseaseManagement);
      fixture.componentRef.setInput('adminMode', true);
      fixture.detectChanges();
      const component = fixture.componentInstance;
      component.historyExplorerOpen = true;
      listDiseaseReviewHistories.mockClear();

      component.onHistoryKeywordInput('rice');
      vi.advanceTimersByTime(349);
      expect(listDiseaseReviewHistories).not.toHaveBeenCalled();

      vi.advanceTimersByTime(1);
      expect(listDiseaseReviewHistories).toHaveBeenCalledWith(
        expect.objectContaining({ keyword: 'rice', page: 0, size: 20 }),
      );
    } finally {
      vi.useRealTimers();
    }
  });

  it('refreshes recent and open explorer histories after an admin review action', async () => {
    listDiseases.mockReturnValue(of({ result: [disease()] }));
    const fixture = TestBed.createComponent(DiseaseManagement);
    fixture.componentRef.setInput('adminMode', true);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;
    component.historyExplorerOpen = true;
    listDiseaseReviewHistories.mockClear();

    component.approve(disease());

    expect(listDiseaseReviewHistories).toHaveBeenCalledWith({ page: 0, size: 5 });
    expect(listDiseaseReviewHistories).toHaveBeenCalledWith(
      expect.objectContaining({ page: 0, size: 20 }),
    );
  });

  it('offers restore for a hidden disease and calls the restore endpoint', async () => {
    const hiddenDisease = { ...disease(), reviewStatus: 'HIDDEN' as const };
    listDiseases.mockReturnValue(of({ result: [hiddenDisease] }));
    const fixture = TestBed.createComponent(DiseaseManagement);
    fixture.componentRef.setInput('adminMode', true);
    fixture.detectChanges();
    await fixture.whenStable();

    fixture.componentInstance.actionMenuId = hiddenDisease.id;
    fixture.detectChanges();
    const restoreButton = Array.from(
      fixture.nativeElement.querySelectorAll('.action-dropdown button') as NodeListOf<HTMLButtonElement>,
    ).find((button) => button.textContent?.includes('Khôi phục bệnh'));

    expect(restoreButton).toBeTruthy();
    restoreButton?.click();
    expect(restoreDisease).toHaveBeenCalledWith(hiddenDisease.id);
  });

  function disease(): DiseaseView {
    return {
      id: 'disease-1',
      name: 'Rice Blast',
      slug: 'rice-blast',
      scientificName: 'Magnaporthe oryzae',
      cropTypeId: 'crop-type-1',
      affectedPart: 'Leaf',
      pathogenType: 'Fungus',
      shortDescription: 'Description',
      description: null,
      symptoms: null,
      causes: null,
      favorableConditions: null,
      preventionMethod: null,
      treatmentGuideline: null,
      thumbnailUrl: null,
      createdSource: 'BRAND',
      brandId: 'brand-1',
      reviewStatus: 'PENDING_REVIEW',
      rejectionReason: null,
      submittedAt: null,
      reviewedAt: null,
      reviewedBy: null,
      publishedAt: null,
      createdAt: '2026-07-26T00:00:00Z',
      createdBy: 'actor-1',
      updatedAt: '2026-07-26T00:00:00Z',
      updatedBy: 'actor-1',
    };
  }

  function cropType(): CropTypeView {
    return {
      id: 'crop-type-1',
      code: 'RICE',
      name: 'Lúa',
      description: null,
      active: true,
    };
  }
});
