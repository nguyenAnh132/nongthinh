package com.nongthinh.file_service.domain.file;

import java.util.Set;
import com.nongthinh.file_service.common.constant.RoleConstant;
import com.nongthinh.file_service.common.currentuser.CurrentUser;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.nongthinh.file_service.domain.file.valueobject.FilePurpose;

public final class FileAccessPolicy {

    private static final Set<String> AVATAR_ROLES = Set.of(
            RoleConstant.ROLE_FARMER,
            RoleConstant.ROLE_ADMIN);
    private static final Set<String> DIAGNOSIS_IMAGE_ROLES = Set.of(
            RoleConstant.ROLE_FARMER,
            RoleConstant.ROLE_BRAND);
    private static final Set<String> POST_MEDIA_ROLES = Set.of(
            RoleConstant.ROLE_FARMER,
            RoleConstant.ROLE_BRAND);
    private static final Set<String> MODEL_ARTIFACT_ROLES = Set.of(RoleConstant.ROLE_ADMIN);
    private static final Set<String> BRAND_FILE_ROLES = Set.of(RoleConstant.ROLE_BRAND, RoleConstant.ROLE_BRAND_PENDING);
    private static final Set<String> CATALOG_IMAGE_ROLES = Set.of(
            RoleConstant.ROLE_BRAND,
            RoleConstant.ROLE_ADMIN);
    private static final Set<String> BUSINESS_LICENSE_REVIEW_PERMISSIONS = Set.of(
            "brand:verify",
            "brand:approve");

    private FileAccessPolicy() {
    }

    public static void assertCanUpload(CurrentUser user, FilePurpose purpose) {
        switch (purpose) {
            case AVATAR -> {
                if (!hasAnyRole(user, AVATAR_ROLES)) {
                    throw new BusinessException(ErrorCode.FILE_PURPOSE_NOT_ALLOWED);
                }
            }
            case BRAND_LOGO, BRAND_BANNER, BUSINESS_LICENSE -> {
                if (!hasAnyRole(user, BRAND_FILE_ROLES)) {
                    throw new BusinessException(ErrorCode.FILE_PURPOSE_NOT_ALLOWED);
                }
            }
            case PRODUCT_IMAGE, DISEASE_IMAGE -> {
                if (!hasAnyRole(user, CATALOG_IMAGE_ROLES)) {
                    throw new BusinessException(ErrorCode.FILE_PURPOSE_NOT_ALLOWED);
                }
            }
            case DIAGNOSIS_IMAGE -> {
                if (!hasAnyRole(user, DIAGNOSIS_IMAGE_ROLES)) {
                    throw new BusinessException(ErrorCode.FILE_PURPOSE_NOT_ALLOWED);
                }
            }
            case POST_IMAGE, POST_VIDEO -> {
                if (!hasAnyRole(user, POST_MEDIA_ROLES)) {
                    throw new BusinessException(ErrorCode.FILE_PURPOSE_NOT_ALLOWED);
                }
            }
            case MODEL_ARTIFACT -> {
                if (!hasAnyRole(user, MODEL_ARTIFACT_ROLES)) {
                    throw new BusinessException(ErrorCode.FILE_PURPOSE_NOT_ALLOWED);
                }
            }
        }
    }

    public static void assertCanRead(CurrentUser user, StoredFile file) {
        if (file.isOwnedBy(user.getUserId())) {
            return;
        }
        if (file.getPurpose() == FilePurpose.BUSINESS_LICENSE
                && hasAnyPermission(user, BUSINESS_LICENSE_REVIEW_PERMISSIONS)) {
            return;
        }
        throw new BusinessException(ErrorCode.FILE_ACCESS_DENIED);
    }

    public static void assertCanDelete(CurrentUser user, StoredFile file) {
        if (!file.isOwnedBy(user.getUserId())) {
            throw new BusinessException(ErrorCode.FILE_ACCESS_DENIED);
        }
        if (file.getPurpose() == FilePurpose.DIAGNOSIS_IMAGE
                || file.getPurpose() == FilePurpose.MODEL_ARTIFACT) {
            throw new BusinessException(ErrorCode.FILE_MANAGED_LIFECYCLE);
        }
    }

    private static boolean hasAnyRole(CurrentUser user, Set<String> roles) {
        return user.getRoles().stream().anyMatch(roles::contains);
    }

    private static boolean hasAnyPermission(CurrentUser user, Set<String> permissions) {
        return user.getPermissions().stream().anyMatch(permissions::contains);
    }
}
