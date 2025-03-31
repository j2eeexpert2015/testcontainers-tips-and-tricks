package com.example.waitstrategies;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

/**
 * CombinedWaitStrategy allows chaining multiple WaitStrategies and executes them sequentially.
 * It logs the execution time of each strategy, logs failure reasons, and prints the total time taken.
 * If any strategy fails, the execution stops and logs how long it took before failing.
 */
public class CombinedWaitStrategy extends AbstractWaitStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CombinedWaitStrategy.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(120);

    // List of WaitStrategies to be executed sequentially
    private final List<WaitStrategy> strategies;

    /**
     * Private constructor to initialize the list of strategies.
     * @param strategies List of WaitStrategy instances
     */
    private CombinedWaitStrategy(List<WaitStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * Factory method to create a CombinedWaitStrategy instance.
     * @param strategies Varargs of WaitStrategy instances
     * @return CombinedWaitStrategy instance
     */
    public static CombinedWaitStrategy of(WaitStrategy... strategies) {
        return new CombinedWaitStrategy(Arrays.asList(strategies));
    }

    /**
     * Allows overriding the default startup timeout for all strategies.
     * @param timeout Duration to apply
     * @return CombinedWaitStrategy instance
     */
    public CombinedWaitStrategy withStartupTimeout(Duration timeout) {
        this.startupTimeout = timeout;
        return this;
    }

    /**
     * Executes all configured WaitStrategies sequentially.
     * Logs time taken by each strategy and total duration.
     * If any strategy fails, logs failure and time until failure.
     */
    @Override
    protected void waitUntilReady() {
        Duration timeoutToUse = (this.startupTimeout != null) ? this.startupTimeout : DEFAULT_TIMEOUT;
        logger.info("CombinedWaitStrategy started with timeout: {} seconds", timeoutToUse.getSeconds());

        long overallStart = System.nanoTime();

        try {
            for (WaitStrategy strategy : strategies) {
                long startTime = System.nanoTime();

                // Apply timeout if strategy supports it
                if (strategy instanceof AbstractWaitStrategy) {
                    ((AbstractWaitStrategy) strategy).withStartupTimeout(timeoutToUse);
                }

                logger.info("Executing WaitStrategy: {}", strategy.getClass().getSimpleName());
                strategy.waitUntilReady(this.waitStrategyTarget);

                long endTime = System.nanoTime();
                long durationMs = (endTime - startTime) / 1_000_000;
                logger.info("WaitStrategy {} passed in {} ms", strategy.getClass().getSimpleName(), durationMs);
            }

            long overallEnd = System.nanoTime();
            long totalDurationMs = (overallEnd - overallStart) / 1_000_000;
            logger.info("CombinedWaitStrategy completed in {} ms", totalDurationMs);

        } catch (Exception e) {
            long failureTime = System.nanoTime();
            long durationMs = (failureTime - overallStart) / 1_000_000;
            logger.error("CombinedWaitStrategy failed after {} ms", durationMs);
            throw e;
        }
    }
}