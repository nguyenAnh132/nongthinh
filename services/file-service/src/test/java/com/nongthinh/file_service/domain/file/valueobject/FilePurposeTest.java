package com.nongthinh.file_service.domain.file.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;

class FilePurposeTest {

    @Test
    void parsesPurposeCaseInsensitively() {
        assertEquals(FilePurpose.DIAGNOSIS_IMAGE, FilePurpose.from("diagnosis_image"));
        assertEquals(FilePurpose.POST_IMAGE, FilePurpose.from(" post_image "));
        assertEquals(FilePurpose.POST_VIDEO, FilePurpose.from("post_video"));
    }

    @Test
    void reportsInvalidPurposeAsValidationError() {
        BusinessException exception = assertThrows(BusinessException.class, () -> FilePurpose.from("unknown"));

        assertEquals(ErrorCode.PURPOSE_INVALID, exception.getErrorCode());
    }
}
