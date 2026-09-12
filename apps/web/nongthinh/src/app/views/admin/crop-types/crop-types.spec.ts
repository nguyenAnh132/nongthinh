import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import {
  AgriCatalogApiService,
  CropTypeView,
} from '../../../core/api/agri-catalog-api.service';
import { ToastService } from '../../../shared/toast/toast.service';
import { AdminCropTypes } from './crop-types';

describe('AdminCropTypes', () => {
  let listCropTypes: ReturnType<typeof vi.fn>;
  let createCropType: ReturnType<typeof vi.fn>;
  let updateCropType: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    listCropTypes = vi.fn(() => of({ result: [cropType()] }));
    createCropType = vi.fn(() => of({ result: cropType() }));
    updateCropType = vi.fn(() => of({ result: { ...cropType(), active: false } }));

    await TestBed.configureTestingModule({
      imports: [AdminCropTypes],
      providers: [
        {
          provide: AgriCatalogApiService,
          useValue: {
            listCropTypes,
            createCropType,
            updateCropType,
            deleteCropType: vi.fn(),
          },
        },
        {
          provide: ToastService,
          useValue: {
            error: vi.fn(),
            success: vi.fn(),
          },
        },
      ],
    }).compileComponents();
  });

  it('loads crop types for the admin dashboard', async () => {
    const fixture = TestBed.createComponent(AdminCropTypes);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(listCropTypes).toHaveBeenCalledTimes(1);
    expect(fixture.componentInstance.cropTypes).toEqual([cropType()]);
    expect(fixture.nativeElement.textContent).toContain('Lúa');
  });

  it('creates a crop type with an uppercase code', () => {
    const fixture = TestBed.createComponent(AdminCropTypes);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.openCreate();
    component.cropTypeForm.patchValue({
      code: 'rice',
      name: 'Lúa',
      description: 'Cây lúa',
    });
    component.onCodeInput();

    component.submitCropType();

    expect(createCropType).toHaveBeenCalledWith({
      code: 'RICE',
      name: 'Lúa',
      description: 'Cây lúa',
    });
  });

  it('updates name and status without changing the code', () => {
    const fixture = TestBed.createComponent(AdminCropTypes);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.cropTypes = [cropType()];
    component.startEdit(cropType());
    component.cropTypeForm.patchValue({ name: 'Lúa nước', active: false });

    component.submitCropType();

    expect(updateCropType).toHaveBeenCalledWith('crop-type-1', {
      name: 'Lúa nước',
      description: null,
      active: false,
    });
  });

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
