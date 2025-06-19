package br.com.wslima.javaspringbootrestapi.config.security.rest.service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPTS = 5;
    private final long BLOCK_TIME_MS = TimeUnit.MINUTES.toMillis(15);

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockTime = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        if (blockTime.containsKey(ip)) {
            long blockExpiresAt = blockTime.get(ip);
            if (System.currentTimeMillis() > blockExpiresAt) {
                blockTime.remove(ip);
                attempts.remove(ip);
                return false;
            }
            return true;
        }
        return false;
    }

    public void loginFailed(String ip) {
        int currentAttempts = attempts.getOrDefault(ip, 0) + 1;
        attempts.put(ip, currentAttempts);

        if (currentAttempts >= MAX_ATTEMPTS) {
            blockTime.put(ip, System.currentTimeMillis() + BLOCK_TIME_MS);
        }
    }

    public void loginSucceeded(String ip) {
        attempts.remove(ip);
        blockTime.remove(ip);
    }

    @Scheduled(fixedDelay = 60_000) // A cada 1 minuto
    public void cleanUp() {
        long now = System.currentTimeMillis();
        blockTime.entrySet().removeIf(entry -> now > entry.getValue());
    }
}
