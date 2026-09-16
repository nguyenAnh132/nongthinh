import { of } from 'rxjs';
import type { FilePurpose } from '../api/file-api.service';
import { FileConfigurationApiService, UploadPolicy } from '../api/file-configuration-api.service';

/** BO fixtures for component tests; production constraints are always fetched from BO. */
export function uploadPolicyFixture(purpose: FilePurpose): UploadPolicy {
  const imageTypes = ['image/jpeg', 'image/png', 'image/webp'];
  const imageExtensions = ['jpg', 'png', 'webp'];
  const maxSizeBytes = ['AVATAR', 'BRAND_LOGO'].includes(purpose) ? 2097152
    : purpose === 'BUSINESS_LICENSE' ? 10485760
    : ['POST_VIDEO', 'MODEL_ARTIFACT'].includes(purpose) ? 52428800 : 5242880;
  return {
    purpose, maxSizeBytes,
    allowedContentTypes: purpose === 'MODEL_ARTIFACT' ? ['application/octet-stream']
      : purpose === 'POST_VIDEO' ? ['video/mp4', 'video/webm', 'video/quicktime']
      : purpose === 'BUSINESS_LICENSE' ? [...imageTypes, 'application/pdf'] : imageTypes,
    allowedExtensions: purpose === 'MODEL_ARTIFACT' ? ['onnx']
      : purpose === 'POST_VIDEO' ? ['mp4', 'webm', 'mov']
      : purpose === 'BUSINESS_LICENSE' ? [...imageExtensions, 'pdf'] : imageExtensions,
  };
}

export function provideUploadPolicyFixtures() {
  return {
    provide: FileConfigurationApiService,
    useValue: { getUploadPolicy: (purpose: FilePurpose) => of({ result: uploadPolicyFixture(purpose) }) },
  };
}
