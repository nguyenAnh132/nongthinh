package com.nongthinh.brand_service.infra.camunda;

public final class CamundaProcessConstants {

    public static final String PROCESS_DEFINITION_KEY = "brand-approval";
    public static final String PHONE_VERIFICATION_TASK_KEY = "phone-verification";
    public static final String DOCUMENTS_REVIEW_TASK_KEY = "documents-review";
    public static final String FINAL_DECISION_TASK_KEY = "final-decision";
    public static final String TICKET_CANDIDATE_GROUP = "ticket:manage";

    public static final String BRAND_PROFILE_ID_VARIABLE = "brandProfileId";
    public static final String USER_ID_VARIABLE = "userId";
    public static final String BRAND_NAME_VARIABLE = "brandName";
    public static final String VERIFICATION_RESULT_VARIABLE = "verificationResult";
    public static final String VERIFICATION_NOTE_VARIABLE = "verificationNote";
    public static final String REVIEWER_ID_VARIABLE = "reviewerId";
    public static final String REVISION_REASON_VARIABLE = "revisionReason";
    public static final String REJECTION_REASON_VARIABLE = "rejectionReason";
    public static final String OUTCOME_VARIABLE = "outcome";
    public static final String DOCUMENTS_OK_VARIABLE = "documentsOk";
    public static final String DOCUMENTS_SUBMITTED_MESSAGE = "documents_submitted";

    private CamundaProcessConstants() {
    }
}
