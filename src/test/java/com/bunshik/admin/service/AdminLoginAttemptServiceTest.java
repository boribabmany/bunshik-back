package com.bunshik.admin.service;

import com.bunshik.admin.security.LoginAttemptLimitException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminLoginAttemptServiceTest {

    @Test
    void blocksSameUsernameAndIpAfterFiveFailures() {
        MutableClock clock = new MutableClock(
                Instant.parse("2026-08-03T00:00:00Z")
        );
        AdminLoginAttemptService service = new AdminLoginAttemptService(clock);

        for (int attempt = 1; attempt <= 4; attempt++) {
            assertThat(service.recordFailure("admin", "127.0.0.1")).isFalse();
        }
        assertThat(service.recordFailure("admin", "127.0.0.1")).isTrue();

        assertThatThrownBy(() ->
                service.ensureLoginAllowed("admin", "127.0.0.1")
        )
                .isInstanceOf(LoginAttemptLimitException.class)
                .hasMessage("로그인에 5회 실패하여 10분간 로그인이 제한됩니다.");
    }

    @Test
    void allowsLoginAgainAfterTenMinutes() {
        MutableClock clock = new MutableClock(
                Instant.parse("2026-08-03T00:00:00Z")
        );
        AdminLoginAttemptService service = new AdminLoginAttemptService(clock);

        for (int attempt = 1; attempt <= 5; attempt++) {
            service.recordFailure("admin", "127.0.0.1");
        }
        clock.advance(Duration.ofMinutes(10));

        assertThatCode(() ->
                service.ensureLoginAllowed("admin", "127.0.0.1")
        ).doesNotThrowAnyException();
    }

    @Test
    void successfulLoginClearsFailures() {
        AdminLoginAttemptService service = new AdminLoginAttemptService();
        for (int attempt = 1; attempt <= 4; attempt++) {
            service.recordFailure("admin", "127.0.0.1");
        }

        service.recordSuccess("admin", "127.0.0.1");

        assertThat(service.recordFailure("admin", "127.0.0.1")).isFalse();
    }

    @Test
    void tracksDifferentIpSeparately() {
        AdminLoginAttemptService service = new AdminLoginAttemptService();
        for (int attempt = 1; attempt <= 5; attempt++) {
            service.recordFailure("admin", "127.0.0.1");
        }

        assertThatCode(() ->
                service.ensureLoginAllowed("admin", "127.0.0.2")
        ).doesNotThrowAnyException();
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
