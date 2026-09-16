import { Component, inject, input } from '@angular/core';
import { FilePurpose } from '../../core/api/file-api.service';
import { UploadPolicyService } from '../../core/service/upload-policy.service';

@Component({
  selector: 'app-upload-policy-hint',
  standalone: true,
  host: { '[style.display]': "policies.unavailable(purpose()) ? 'block' : 'none'" },
  template: `
    @if (policies.unavailable(purpose())) {
      <span aria-live="polite">{{ policies.hint(purpose()) }}</span>
      @if (policies.error(purpose())) {
        <button type="button" (click)="$event.preventDefault(); $event.stopPropagation(); policies.refresh(purpose())">Thử lại</button>
      }
    }
  `,
  styles: [`
    :host { display: block; color: #6f7a6d; font-size: 12px; line-height: 1.6; margin-top: 6px; }
    button { border: 0; background: transparent; color: #006b27; text-decoration: underline; cursor: pointer; }
  `],
})
export class UploadPolicyHint {
  readonly purpose = input.required<FilePurpose>();
  readonly policies = inject(UploadPolicyService);
}
