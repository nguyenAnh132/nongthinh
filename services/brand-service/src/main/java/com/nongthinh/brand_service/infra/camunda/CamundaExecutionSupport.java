package com.nongthinh.brand_service.infra.camunda;

import java.util.UUID;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.domain.exception.BusinessException;

public final class CamundaExecutionSupport {

    private CamundaExecutionSupport() {
    }

    public static UUID requireBrandProfileId(DelegateExecution execution) {
        return UUID.fromString(requireStringVariable(execution, CamundaProcessConstants.BRAND_PROFILE_ID_VARIABLE));
    }

    public static UUID requireUserId(DelegateExecution execution) {
        return UUID.fromString(requireStringVariable(execution, CamundaProcessConstants.USER_ID_VARIABLE));
    }

    public static UUID resolveReviewerId(DelegateExecution execution) {
        Object reviewerId = execution.getVariable(CamundaProcessConstants.REVIEWER_ID_VARIABLE);
        if (reviewerId == null || reviewerId.toString().isBlank()) {
            return null;
        }
        return UUID.fromString(reviewerId.toString());
    }

    public static String resolveRevisionReason(DelegateExecution execution) {
        Object revisionReason = execution.getVariable(CamundaProcessConstants.REVISION_REASON_VARIABLE);
        if (revisionReason != null && !revisionReason.toString().isBlank()) {
            return revisionReason.toString().trim();
        }
        return "Please update your profile or re-upload the business license";
    }

    public static String resolveRejectionReason(DelegateExecution execution) {
        Object outcome = execution.getVariable(CamundaProcessConstants.OUTCOME_VARIABLE);
        if ("REJECTED".equals(outcome)) {
            Object rejectionReason = execution.getVariable(CamundaProcessConstants.REJECTION_REASON_VARIABLE);
            if (rejectionReason != null && !rejectionReason.toString().isBlank()) {
                return rejectionReason.toString().trim();
            }
            return "Brand approval rejected";
        }

        Object verificationNote = execution.getVariable(CamundaProcessConstants.VERIFICATION_NOTE_VARIABLE);
        if (verificationNote != null && !verificationNote.toString().isBlank()) {
            return verificationNote.toString().trim();
        }

        Object verificationResult = execution.getVariable(CamundaProcessConstants.VERIFICATION_RESULT_VARIABLE);
        if (verificationResult != null) {
            return "Phone verification failed: " + verificationResult;
        }

        return "Brand approval rejected";
    }

    private static String requireStringVariable(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (value == null || value.toString().isBlank()) {
            throw new BusinessException(ErrorCode.PROCESS_VARIABLE_MISSING);
        }
        return value.toString();
    }
}
