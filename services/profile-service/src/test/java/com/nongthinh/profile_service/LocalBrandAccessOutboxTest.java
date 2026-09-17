package com.nongthinh.profile_service;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named = "brand.local.integration", matches = "true")
class LocalBrandAccessOutboxTest {
    @Test
    void migrationBackfillsExistingBrandsAndCapturesOnlyCommittedStatusChanges() {
        // Dedicated local test cluster; never use the application's configured datasource.
        var data = new DriverManagerDataSource("jdbc:postgresql://127.0.0.1:55441/postgres", "postgres", "");
        String schema = "brand_access_test_" + UUID.randomUUID().toString().replace("-", "");
        Flyway.configure().dataSource(data).schemas(schema).defaultSchema(schema).target("9").load().migrate();
        var scoped = new DriverManagerDataSource(
                "jdbc:postgresql://127.0.0.1:55441/postgres?currentSchema=" + schema, "postgres", "");
        var jdbc = new JdbcTemplate(scoped);
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000123");
        jdbc.update("""
                INSERT INTO brand_profiles(id,user_id,brand_name,phone,representative_name,representative_phone,
                    representative_email,status,created_at,updated_at)
                VALUES (?,?,'Brand','0900000000','Representative','0900000000','test@example.invalid',
                    'PENDING_APPROVAL',now(),now())
                """, id, id);
        Flyway.configure().dataSource(data).schemas(schema).defaultSchema(schema).load().migrate();
        assertEquals(1L, count(jdbc));
        assertEquals("PENDING_APPROVAL", jdbc.queryForObject("SELECT status FROM brand_access_outbox", String.class));

        jdbc.update("UPDATE brand_profiles SET brand_name='Updated', status='PENDING_APPROVAL' WHERE id=?", id);
        assertEquals(1L, count(jdbc));

        var tx = new TransactionTemplate(new DataSourceTransactionManager(scoped));
        tx.executeWithoutResult(transaction -> {
            jdbc.update("UPDATE brand_profiles SET status='ACTIVE' WHERE id=?", id);
            assertEquals(2L, count(jdbc));
            transaction.setRollbackOnly();
        });
        assertEquals(1L, count(jdbc));
        assertEquals("PENDING_APPROVAL", jdbc.queryForObject("SELECT status FROM brand_profiles WHERE id=?", String.class, id));

        jdbc.update("UPDATE brand_profiles SET status='ACTIVE' WHERE id=?", id);
        jdbc.update("UPDATE brand_profiles SET status='REJECTED' WHERE id=?", id);
        assertEquals(3L, count(jdbc));
        assertEquals(1L, jdbc.queryForObject(
                "SELECT count(*) FROM brand_access_outbox WHERE status='REJECTED' AND published_at IS NULL", Long.class));

        UUID next = UUID.fromString("00000000-0000-0000-0000-000000000124");
        jdbc.update("""
                INSERT INTO brand_profiles(id,user_id,brand_name,phone,representative_name,representative_phone,
                    representative_email,status,created_at,updated_at)
                VALUES (?,?,'New','0900000000','Representative','0900000000','new@example.invalid',
                    'PENDING_APPROVAL',now(),now())
                """, next, next);
        assertEquals(4L, count(jdbc));
    }

    private long count(JdbcTemplate jdbc) {
        return jdbc.queryForObject("SELECT count(*) FROM brand_access_outbox", Long.class);
    }
}
