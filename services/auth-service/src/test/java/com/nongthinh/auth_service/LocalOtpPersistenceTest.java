package com.nongthinh.auth_service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.nongthinh.auth_service.application.command.ResendEmailOtpCommand;
import com.nongthinh.auth_service.application.command.VerifyEmailOtpCommand;
import com.nongthinh.auth_service.application.event.RegisterOtpRequest;
import com.nongthinh.auth_service.application.port.in.otp.ResendEmailOtpUseCase;
import com.nongthinh.auth_service.application.port.in.otp.VerifyEmailOtpUseCase;
import com.nongthinh.auth_service.application.port.in.otp.impl.ResendEmailOtpUseCaseImpl;
import com.nongthinh.auth_service.application.port.in.otp.impl.VerifyEmailOtpUseCaseImpl;
import com.nongthinh.auth_service.application.port.out.ClockProvider;
import com.nongthinh.auth_service.application.port.out.EventPublisher;
import com.nongthinh.auth_service.application.port.out.IdGenerator;
import com.nongthinh.auth_service.application.port.out.SystemParam;
import com.nongthinh.auth_service.application.port.out.otp.OtpGenerator;
import com.nongthinh.auth_service.application.port.out.otp.OtpHash;
import com.nongthinh.auth_service.application.port.out.repository.OtpRepository;
import com.nongthinh.auth_service.application.port.out.repository.UserRepository;
import com.nongthinh.auth_service.application.service.EmailOtpIssuer;
import com.nongthinh.auth_service.common.exception.ErrorCode;
import com.nongthinh.auth_service.domain.exception.BusinessException;
import com.nongthinh.auth_service.domain.otp.EmailOtp;
import com.nongthinh.auth_service.domain.user.User;
import com.nongthinh.auth_service.domain.user.valueobject.Email;
import com.nongthinh.auth_service.infra.exception.InfrastructureException;
import com.nongthinh.auth_service.infra.persistence.otp.*;
import com.nongthinh.auth_service.infra.persistence.user.*;
import com.nongthinh.auth_service.infra.security.OtpHashImpl;
import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@EnabledIfSystemProperty(named = "auth.otp.local.integration", matches = "true")
@SpringJUnitConfig(LocalOtpPersistenceTest.Config.class)
class LocalOtpPersistenceTest {
    private static final Instant NOW = Instant.parse("2026-09-14T00:00:00Z");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String EMAIL = "otp@example.com";

    @Autowired private UserRepository users;
    @Autowired private OtpRepository store;
    @Autowired private EmailOtpIssuer issuer;
    @Autowired private VerifyEmailOtpUseCase verifyOtp;
    @Autowired private ResendEmailOtpUseCase resendOtp;
    @Autowired private ClockProvider clock;
    @Autowired private EventPublisher events;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactions;

    @BeforeEach
    void prepare() {
        reset(store, events, clock);
        when(clock.now()).thenReturn(NOW);
        jdbc.update("delete from email_otps");
        jdbc.update("delete from users");
        users.save(User.createPendingEmailVerification(USER_ID, null, Email.of(EMAIL), "password-hash", true, NOW));
    }

    @Test
    void initialIssuancePersistsHashAndPublishesOnlyAfterCommit() {
        doAnswer(invocation -> {
            // OTP state is available when the post-commit callback sends the event.
            assertThat(jdbc.queryForObject("select count(*) from email_otps", Integer.class)).isEqualTo(1);
            return null;
        }).when(events).publish(any(RegisterOtpRequest.class));
        issuer.issueInitial(USER_ID, EMAIL, "Farmer");
        EmailOtp otp = store.findByEmail(EMAIL).orElseThrow();
        assertThat(otp.getOtpHash()).isNotEqualTo("123456");
        assertThat(otp.getExpiresAt()).isAfter(NOW);
        verify(events).publish(any(RegisterOtpRequest.class));
        assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success = true", Integer.class))
                .isEqualTo(2);
    }

    @Test
    void failedAttemptCommitsDespiteBusinessErrorWithoutExtendingExpiry() {
        issuer.issueInitial(USER_ID, EMAIL, "Farmer");
        Instant expiry = store.findByEmail(EMAIL).orElseThrow().getExpiresAt();
        assertBusiness(() -> verifyOtp.execute(new VerifyEmailOtpCommand(EMAIL, "wrong")), ErrorCode.OTP_INVALID);
        EmailOtp otp = store.findByEmail(EMAIL).orElseThrow();
        assertThat(otp.getAttemptCount()).isEqualTo(1);
        assertThat(otp.getExpiresAt()).isEqualTo(expiry);
        assertThat(users.findByEmail(EMAIL).orElseThrow().isEmailVerified()).isFalse();
    }

    @Test
    void verificationCommitsUserAndConsumedOtpTogether() {
        issuer.issueInitial(USER_ID, EMAIL, "Farmer");
        verifyOtp.execute(new VerifyEmailOtpCommand(EMAIL, "123456"));
        assertThat(users.findByEmail(EMAIL).orElseThrow().isEmailVerified()).isTrue();
        assertThat(store.findByEmail(EMAIL).orElseThrow().getConsumedAt()).isEqualTo(NOW);
        assertBusiness(() -> verifyOtp.execute(new VerifyEmailOtpCommand(EMAIL, "123456")), ErrorCode.EMAIL_ALREADY_VERIFIED);
    }

    @Test
    void persistenceFailureRollsBackUserVerification() {
        issuer.issueInitial(USER_ID, EMAIL, "Farmer");
        doThrow(new InfrastructureException(ErrorCode.OTP_PERSISTENCE_FAILED)).when(store).save(any());
        assertThatThrownBy(() -> verifyOtp.execute(new VerifyEmailOtpCommand(EMAIL, "123456")))
                .isInstanceOf(InfrastructureException.class);
        assertThat(users.findByEmail(EMAIL).orElseThrow().isEmailVerified()).isFalse();
        assertThat(store.findByEmail(EMAIL).orElseThrow().getConsumedAt()).isNull();
    }

    @Test
    void rolledBackIssuanceDoesNotSendEmail() {
        TransactionTemplate tx = new TransactionTemplate(transactions);
        tx.executeWithoutResult(status -> {
            issuer.issueInitial(USER_ID, EMAIL, "Farmer");
            status.setRollbackOnly();
        });
        assertThat(store.findByEmail(EMAIL)).isEmpty();
        verifyNoInteractions(events);
    }

    @Test
    void resendEnforcesPersistedCooldownAndRestartsExpiredChallenge() {
        issuer.issueInitial(USER_ID, EMAIL, "Farmer");
        EmailOtp initial = store.findByEmail(EMAIL).orElseThrow();
        assertBusiness(() -> resendOtp.execute(new ResendEmailOtpCommand(EMAIL)), ErrorCode.OTP_RESEND_TOO_FREQUENT);
        when(clock.now()).thenReturn(initial.getExpiresAt());
        resendOtp.execute(new ResendEmailOtpCommand(EMAIL));
        EmailOtp renewed = store.findByEmail(EMAIL).orElseThrow();
        assertThat(renewed.getId()).isEqualTo(initial.getId());
        assertThat(renewed.getResendCount()).isZero();
        assertThat(renewed.getExpiresAt()).isAfter(initial.getExpiresAt());
    }

    @Test
    void concurrentVerificationConsumesOtpOnlyOnce() throws Exception {
        issuer.issueInitial(USER_ID, EMAIL, "Farmer");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var task = (java.util.concurrent.Callable<String>) () -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Concurrent test did not start");
                }
                try {
                    verifyOtp.execute(new VerifyEmailOtpCommand(EMAIL, "123456"));
                    return "verified";
                } catch (BusinessException ex) {
                    return ex.getErrorCode().name();
                }
            };
            var first = executor.submit(task);
            var second = executor.submit(task);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(java.util.List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("verified", "EMAIL_ALREADY_VERIFIED");
        }
    }

    @Test
    void concurrentFirstResendsCreateOnlyOneChallenge() throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            var task = (java.util.concurrent.Callable<String>) () -> {
                if (!start.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Concurrent test did not start");
                }
                try {
                    resendOtp.execute(new ResendEmailOtpCommand(EMAIL));
                    return "sent";
                } catch (BusinessException ex) {
                    return ex.getErrorCode().name();
                }
            };
            var first = executor.submit(task);
            var second = executor.submit(task);
            start.countDown();
            assertThat(java.util.List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("sent", "OTP_RESEND_TOO_FREQUENT");
            assertThat(jdbc.queryForObject("select count(*) from email_otps", Integer.class)).isEqualTo(1);
            verify(events).publish(any(RegisterOtpRequest.class));
        }
    }

    private void assertBusiness(Runnable action, ErrorCode code) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(BusinessException.class,
                ex -> assertThat(ex.getErrorCode()).isEqualTo(code));
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {JpaOtpRepository.class, JpaUserRepository.class})
    @Import({OtpPersistenceMapper.class, UserRepositoryImpl.class,
            UserPersistenceMapper.class, EmailOtpIssuer.class, VerifyEmailOtpUseCaseImpl.class,
            ResendEmailOtpUseCaseImpl.class})
    static class Config {
        @Bean DataSource dataSource() {
            // Explicit opt-in, isolated local database; never use application-dev credentials.
            return new DriverManagerDataSource("jdbc:postgresql://127.0.0.1:55439/otp_test", "postgres", "");
        }

        @Bean(initMethod = "migrate") Flyway flyway(DataSource dataSource) {
            return Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();
        }

        @Bean
        @DependsOn("flyway")
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.nongthinh.auth_service.infra.persistence");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "validate", "hibernate.jdbc.time_zone", "UTC"));
            return factory;
        }

        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
        @Bean JdbcTemplate jdbcTemplate(DataSource dataSource) { return new JdbcTemplate(dataSource); }
        @Bean ClockProvider clockProvider() { return mock(ClockProvider.class); }
        @Bean EventPublisher eventPublisher() { return mock(EventPublisher.class); }
        @Bean IdGenerator idGenerator() {
            AtomicLong sequence = new AtomicLong(10);
            return () -> new UUID(0, sequence.incrementAndGet());
        }
        @Bean OtpGenerator otpGenerator() { return length -> "123456"; }
        @Bean OtpHash otpHash() { return new OtpHashImpl(new BCryptPasswordEncoder(4)); }
        @Bean SystemParam systemParam() {
            SystemParam params = mock(SystemParam.class);
            when(params.getInt(anyString(), anyInt())).thenAnswer(call -> call.getArgument(1));
            return params;
        }
        @Bean OtpRepository otpRepository(JpaOtpRepository repository, OtpPersistenceMapper mapper) {
            return spy(new OtpRepositoryImpl(repository, mapper));
        }
    }
}
