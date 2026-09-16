import { UploadPolicyService } from '../../../core/service/upload-policy.service';
import { UploadPolicyHint } from '../../../shared/upload-policy-hint/upload-policy-hint';
import { CommonModule, DatePipe } from '@angular/common';
import { Component, afterNextRender, inject } from '@angular/core';
import { FormArray, FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import {
  AgriCatalogApiService,
  AiModelDeploymentView,
  AiModelDiseaseMappingView,
  AiModelVersionClassView,
  AiModelVersionView,
  AiModelView,
  CropTypeView,
  DiseaseView,
} from '../../../core/api/agri-catalog-api.service';
import { FileApiService, FileView } from '../../../core/api/file-api.service';
import { apiErrorMessage, unwrapApiResult } from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-admin-ai-models',
  standalone: true,
  imports: [UploadPolicyHint, CommonModule, DatePipe, FormsModule, ReactiveFormsModule],
  templateUrl: './ai-models.html',
  styleUrl: './ai-models.scss',
})
export class AdminAiModels {
  readonly uploadPolicies = inject(UploadPolicyService).watch(['MODEL_ARTIFACT']);
  private readonly formBuilder = inject(FormBuilder);
  private readonly catalogApi = inject(AgriCatalogApiService);
  private readonly fileApi = inject(FileApiService);
  private readonly toast = inject(ToastService);

  readonly modelForm = this.formBuilder.nonNullable.group({
    code: ['', [Validators.required, Validators.pattern(/^[A-Z][A-Z0-9_]*$/)]],
    name: ['', Validators.required],
    description: [''],
    taskType: ['DISEASE_DETECTION' as const],
    cropCoverageType: this.formBuilder.nonNullable.control<'SELECTED_CROPS' | 'ALL_CROPS'>(
      'SELECTED_CROPS',
    ),
    cropTypeIds: this.formBuilder.nonNullable.control<string[]>([]),
  });

  readonly versionForm = this.formBuilder.nonNullable.group({
    version: ['', Validators.required],
    artifactFileId: ['', Validators.required],
    artifactSha256: ['', [Validators.required, Validators.pattern(/^[a-fA-F0-9]{64}$/)]],
    inputWidth: [640, [Validators.required, Validators.min(1)]],
    inputHeight: [640, [Validators.required, Validators.min(1)]],
    classes: this.formBuilder.array([this.createClassControl(0)]),
  });

  readonly deploymentForm = this.formBuilder.nonNullable.group({
    modelVersionId: ['', Validators.required],
    deploymentScope: this.formBuilder.nonNullable.control<'CROP' | 'ALL_CROPS'>('CROP'),
    cropTypeId: [''],
    priority: [0, [Validators.required, Validators.min(0)]],
  });

  models: AiModelView[] = [];
  cropTypes: CropTypeView[] = [];
  versions: AiModelVersionView[] = [];
  mappings: AiModelDiseaseMappingView[] = [];
  deployments: AiModelDeploymentView[] = [];
  diseases: DiseaseView[] = [];
  selectedModelId: string | null = null;
  selectedVersionId: string | null = null;
  mappingCropTypeId = '';
  modelEditorOpen = false;
  editingModelId: string | null = null;
  loading = false;
  loadingVersions = false;
  loadingMappings = false;
  savingModel = false;
  savingVersion = false;
  savingDeployment = false;
  uploadingArtifact = false;
  actionId = '';
  artifactFileName = '';
  loadError = '';
  private mappingLoadSequence = 0;

  constructor() {
    afterNextRender(() => this.loadWorkspace());
  }

  get selectedModel(): AiModelView | null {
    return this.models.find((model) => model.id === this.selectedModelId) ?? null;
  }

  get selectedVersion(): AiModelVersionView | null {
    return this.versions.find((version) => version.id === this.selectedVersionId) ?? null;
  }

  get classControls(): FormArray {
    return this.versionForm.controls.classes;
  }

  get diseaseClasses(): AiModelVersionClassView[] {
    return this.selectedVersion?.classes.filter((item) => item.classKind === 'DISEASE') ?? [];
  }

  loadWorkspace(): void {
    this.loading = true;
    this.loadError = '';
    forkJoin({
      models: this.catalogApi.listAiModels(),
      cropTypes: this.catalogApi.listCropTypes(),
      deployments: this.catalogApi.listAiModelDeployments(),
    })
      .pipe(
        finalize(() => {
          this.loading = false;
        }),
      )
      .subscribe({
        next: ({ models, cropTypes, deployments }) => {
          this.models = unwrapApiResult<AiModelView[]>(models) ?? [];
          this.cropTypes = unwrapApiResult<CropTypeView[]>(cropTypes) ?? [];
          this.deployments = unwrapApiResult<AiModelDeploymentView[]>(deployments) ?? [];
          if (this.selectedModelId && this.models.some((item) => item.id === this.selectedModelId)) {
            this.selectModel(this.selectedModelId);
          } else if (this.models.length) {
            this.selectModel(this.models[0].id);
          }
        },
        error: (error) => {
          this.loadError = apiErrorMessage(error, 'Không thể tải không gian quản trị model.');
        },
      });
  }

  selectModel(modelId: string): void {
    this.mappingLoadSequence += 1;
    this.selectedModelId = modelId;
    this.selectedVersionId = null;
    this.mappings = [];
    this.diseases = [];
    this.versions = [];
    this.mappingCropTypeId = '';
    this.loadingMappings = false;
    const model = this.models.find((item) => item.id === modelId);
    this.deploymentForm.patchValue({
      modelVersionId: '',
      deploymentScope: model?.cropCoverageType === 'ALL_CROPS' ? 'ALL_CROPS' : 'CROP',
      cropTypeId: '',
    });
    this.loadingVersions = true;
    this.catalogApi
      .listAiModelVersions(modelId)
      .pipe(
        finalize(() => {
          this.loadingVersions = false;
        }),
      )
      .subscribe({
        next: (response) => {
          this.versions = (unwrapApiResult<AiModelVersionView[]>(response) ?? []).sort(
            (left, right) => right.createdAt.localeCompare(left.createdAt),
          );
          if (this.versions.length) this.selectVersion(this.versions[0].id);
        },
        error: (error) => {
          this.toast.error(apiErrorMessage(error, 'Không thể tải các phiên bản model.'));
        },
      });
  }

  selectVersion(versionId: string): void {
    this.selectedVersionId = versionId;
    this.mappings = [];
    this.diseases = [];
    this.deploymentForm.patchValue({ modelVersionId: versionId });
    const defaultCropId = this.mappingCropTypeId || this.coveredCropTypes()[0]?.id || this.cropTypes[0]?.id || '';
    this.changeMappingCrop(defaultCropId);
  }

  changeMappingCrop(cropTypeId: string): void {
    const loadSequence = ++this.mappingLoadSequence;
    this.mappingCropTypeId = cropTypeId;
    const version = this.selectedVersion;
    if (!version || !cropTypeId) {
      this.mappings = [];
      this.diseases = [];
      this.loadingMappings = false;
      return;
    }
    this.loadingMappings = true;
    forkJoin({
      mappings: this.catalogApi.listAiModelDiseaseMappings(version.id),
      diseases: this.catalogApi.listDiseases({ cropTypeId }),
    })
      .pipe(
        finalize(() => {
          if (loadSequence === this.mappingLoadSequence) this.loadingMappings = false;
        }),
      )
      .subscribe({
        next: ({ mappings, diseases }) => {
          if (
            loadSequence !== this.mappingLoadSequence ||
            this.selectedVersionId !== version.id ||
            this.mappingCropTypeId !== cropTypeId
          ) {
            return;
          }
          this.mappings = unwrapApiResult<AiModelDiseaseMappingView[]>(mappings) ?? [];
          this.diseases = unwrapApiResult<DiseaseView[]>(diseases) ?? [];
        },
        error: (error) => {
          if (loadSequence !== this.mappingLoadSequence) return;
          this.toast.error(apiErrorMessage(error, 'Không thể tải mapping class và bệnh.'));
        },
      });
  }

  openCreateModel(): void {
    this.editingModelId = null;
    this.modelForm.reset({
      code: '',
      name: '',
      description: '',
      taskType: 'DISEASE_DETECTION',
      cropCoverageType: 'SELECTED_CROPS',
      cropTypeIds: [],
    });
    this.modelEditorOpen = true;
  }

  openEditModel(model: AiModelView): void {
    this.editingModelId = model.id;
    this.modelForm.reset({
      code: model.code,
      name: model.name,
      description: model.description ?? '',
      taskType: model.taskType,
      cropCoverageType: model.cropCoverageType,
      cropTypeIds: [...model.cropTypeIds],
    });
    this.modelEditorOpen = true;
  }

  closeModelEditor(): void {
    if (!this.savingModel) this.modelEditorOpen = false;
  }

  saveModel(): void {
    if (this.modelForm.invalid) {
      this.modelForm.markAllAsTouched();
      return;
    }
    const value = this.modelForm.getRawValue();
    const cropTypeIds = value.cropCoverageType === 'ALL_CROPS' ? [] : value.cropTypeIds;
    if (value.cropCoverageType === 'SELECTED_CROPS' && !cropTypeIds.length) {
      this.toast.error('Hãy chọn ít nhất một loại cây trồng cho phạm vi model.');
      return;
    }
    this.savingModel = true;
    const request = this.editingModelId
      ? this.catalogApi.updateAiModel(this.editingModelId, {
          name: value.name.trim(),
          description: this.nullIfBlank(value.description),
          cropCoverageType: value.cropCoverageType,
          cropTypeIds,
        })
      : this.catalogApi.createAiModel({
          code: value.code.trim(),
          name: value.name.trim(),
          description: this.nullIfBlank(value.description),
          taskType: value.taskType,
          cropCoverageType: value.cropCoverageType,
          cropTypeIds,
        });
    request
      .pipe(
        finalize(() => {
          this.savingModel = false;
        }),
      )
      .subscribe({
        next: (response) => {
          const model = unwrapApiResult<AiModelView>(response);
          if (!model) {
            this.toast.error('Máy chủ không trả về model vừa lưu.');
            return;
          }
          this.models = this.editingModelId
            ? this.models.map((item) => (item.id === model.id ? model : item))
            : [model, ...this.models];
          this.modelEditorOpen = false;
          this.selectModel(model.id);
          this.toast.success(this.editingModelId ? 'Đã cập nhật model.' : 'Đã tạo model.');
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể lưu model.')),
      });
  }

  retireModel(model: AiModelView): void {
    if (this.actionId) return;
    if (!window.confirm(`Ngừng sử dụng model “${model.name}”? Các phiên bản của model này sẽ không thể phục vụ chẩn đoán mới.`)) return;
    this.actionId = model.id;
    this.catalogApi
      .retireAiModel(model.id)
      .pipe(
        finalize(() => {
          this.actionId = '';
        }),
      )
      .subscribe({
        next: () => {
          this.toast.success('Đã ngừng sử dụng model.');
          this.loadWorkspace();
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể retire model.')),
      });
  }

  addClass(): void {
    this.classControls.push(this.createClassControl(this.classControls.length));
  }

  removeClass(index: number): void {
    if (this.classControls.length > 1) this.classControls.removeAt(index);
  }

  uploadArtifact(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file || this.uploadingArtifact) return;
    const validationError = this.uploadPolicies.validate(file, 'MODEL_ARTIFACT');
    if (validationError) {
      this.toast.error(validationError);
      return;
    }
    this.uploadingArtifact = true;
    void this.sha256(file)
      .then((checksum) => {
        this.fileApi
          .upload(file, 'MODEL_ARTIFACT')
          .pipe(
            finalize(() => {
              this.uploadingArtifact = false;
            }),
          )
          .subscribe({
            next: (response) => {
              const uploaded = unwrapApiResult<FileView>(response);
              if (!uploaded) {
                this.toast.error('Máy chủ không trả về artifact đã tải lên.');
                return;
              }
              this.versionForm.patchValue({ artifactFileId: uploaded.id, artifactSha256: checksum });
              this.artifactFileName = uploaded.originalFileName;
              this.toast.success('Đã tải artifact và kiểm tra checksum cục bộ.');
            },
            error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể tải artifact ONNX.')),
          });
      })
      .catch(() => {
        this.uploadingArtifact = false;
        this.toast.error('Không thể tính checksum cho artifact.');
      });
  }

  createVersion(): void {
    const model = this.selectedModel;
    if (!model) return;
    if (this.versionForm.invalid) {
      this.versionForm.markAllAsTouched();
      return;
    }
    const value = this.versionForm.getRawValue();
    const classIndexes = value.classes.map((item) => item.classIndex);
    if (new Set(classIndexes).size !== classIndexes.length) {
      this.toast.error('Mỗi class trong manifest phải có class index duy nhất.');
      return;
    }
    this.savingVersion = true;
    this.catalogApi
      .createAiModelVersion(model.id, {
        version: value.version.trim(),
        artifactFileId: value.artifactFileId,
        artifactSha256: value.artifactSha256,
        inputWidth: Number(value.inputWidth),
        inputHeight: Number(value.inputHeight),
        classes: value.classes.map((item) => ({
          classIndex: Number(item.classIndex),
          classCode: item.classCode.trim(),
          displayName: item.displayName.trim(),
          classKind: item.classKind,
        })),
      })
      .pipe(
        finalize(() => {
          this.savingVersion = false;
        }),
      )
      .subscribe({
        next: (response) => {
          const version = unwrapApiResult<AiModelVersionView>(response);
          if (!version) {
            this.toast.error('Máy chủ không trả về phiên bản vừa tạo.');
            return;
          }
          this.versions = [version, ...this.versions];
          this.selectVersion(version.id);
          this.resetVersionForm();
          this.toast.success('Đã tạo phiên bản. Hãy validate, mapping và đánh dấu READY trước khi activate.');
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể tạo phiên bản model.')),
      });
  }

  validateVersion(version: AiModelVersionView): void {
    this.runVersionAction(version, 'validate');
  }

  markVersionReady(version: AiModelVersionView): void {
    this.runVersionAction(version, 'ready');
  }

  retireVersion(version: AiModelVersionView): void {
    if (!window.confirm(`Ngừng sử dụng phiên bản “${version.version}”? Thao tác này không thể hoàn tác trên dashboard.`)) return;
    this.runVersionAction(version, 'retire');
  }

  saveMapping(modelClass: AiModelVersionClassView, diseaseId: string): void {
    const version = this.selectedVersion;
    if (!version || !this.mappingCropTypeId || this.actionId) return;
    const current = this.mappings.find(
      (item) => item.modelVersionClassId === modelClass.id && item.cropTypeId === this.mappingCropTypeId,
    );
    this.actionId = `mapping-${modelClass.id}`;
    const completed = () => {
      this.actionId = '';
    };
    if (!diseaseId && current) {
      this.catalogApi
        .deleteAiModelDiseaseMapping(current.id)
        .pipe(finalize(completed))
        .subscribe({
          next: () => {
            this.mappings = this.mappings.filter((item) => item.id !== current.id);
            this.toast.success('Đã xoá mapping bệnh.');
          },
          error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể xoá mapping bệnh.')),
        });
      return;
    }
    if (!diseaseId) {
      completed();
      return;
    }
    const payload = {
      modelVersionClassId: modelClass.id,
      cropTypeId: this.mappingCropTypeId,
      diseaseId,
    };
    const request = current
      ? this.catalogApi.updateAiModelDiseaseMapping(current.id, payload)
      : this.catalogApi.createAiModelDiseaseMapping(payload);
    request.pipe(finalize(completed)).subscribe({
      next: (response) => {
        const mapping = unwrapApiResult<AiModelDiseaseMappingView>(response);
        if (!mapping) {
          this.toast.error('Máy chủ không trả về mapping vừa lưu.');
          return;
        }
        this.mappings = current
          ? this.mappings.map((item) => (item.id === mapping.id ? mapping : item))
          : [...this.mappings, mapping];
        this.toast.success('Đã lưu mapping class sang bệnh.');
      },
      error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể lưu mapping bệnh.')),
    });
  }

  createDeployment(): void {
    if (!this.canCreateDeployment()) {
      this.toast.error('Chỉ phiên bản READY hoặc ACTIVE mới có thể tạo triển khai.');
      return;
    }
    if (this.deploymentForm.invalid) {
      this.deploymentForm.markAllAsTouched();
      return;
    }
    const value = this.deploymentForm.getRawValue();
    if (value.deploymentScope === 'CROP' && !value.cropTypeId) {
      this.toast.error('Deployment theo cây trồng cần chọn loại cây trồng.');
      return;
    }
    this.savingDeployment = true;
    this.catalogApi
      .createAiModelDeployment({
        modelVersionId: value.modelVersionId,
        deploymentScope: value.deploymentScope,
        cropTypeId: value.deploymentScope === 'CROP' ? value.cropTypeId : null,
        priority: Number(value.priority),
      })
      .pipe(
        finalize(() => {
          this.savingDeployment = false;
        }),
      )
      .subscribe({
        next: (response) => {
          const deployment = unwrapApiResult<AiModelDeploymentView>(response);
          if (!deployment) {
            this.toast.error('Máy chủ không trả về deployment vừa tạo.');
            return;
          }
          this.deployments = [deployment, ...this.deployments];
          this.toast.success('Đã tạo deployment ở trạng thái INACTIVE.');
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể tạo deployment.')),
      });
  }

  activateDeployment(deployment: AiModelDeploymentView): void {
    if (!this.canActivate(deployment) || this.actionId) return;
    const replaced = this.activeDeploymentForSlot(deployment);
    this.actionId = deployment.id;
    this.catalogApi
      .activateAiModelDeployment(deployment.id)
      .pipe(
        finalize(() => {
          this.actionId = '';
        }),
      )
      .subscribe({
        next: (response) => {
          const updated = unwrapApiResult<AiModelDeploymentView>(response);
          if (updated) {
            this.deployments = this.deployments.map((item) => {
              if (item.id === updated.id) return updated;
              if (item.id === replaced?.id) return { ...item, status: 'INACTIVE' };
              return item;
            });
          }
          this.refreshDeploymentState();
          this.toast.success(
            replaced
              ? `Đã chuyển model phục vụ sang ${this.versionFor(deployment.modelVersionId)?.version ?? 'phiên bản đã chọn'}.`
              : 'Đã kích hoạt model cho phạm vi đã chọn.',
          );
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Checklist server chưa cho phép activate deployment.')),
      });
  }

  deactivateDeployment(deployment: AiModelDeploymentView): void {
    if (this.actionId) return;
    if (!window.confirm(`Tạm dừng model cho “${this.cropName(deployment.cropTypeId)}”? Nếu không có deployment dự phòng, Farmer sẽ chưa thể chẩn đoán.`)) return;
    this.actionId = deployment.id;
    this.catalogApi
      .deactivateAiModelDeployment(deployment.id)
      .pipe(
        finalize(() => {
          this.actionId = '';
        }),
      )
      .subscribe({
        next: (response) => {
          const updated = unwrapApiResult<AiModelDeploymentView>(response);
          if (updated) this.replaceDeployment(updated);
          this.refreshDeploymentState();
          this.toast.success('Đã tạm dừng model cho phạm vi đã chọn.');
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Không thể deactivate deployment.')),
      });
  }

  mappingDiseaseId(modelVersionClassId: string): string {
    return this.mappings.find(
      (item) => item.modelVersionClassId === modelVersionClassId && item.cropTypeId === this.mappingCropTypeId,
    )?.diseaseId ?? '';
  }

  coveredCropTypes(): CropTypeView[] {
    const model = this.selectedModel;
    if (!model || model.cropCoverageType === 'ALL_CROPS') return this.cropTypes.filter((item) => item.active);
    return this.cropTypes.filter((item) => model.cropTypeIds.includes(item.id));
  }

  cropName(cropTypeId: string | null): string {
    if (!cropTypeId) return 'Tất cả cây trồng';
    return this.cropTypes.find((item) => item.id === cropTypeId)?.name ?? cropTypeId;
  }

  versionFor(versionId: string): AiModelVersionView | null {
    return this.versions.find((item) => item.id === versionId) ?? null;
  }

  canActivate(deployment: AiModelDeploymentView): boolean {
    const status = this.versionFor(deployment.modelVersionId)?.status;
    return status === 'READY' || status === 'ACTIVE';
  }

  canCreateDeployment(): boolean {
    const status = this.selectedVersion?.status;
    return status === 'READY' || status === 'ACTIVE';
  }

  deploymentsForSelectedModel(): AiModelDeploymentView[] {
    const versionIds = new Set(this.versions.map((item) => item.id));
    return this.deployments.filter((item) => versionIds.has(item.modelVersionId));
  }

  activeDeploymentForSlot(deployment: AiModelDeploymentView): AiModelDeploymentView | null {
    return this.deployments.find(
      (item) =>
        item.id !== deployment.id &&
        item.status === 'ACTIVE' &&
        item.deploymentScope === deployment.deploymentScope &&
        item.cropTypeId === deployment.cropTypeId &&
        item.priority === deployment.priority,
    ) ?? null;
  }

  activationLabel(deployment: AiModelDeploymentView): string {
    const target = this.versionFor(deployment.modelVersionId)?.version ?? 'phiên bản này';
    const active = this.activeDeploymentForSlot(deployment);
    if (!active) return `Kích hoạt ${target}`;
    const current = this.versionFor(active.modelVersionId)?.version ?? 'phiên bản hiện tại';
    return `Chuyển sang ${target} (thay ${current})`;
  }

  requiredMappingCount(version: AiModelVersionView): number {
    const diseaseClassCount = version.classes.filter((item) => item.classKind === 'DISEASE').length;
    return diseaseClassCount * this.coveredCropTypes().length;
  }

  completedMappingCount(version: AiModelVersionView): number {
    if (version.id !== this.selectedVersionId) return 0;
    const classIds = new Set(
      version.classes.filter((item) => item.classKind === 'DISEASE').map((item) => item.id),
    );
    const cropIds = new Set(this.coveredCropTypes().map((item) => item.id));
    return new Set(
      this.mappings
        .filter((item) => classIds.has(item.modelVersionClassId) && cropIds.has(item.cropTypeId))
        .map((item) => `${item.modelVersionClassId}:${item.cropTypeId}`),
    ).size;
  }

  canMarkReady(version: AiModelVersionView): boolean {
    return version.id === this.selectedVersionId &&
      !this.loadingMappings &&
      this.completedMappingCount(version) === this.requiredMappingCount(version);
  }

  readinessHint(version: AiModelVersionView): string {
    if (version.status === 'ACTIVE') return 'Đang phục vụ chẩn đoán ở ít nhất một phạm vi.';
    if (version.status === 'READY') return 'Checklist server đã đạt: có thể deployment và activate.';
    if (version.status === 'VALIDATED') return 'Đã validate artifact. Hoàn tất mapping bệnh theo crop rồi đánh dấu READY.';
    if (version.status === 'VALIDATING') return 'Server đang kiểm tra artifact và class manifest.';
    if (version.status === 'DRAFT') return 'Chưa validate artifact.';
    return 'Phiên bản đã retire, không thể kích hoạt.';
  }

  private runVersionAction(version: AiModelVersionView, action: 'validate' | 'ready' | 'retire'): void {
    if (this.actionId) return;
    this.actionId = version.id;
    const request =
      action === 'validate'
        ? this.catalogApi.validateAiModelVersion(version.id)
        : action === 'ready'
          ? this.catalogApi.markAiModelVersionReady(version.id)
          : this.catalogApi.retireAiModelVersion(version.id);
    request
      .pipe(
        finalize(() => {
          this.actionId = '';
        }),
      )
      .subscribe({
        next: (response) => {
          const updated = unwrapApiResult<AiModelVersionView>(response);
          if (updated) {
            this.versions = this.versions.map((item) => (item.id === updated.id ? updated : item));
          }
          this.toast.success(
            action === 'validate'
              ? 'Đã kiểm tra file ONNX.'
              : action === 'ready'
                ? 'Phiên bản đã được cho phép triển khai.'
                : 'Đã ngừng sử dụng phiên bản.',
          );
        },
        error: (error) => this.toast.error(apiErrorMessage(error, 'Thao tác version không được server chấp nhận.')),
      });
  }

  private replaceDeployment(updated: AiModelDeploymentView): void {
    this.deployments = this.deployments.map((item) => (item.id === updated.id ? updated : item));
  }

  private refreshDeploymentState(): void {
    const modelId = this.selectedModelId;
    if (!modelId) return;
    forkJoin({
      deployments: this.catalogApi.listAiModelDeployments(),
      versions: this.catalogApi.listAiModelVersions(modelId),
    }).subscribe({
      next: ({ deployments, versions }) => {
        if (this.selectedModelId !== modelId) return;
        this.deployments = unwrapApiResult<AiModelDeploymentView[]>(deployments) ?? this.deployments;
        this.versions = (unwrapApiResult<AiModelVersionView[]>(versions) ?? this.versions).sort(
          (left, right) => right.createdAt.localeCompare(left.createdAt),
        );
      },
      error: () => this.toast.error('Thao tác đã hoàn tất nhưng chưa thể tải lại trạng thái mới.'),
    });
  }

  private resetVersionForm(): void {
    this.versionForm.reset({
      version: '',
      artifactFileId: '',
      artifactSha256: '',
      inputWidth: 640,
      inputHeight: 640,
    });
    while (this.classControls.length > 1) this.classControls.removeAt(this.classControls.length - 1);
    this.classControls.at(0).reset({ classIndex: 0, classCode: '', displayName: '', classKind: 'DISEASE' });
    this.artifactFileName = '';
  }

  private createClassControl(index: number) {
    return this.formBuilder.nonNullable.group({
      classIndex: [index, [Validators.required, Validators.min(0)]],
      classCode: ['', Validators.required],
      displayName: ['', Validators.required],
      classKind: this.formBuilder.nonNullable.control<'DISEASE' | 'HEALTHY'>('DISEASE'),
    });
  }

  private nullIfBlank(value: string): string | null {
    const trimmed = value.trim();
    return trimmed || null;
  }

  private async sha256(file: File): Promise<string> {
    const bytes = await file.arrayBuffer();
    const digest = await crypto.subtle.digest('SHA-256', bytes);
    return Array.from(new Uint8Array(digest))
      .map((item) => item.toString(16).padStart(2, '0'))
      .join('');
  }
}
