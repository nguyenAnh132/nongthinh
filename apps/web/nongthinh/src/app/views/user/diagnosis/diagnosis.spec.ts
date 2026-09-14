import { TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import {
  AgriCatalogApiService,
  CropTypeView,
  DiseaseView,
} from '../../../core/api/agri-catalog-api.service';
import {
  DiagnosisApiService,
  DiagnosisHistoryDetail,
  DiagnosisResult,
  HistorySummary,
} from '../../../core/api/diagnosis-api.service';
import { FileApiService, FileView } from '../../../core/api/file-api.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { FarmerDiagnosis } from './diagnosis';

describe('FarmerDiagnosis', () => {
  let createDiagnosis: ReturnType<typeof vi.fn>;
  let getFileContent: ReturnType<typeof vi.fn>;
  let getHistory: ReturnType<typeof vi.fn>;
  let getPublishedDisease: ReturnType<typeof vi.fn>;
  let listActiveCropTypes: ReturnType<typeof vi.fn>;
  let listHistory: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    createDiagnosis = vi.fn(() => of({ result: diagnosisResult() }));
    getFileContent = vi.fn(() => of(new Blob(['image'], { type: 'image/png' })));
    getHistory = vi.fn(() =>
      of({ result: { history: historySummary(), snapshot: diagnosisResult() } }),
    );
    getPublishedDisease = vi.fn(() => of({ result: disease() }));
    listActiveCropTypes = vi.fn(() => of({ result: [cropType()] }));
    listHistory = vi.fn(() => of({ result: [] }));

    await TestBed.configureTestingModule({
      imports: [FarmerDiagnosis],
      providers: [
        {
          provide: AgriCatalogApiService,
          useValue: {
            listActiveCropTypes,
            getPublishedDisease,
            getDiseaseRecommendations: vi.fn(() => of({ result: [] })),
            listDiseaseRecommendations: vi.fn(() => of({ result: {
              items: [], page: 0, size: 10, totalElements: 0, totalPages: 0, hasNext: false,
            } })),
          },
        },
        {
          provide: FileApiService,
          useValue: {
            upload: vi.fn(() => of({ result: uploadedFile() })),
            getContent: getFileContent,
          },
        },
        {
          provide: DiagnosisApiService,
          useValue: {
            create: createDiagnosis,
            listHistory,
            getHistory,
          },
        },
        {
          provide: ToastService,
          useValue: { error: vi.fn(), success: vi.fn() },
        },
      ],
    }).compileComponents();
  });

  it('renders layout-shaped skeletons until crops and history finish loading', async () => {
    const cropResponse = new Subject<{ result: CropTypeView[] }>();
    const historyResponse = new Subject<{ result: HistorySummary[] }>();
    listActiveCropTypes.mockReturnValue(cropResponse);
    listHistory.mockReturnValue(historyResponse);
    const fixture = TestBed.createComponent(FarmerDiagnosis);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const scanSkeleton = fixture.nativeElement.querySelector('.scan-skeleton');
    const historySkeleton = fixture.nativeElement.querySelector('.history-skeleton');
    expect(scanSkeleton).not.toBeNull();
    expect(scanSkeleton.getAttribute('aria-busy')).toBe('true');
    expect(historySkeleton).not.toBeNull();
    expect(historySkeleton.querySelectorAll('.history-skeleton__card')).toHaveLength(3);

    cropResponse.next({ result: [cropType()] });
    cropResponse.complete();
    historyResponse.next({ result: [] });
    historyResponse.complete();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.scan-skeleton')).toBeNull();
    expect(fixture.nativeElement.querySelector('.history-skeleton')).toBeNull();
    expect(fixture.nativeElement.querySelector('.scan-panel form')).not.toBeNull();
  });

  it('opens product suggestions from the completed scan without loading history', async () => {
    const fixture = TestBed.createComponent(FarmerDiagnosis);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.componentInstance.result = diagnosisResult();
    fixture.changeDetectorRef.markForCheck();
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('.result-panel .primary-button');
    expect(button.textContent).toContain('Thuốc bảo vệ thực vật được gợi ý');
    button.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-recommended-products')).not.toBeNull();
    expect(getHistory).not.toHaveBeenCalled();
    fixture.componentInstance.closeModalOnEscape();
    expect(fixture.componentInstance.recommendationSnapshot).toBeNull();
    fixture.destroy();
  });

  it('opens product suggestions using the selected history snapshot', async () => {
    const fixture = TestBed.createComponent(FarmerDiagnosis);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.componentInstance.selectHistory(historySummary(), true);
    fixture.detectChanges();
    expect(getHistory).toHaveBeenCalledWith(historySummary().id);
    expect(fixture.componentInstance.recommendationSnapshot?.diagnosisId).toBe(diagnosisResult().diagnosisId);
    expect(fixture.nativeElement.querySelector('app-recommended-products')).not.toBeNull();
    fixture.destroy();
  });

  it('shows a completed diagnosis immediately and prepends it to history', async () => {
    const fixture = TestBed.createComponent(FarmerDiagnosis);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;
    component.scanForm.controls.cropTypeId.setValue('crop-1');
    component.previews = [
      {
        file: new File(['image'], 'rice.png', { type: 'image/png' }),
        url: 'blob:rice-preview',
      },
    ];

    component.scan();
    fixture.detectChanges();

    expect(component.result?.diagnosisId).toBe('diagnosis-1');
    expect(component.history[0]?.id).toBe('diagnosis-1');
    expect(component.imageUrlForFile('file-1')).toBe('blob:rice-preview');
    expect(listHistory).toHaveBeenCalledTimes(1);
    const resultPanel = fixture.nativeElement.querySelector('.result-panel');
    expect(resultPanel).not.toBeNull();
    expect(resultPanel.querySelector('.annotated-preview')?.style.aspectRatio).toBe('1200 / 800');
    expect(resultPanel.querySelectorAll('.history-finding')).toHaveLength(1);
    expect(resultPanel.querySelector('.history-finding__confidence')?.textContent).toContain('91%');
    expect(resultPanel.querySelector('.annotated-preview__trigger')).not.toBeNull();

    (resultPanel.querySelector('.annotated-preview__trigger') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.image-lightbox')).not.toBeNull();
  });

  it('renders a diagnosis when the asynchronous request completes in zoneless mode', async () => {
    const diagnosisResponse = new Subject<{ result: DiagnosisResult }>();
    createDiagnosis.mockReturnValue(diagnosisResponse);
    const fixture = TestBed.createComponent(FarmerDiagnosis);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;
    component.scanForm.controls.cropTypeId.setValue('crop-1');
    component.previews = [
      {
        file: new File(['image'], 'rice.png', { type: 'image/png' }),
        url: 'blob:rice-preview',
      },
    ];

    component.scan();
    diagnosisResponse.next({ result: diagnosisResult() });
    diagnosisResponse.complete();
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('.result-panel')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('#result-title')?.textContent).toContain(
      'Cây trồng: Lúa',
    );
    expect(
      fixture.nativeElement.querySelector('.result-panel .status-badge')?.textContent,
    ).toContain('Có dấu hiệu bệnh');
  });

  it('opens the selected history snapshot in a modal when its request completes', async () => {
    const history = historySummary();
    const historyResponse = new Subject<{ result: DiagnosisHistoryDetail }>();
    listHistory.mockReturnValue(of({ result: [history] }));
    getHistory.mockReturnValue(historyResponse);
    const fixture = TestBed.createComponent(FarmerDiagnosis);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();

    const historyButton = fixture.nativeElement.querySelector(
      '.history-list button',
    ) as HTMLButtonElement;
    expect(historyButton).not.toBeNull();
    historyButton.click();
    expect(getHistory).toHaveBeenCalledWith(history.id);

    historyResponse.next({
      result: {
        history,
        snapshot: JSON.stringify(diagnosisResult()) as unknown as DiagnosisResult,
      },
    });
    historyResponse.complete();
    await fixture.whenStable();

    const detailModal = fixture.nativeElement.querySelector('.history-modal');
    expect(detailModal).not.toBeNull();
    expect(detailModal.getAttribute('role')).toBe('dialog');
    expect(detailModal.textContent).toContain('Bệnh đạo ôn');
    expect(detailModal.textContent).not.toContain('KẾT QUẢ ĐÃ LƯU');
    expect(detailModal.textContent).not.toContain('Kết quả chẩn đoán');
    expect(detailModal.querySelectorAll('.history-finding')).toHaveLength(1);
    expect(detailModal.querySelector('.history-finding__confidence')?.textContent).toContain('91%');
    expect(detailModal.textContent).not.toContain('Có dấu hiệu bệnh');
    expect(detailModal.textContent).not.toContain('rice.png');
    expect(detailModal.querySelector('.annotated-preview img')).not.toBeNull();
    expect(getFileContent).toHaveBeenCalledWith('file-1');

    (detailModal.querySelector('.annotated-preview__trigger') as HTMLButtonElement).click();
    await fixture.whenStable();
    const lightbox = fixture.nativeElement.querySelector('.image-lightbox');
    expect(lightbox).not.toBeNull();
    expect(lightbox.getAttribute('role')).toBe('dialog');

    (lightbox.querySelector('[aria-label="Phóng to ảnh"]') as HTMLButtonElement).click();
    expect(component.imageZoom).toBe(1.25);
    (lightbox.querySelector('[aria-label="Đóng ảnh"]') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.image-lightbox')).toBeNull();

    (detailModal.querySelector('.modal-close') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.history-modal')).toBeNull();
  });

  it('keeps the current records visible when refreshing history fails', async () => {
    const history = historySummary();
    listHistory.mockReturnValue(of({ result: [history] }));
    const fixture = TestBed.createComponent(FarmerDiagnosis);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelectorAll('.history-list .history-card')).toHaveLength(1);
    listHistory.mockReturnValue(throwError(() => new Error('History unavailable')));

    (fixture.nativeElement.querySelector('.history-refresh') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelectorAll('.history-list .history-card')).toHaveLength(1);
    expect(fixture.nativeElement.querySelector('.history-panel')?.textContent).toContain(
      'History unavailable',
    );
  });

  it('accepts a history endpoint that returns the diagnosis snapshot directly', async () => {
    const history = historySummary();
    listHistory.mockReturnValue(of({ result: [history] }));
    getHistory.mockReturnValue(of({ result: diagnosisResult() }));
    const fixture = TestBed.createComponent(FarmerDiagnosis);
    fixture.detectChanges();
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('.history-list button') as HTMLButtonElement).click();
    await fixture.whenStable();

    const detailModal = fixture.nativeElement.querySelector('.history-modal');
    expect(detailModal).not.toBeNull();
    expect(detailModal.textContent).toContain('Bệnh đạo ôn');
  });

  it('finds a diagnosis snapshot inside unknown nested wrappers', async () => {
    const history = historySummary();
    listHistory.mockReturnValue(of({ result: [history] }));
    getHistory.mockReturnValue(
      of({
        result: {
          payload: {
            stored_result: {
              data: JSON.stringify(diagnosisResult()),
            },
          },
        },
      }),
    );
    const fixture = TestBed.createComponent(FarmerDiagnosis);
    fixture.detectChanges();
    await fixture.whenStable();

    (fixture.nativeElement.querySelector('.history-list button') as HTMLButtonElement).click();
    await fixture.whenStable();

    const detailModal = fixture.nativeElement.querySelector('.history-modal');
    expect(detailModal).not.toBeNull();
    expect(detailModal.querySelectorAll('.result-image-card')).toHaveLength(1);
    expect(detailModal.textContent).toContain('Bệnh đạo ôn');
  });

  it('opens disease information in a modal and closes it', async () => {
    const fixture = TestBed.createComponent(FarmerDiagnosis);
    fixture.detectChanges();
    await fixture.whenStable();
    const component = fixture.componentInstance;
    component.scanForm.controls.cropTypeId.setValue('crop-1');
    component.previews = [
      {
        file: new File(['image'], 'rice.png', { type: 'image/png' }),
        url: 'blob:rice-preview',
      },
    ];
    component.scan();
    await fixture.whenStable();

    const diseaseButton = fixture.nativeElement.querySelector(
      '.result-panel .history-finding > button',
    ) as HTMLButtonElement;
    diseaseButton.click();
    await fixture.whenStable();

    const modal = fixture.nativeElement.querySelector('.disease-modal');
    expect(modal).not.toBeNull();
    expect(modal.getAttribute('role')).toBe('dialog');
    expect(modal.textContent).toContain('Bệnh đạo ôn');
    expect(getPublishedDisease).toHaveBeenCalledWith('disease-1');

    (modal.querySelector('.modal-close') as HTMLButtonElement).click();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.disease-modal')).toBeNull();
  });

  function cropType(): CropTypeView {
    return {
      id: 'crop-1',
      code: 'RICE',
      name: 'Lúa',
      description: null,
      active: true,
    };
  }

  function uploadedFile(): FileView {
    return {
      id: 'file-1',
      ownerUserId: 'farmer-1',
      purpose: 'DIAGNOSIS_IMAGE',
      originalFileName: 'rice.png',
      contentType: 'image/png',
      sizeBytes: 5,
      publicUrl: null,
      status: 'ACTIVE',
      createdAt: null,
      updatedAt: null,
    };
  }

  function historySummary(): HistorySummary {
    return {
      id: 'diagnosis-1',
      cropTypeId: 'crop-1',
      modelId: 'model-1',
      modelVersionId: 'version-1',
      modelVersion: '1.0.0',
      status: 'DISEASED',
      createdAt: '2026-08-19T12:00:00Z',
    };
  }

  function disease(): DiseaseView {
    return {
      id: 'disease-1',
      name: 'Bệnh đạo ôn',
      slug: 'benh-dao-on',
      scientificName: 'Magnaporthe oryzae',
      cropTypeId: 'crop-1',
      affectedPart: 'Lá',
      pathogenType: 'Nấm',
      shortDescription: 'Bệnh phổ biến trên cây lúa.',
      description: null,
      symptoms: 'Vết bệnh hình thoi trên lá.',
      causes: 'Nấm gây bệnh.',
      favorableConditions: null,
      preventionMethod: 'Giữ ruộng thông thoáng.',
      treatmentGuideline: 'Xử lý theo hướng dẫn chuyên gia.',
      thumbnailUrl: null,
      createdSource: 'ADMIN',
      brandId: null,
      reviewStatus: 'APPROVED',
      rejectionReason: null,
      submittedAt: null,
      reviewedAt: null,
      reviewedBy: null,
      publishedAt: '2026-08-01T00:00:00Z',
      createdAt: '2026-08-01T00:00:00Z',
      createdBy: 'admin-1',
      updatedAt: '2026-08-01T00:00:00Z',
      updatedBy: 'admin-1',
    };
  }

  function diagnosisResult(): DiagnosisResult {
    return {
      diagnosisId: 'diagnosis-1',
      cropTypeId: 'crop-1',
      model: {
        id: 'model-1',
        name: 'Rice detector',
        versionId: 'version-1',
        version: '1.0.0',
      },
      status: 'DISEASED',
      images: [
        {
          file: {
            id: 'file-1',
            originalFileName: 'rice.png',
            contentType: 'image/png',
            sizeBytes: 5,
            createdAt: '2026-08-19T12:00:00Z',
            updatedAt: '2026-08-19T12:00:00Z',
          },
          width: 1200,
          height: 800,
          status: 'DISEASED',
          detections: [
            {
              classCode: 'BLAST',
              displayName: 'Đạo ôn',
              classKind: 'DISEASE',
              confidence: 0.91,
              boundingBox: { x: 100, y: 80, width: 240, height: 180 },
              disease: { id: 'disease-1', displayName: 'Bệnh đạo ôn' },
            },
          ],
        },
      ],
      groups: [
        {
          status: 'DISEASED',
          classCode: 'BLAST',
          displayName: 'Đạo ôn',
          disease: { id: 'disease-1', displayName: 'Bệnh đạo ôn' },
          fileIds: ['file-1'],
          detectionCount: 1,
        },
      ],
      processingTimeMs: 125,
    };
  }
});
