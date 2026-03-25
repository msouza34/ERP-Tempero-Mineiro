package com.temperomineiro.erp.service;

import com.temperomineiro.erp.exception.BusinessException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final Clock clock;
    private final Map<String, LoginAttemptState> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void assertLoginAllowed(String email) {
        String normalizedEmail = normalize(email);
        LoginAttemptState state = attempts.get(normalizedEmail);
        if (state == null) {
            return;
        }

        if (state.blockedUntil().isAfter(now())) {
            throw new BusinessException("Muitas tentativas de login. Aguarde 15 minutos e tente novamente.");
        }

        if (isBlockedState(state)) {
            attempts.remove(normalizedEmail);
        }
    }

    public void registerFailure(String email) {
        String normalizedEmail = normalize(email);
        Instant currentTime = now();
        attempts.compute(normalizedEmail, (key, state) -> {
            if (state == null) {
                return new LoginAttemptState(1, Instant.EPOCH);
            }

            if (isBlockedState(state) && !state.blockedUntil().isAfter(currentTime)) {
                return new LoginAttemptState(1, Instant.EPOCH);
            }

            int failedAttempts = state.failedAttempts() + 1;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                return new LoginAttemptState(failedAttempts, currentTime.plus(LOCK_DURATION));
            }

            return new LoginAttemptState(failedAttempts, Instant.EPOCH);
        });
    }

    public void registerSuccess(String email) {
        attempts.remove(normalize(email));
    }

    public void resetAll() {
        attempts.clear();
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlockedState(LoginAttemptState state) {
        return state.blockedUntil().isAfter(Instant.EPOCH);
    }

    private record LoginAttemptState(int failedAttempts, Instant blockedUntil) {
    }
}
