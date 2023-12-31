package ariefbelajarjava.resilience4j;

import io.github.resilience4j.bulkhead.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Slf4j
public class BulkheadTest {

    private final AtomicLong counter = new AtomicLong(0L);

    @SneakyThrows
    public void slow(){
        long value = counter.incrementAndGet();
        log.info("Slow : " + value);
        Thread.sleep(2_000L);
    }

    @Test
    void testSemaphore() throws InterruptedException {
        Bulkhead bulkhead = Bulkhead.ofDefaults("arief");

        for (int i = 0; i < 1000; i++) {
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, () -> slow());
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }

    @Test //added on metric video segment
    void testSemaphoreWithMetric() throws InterruptedException {
        Bulkhead bulkhead = Bulkhead.ofDefaults("arief");

        int availableConcurrentCalls = bulkhead.getMetrics().getAvailableConcurrentCalls();
        int maxAllowedConcurrentCalls = bulkhead.getMetrics().getMaxAllowedConcurrentCalls();

        log.info(String.valueOf(availableConcurrentCalls));
        log.info(String.valueOf(maxAllowedConcurrentCalls));

        for (int i = 0; i < 1000; i++) {
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, () -> slow());
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }

    @Test
    void testThreadPool() {

        log.info(String.valueOf(Runtime.getRuntime().availableProcessors()));

        ThreadPoolBulkhead bulkhead = ThreadPoolBulkhead.ofDefaults("arief");

        for (int i = 0; i < 1000; i++) {
            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, () -> slow());
            supplier.get();
        }
    }

    @Test
    void testSemaphoreConfig() throws InterruptedException {
        log.info(String.valueOf(Runtime.getRuntime().availableProcessors()));

        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(9)
                .maxWaitDuration(Duration.ofSeconds(5))
                .build();

        Bulkhead bulkhead = Bulkhead.of("arief",config);

        for (int i = 0; i < 18; i++) {
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, () -> slow());
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }

    @Test
    void testThreadPoolConfig() throws InterruptedException {
        log.info(String.valueOf(Runtime.getRuntime().availableProcessors()));

        ThreadPoolBulkheadConfig config = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(5)
                .coreThreadPoolSize(5)
                .queueCapacity(20)
                .build();

        ThreadPoolBulkhead bulkhead = ThreadPoolBulkhead.of("arief",config);

        for (int i = 0; i < 20; i++) {
            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, () -> slow());
            supplier.get();
        }

        Thread.sleep(10_000L);
    }

    @Test
    void testSemaphoreRegistry() throws InterruptedException {
        log.info(String.valueOf(Runtime.getRuntime().availableProcessors()));

        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(9)
                .maxWaitDuration(Duration.ofSeconds(5))
                .build();

        BulkheadRegistry registry = BulkheadRegistry.ofDefaults();
        registry.addConfiguration("config",config);

        Bulkhead bulkhead = registry.bulkhead("arief","config");

        for (int i = 0; i < 18; i++) {
            Runnable runnable = Bulkhead.decorateRunnable(bulkhead, () -> slow());
            new Thread(runnable).start();
        }

        Thread.sleep(10_000L);
    }

    @Test
    void testThreadPoolRegistry() throws InterruptedException {
        log.info(String.valueOf(Runtime.getRuntime().availableProcessors()));

        ThreadPoolBulkheadConfig config = ThreadPoolBulkheadConfig.custom()
                .maxThreadPoolSize(5)
                .coreThreadPoolSize(5)
                .queueCapacity(20)
                .build();

        ThreadPoolBulkheadRegistry registry = ThreadPoolBulkheadRegistry.ofDefaults();
        registry.addConfiguration("config",config);

        ThreadPoolBulkhead bulkhead = registry.bulkhead("arief","config");

        for (int i = 0; i < 20; i++) {
            Supplier<CompletionStage<Void>> supplier = ThreadPoolBulkhead.decorateRunnable(bulkhead, () -> slow());
            supplier.get();
        }

        Thread.sleep(10_000L);
    }

}
