import { DestroyRef, Injectable, afterNextRender, inject, signal } from '@angular/core';
import { Observable, catchError, finalize, map, of, shareReplay, tap, throwError } from 'rxjs';
import { FilePurpose } from '../api/file-api.service';
import { FileConfigurationApiService, UploadPolicy } from '../api/file-configuration-api.service';
import { unwrapApiResult } from '../models/api-response';
import { UserFacingError } from '../models/user-facing-error';

interface PolicyState {
  policy?: UploadPolicy;
  loadedAt?: number;
  loading?: boolean;
  error?: string;
}

export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} byte`;
  const divisor = bytes < 1024 * 1024 ? 1024 : 1024 * 1024;
  return `${Number((bytes / divisor).toFixed(2))} ${divisor === 1024 ? 'KB' : 'MB'}`;
}

@Injectable({ providedIn: 'root' })
export class UploadPolicyService {
  private readonly api = inject(FileConfigurationApiService);
  private readonly states = signal<Partial<Record<FilePurpose, PolicyState>>>({});
  private readonly inFlight = new Map<FilePurpose, Observable<UploadPolicy>>();
  private readonly ttlMs = 60_000;

  /** Called in the component injection context; refresh only while that screen is mounted. */
  watch(purposes: FilePurpose[]): this {
    const destroyRef = inject(DestroyRef);
    let timer: ReturnType<typeof setInterval> | undefined;
    afterNextRender(() => {
      purposes.forEach((purpose) => this.refresh(purpose));
      timer = setInterval(() => purposes.forEach((purpose) => this.refresh(purpose)), this.ttlMs);
    });
    destroyRef.onDestroy(() => { if (timer) clearInterval(timer); });
    return this;
  }

  ensure(purpose: FilePurpose, force = false): Observable<UploadPolicy> {
    const pending = this.inFlight.get(purpose);
    if (pending) return pending;
    const state = this.states()[purpose];
    if (!force && state?.policy && !state.error && Date.now() - (state.loadedAt ?? 0) < this.ttlMs) {
      return of(state.policy);
    }
    this.setState(purpose, { ...state, loading: true, error: undefined });
    const request = this.api.getUploadPolicy(purpose).pipe(
      map((response) => {
        const policy = unwrapApiResult<UploadPolicy>(response);
        if (!policy || policy.purpose !== purpose || !Number.isSafeInteger(policy.maxSizeBytes)
          || (policy.maxSizeBytes ?? 0) <= 0 || !Array.isArray(policy.allowedContentTypes)
          || !Array.isArray(policy.allowedExtensions)) {
          throw new Error('Cấu hình tải tệp chưa đầy đủ.');
        }
        return policy;
      }),
      tap((policy) => this.setState(purpose, { policy, loadedAt: Date.now() })),
      catchError(() => {
        const error = 'Không thể tải giới hạn tệp. Vui lòng thử lại.';
        this.setState(purpose, { error });
        return throwError(() => new UserFacingError(error));
      }),
      finalize(() => this.inFlight.delete(purpose)),
      shareReplay({ bufferSize: 1, refCount: false }),
    );
    this.inFlight.set(purpose, request);
    return request;
  }

  refresh(purpose: FilePurpose): void {
    this.ensure(purpose, true).subscribe({ error: () => {} });
  }

  invalidate(purpose: FilePurpose): void {
    this.setState(purpose, {});
  }

  error(purpose: FilePurpose): string | undefined {
    return this.states()[purpose]?.error;
  }

  unavailable(purpose: FilePurpose): boolean {
    const state = this.states()[purpose];
    return !state?.policy || !!state.error || state.policy.allowedContentTypes?.length === 0;
  }

  acceptsType(file: File, purpose: FilePurpose): boolean {
    const policy = this.states()[purpose]?.policy;
    if (!policy) return false;
    // file-service normalizes ONNX to binary regardless of the browser MIME type.
    return purpose === 'MODEL_ARTIFACT'
      ? file.name.toLowerCase().endsWith('.onnx') && !!policy.allowedContentTypes?.includes('application/octet-stream')
      : !!policy.allowedContentTypes?.includes(file.type.toLowerCase());
  }

  accept(purpose: FilePurpose): string {
    const policy = this.states()[purpose]?.policy;
    if (!policy) return '';
    if (purpose === 'MODEL_ARTIFACT') return (policy.allowedExtensions ?? []).map((ext) => '.' + ext).join(',');
    return [...(policy.allowedContentTypes ?? []), ...(policy.allowedExtensions ?? []).map((ext) => '.' + ext)].join(',');
  }

  hint(purpose: FilePurpose): string {
    const state = this.states()[purpose];
    if (state?.error) return state.error;
    if (!state?.policy) return 'Đang tải giới hạn tệp...';
    if (!state.policy.allowedContentTypes?.length) return 'Đã tắt tải tệp cho mục đích này.';
    const types = state.policy.allowedExtensions?.map((ext) => ext.toUpperCase()).join(', ');
    return `${types} · tối đa ${formatFileSize(state.policy.maxSizeBytes!)}/tệp`;
  }

  validate(file: File, purpose: FilePurpose): string | null {
    if (this.unavailable(purpose)) return this.hint(purpose);
    if (file.size <= 0) return 'Tệp tải lên đang trống.';
    if (!this.acceptsType(file, purpose)) return `Định dạng tệp không được chấp nhận. ${this.hint(purpose)}`;
    const max = this.states()[purpose]!.policy!.maxSizeBytes!;
    return file.size > max ? `Tệp không được vượt quá ${formatFileSize(max)}.` : null;
  }

  private setState(purpose: FilePurpose, state: PolicyState): void {
    this.states.update((states) => ({ ...states, [purpose]: state }));
  }
}
