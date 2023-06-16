package ariefbelajarjava.resilience4j;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class DecoratorsTest {

    private final AtomicLong counter = new AtomicLong(0L);

    @SneakyThrows
    public void slow(){
        long value = counter.incrementAndGet();
        log.info("Slow : " + value);
        Thread.sleep(  2500L);
        throw new IllegalArgumentException("Error");
    }
    @Test
    void testDecorators() throws InterruptedException {
        RateLimiter rateLimiter1 = RateLimiter.of("arief-ratelimiter", RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .build());

        Retry retry1 = Retry.of("arief-retry", RetryConfig.custom()
                .maxAttempts(10)
                .waitDuration(Duration.ofMillis(10))
                .build());

        Runnable runnable = Decorators.ofRunnable(() -> slow())
                .withRetry(retry1)
                .withRateLimiter(rateLimiter1)
                .decorate();

        for (int i = 0; i < 20; i++) {
            new Thread(runnable).start();
        }

        Thread.sleep(10_000);
    }
}
