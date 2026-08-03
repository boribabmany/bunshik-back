package com.bunshik.admin.service;

import com.bunshik.admin.security.LoginAttemptLimitException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AdminLoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(10);
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final ConcurrentHashMap<String, AttemptState> attempts =
            new ConcurrentHashMap<>();
    private final Clock clock;

    public AdminLoginAttemptService() {
        this(Clock.systemUTC());
    }

    AdminLoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void ensureLoginAllowed(String username, String clientIp) {
        String key = createKey(username, clientIp);
        AttemptState state = attempts.get(key);

        if (state == null) {
            return;
        }

        Instant now = clock.instant();
        if (!now.isBefore(state.expiresAt())) {
            attempts.remove(key, state);
            return;
        }

        if (state.failedAttempts() >= MAX_FAILED_ATTEMPTS) {
            throw new LoginAttemptLimitException();
        }
    }

    public boolean recordFailure(String username, String clientIp) {
        String key = createKey(username, clientIp);
        Instant now = clock.instant();
        AtomicBoolean blocked = new AtomicBoolean(false);

        makeRoomFor(key, now);
        attempts.compute(key, (ignored, current) -> {
            int previousFailures = current == null
                    || !now.isBefore(current.expiresAt())
                    ? 0
                    : current.failedAttempts();
            int failedAttempts = previousFailures + 1;
            blocked.set(failedAttempts >= MAX_FAILED_ATTEMPTS);
            return new AttemptState(
                    failedAttempts,
                    now.plus(BLOCK_DURATION)
            );
        });

        return blocked.get();
    }

    public void recordSuccess(String username, String clientIp) {
        attempts.remove(createKey(username, clientIp));
    }

    private void makeRoomFor(String key, Instant now) {
        if (attempts.size() < MAX_TRACKED_KEYS || attempts.containsKey(key)) {
            return;
        }

        attempts.entrySet().removeIf(entry ->
                !now.isBefore(entry.getValue().expiresAt())
        );

        if (attempts.size() >= MAX_TRACKED_KEYS) {
            attempts.keySet().stream().findFirst().ifPresent(attempts::remove);
        }
    }

    private String createKey(String username, String clientIp) {
        String normalizedUsername = username == null
                ? ""
                : username.trim().toLowerCase(Locale.ROOT);
        String normalizedIp = clientIp == null || clientIp.isBlank()
                ? "unknown"
                : clientIp.trim();
        return normalizedUsername + "|" + normalizedIp;
    }

    private record AttemptState(int failedAttempts, Instant expiresAt) {
    }
}
