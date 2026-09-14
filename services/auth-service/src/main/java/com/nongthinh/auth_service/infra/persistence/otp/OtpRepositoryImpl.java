package com.nongthinh.auth_service.infra.persistence.otp;

import com.nongthinh.auth_service.application.port.out.repository.OtpRepository;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.otp.EmailOtp;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OtpRepositoryImpl implements OtpRepository {
    private final JpaOtpRepository jpaOtpRepository;
    private final OtpPersistenceMapper mapper;

    @Override
    public EmailOtp save(EmailOtp otp) {
        try {
            return mapper.toDomain(jpaOtpRepository.saveAndFlush(mapper.toEntity(otp)));
        } catch (DataAccessException ex) {
            throw new InfrastructureException(ErrorCode.OTP_PERSISTENCE_FAILED, ex);
        }
    }

    @Override
    public Optional<EmailOtp> findByEmail(String email) {
        try {
            return jpaOtpRepository.findByEmail(Email.normalize(email)).map(mapper::toDomain);
        } catch (DataAccessException ex) {
            throw new InfrastructureException(ErrorCode.OTP_PERSISTENCE_FAILED, ex);
        }
    }

    @Override
    public Optional<EmailOtp> findByEmailForUpdate(String email) {
        try {
            return jpaOtpRepository.findByEmailForUpdate(Email.normalize(email)).map(mapper::toDomain);
        } catch (DataAccessException ex) {
            throw new InfrastructureException(ErrorCode.OTP_PERSISTENCE_FAILED, ex);
        }
    }
}
