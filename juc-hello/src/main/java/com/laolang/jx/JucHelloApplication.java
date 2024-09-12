package com.laolang.jx;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JucHelloApplication {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        log.info("juc hello is running...");

        ExecutorService executorService = Executors.newFixedThreadPool(2, new SimplThreadFactory("call-"));

        // 计算奇数
        CompletableFuture.supplyAsync(() -> new SimpleCallable(NumberType.ODD).call(), executorService)
                .whenComplete((integer, throwable) -> log.info("奇数之和:{}", integer));
        // 计算偶数
        CompletableFuture.supplyAsync(() -> new SimpleCallable(NumberType.EVEN).call(), executorService)
                .whenComplete((integer, throwable) -> log.info("偶数之和:{}", integer));

        int i = 0;
        while (i < 5) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("主线程的某些任务");
            i++;
        }
        log.info("主线程运行完毕");

        executorService.shutdown();
    }

    /**
     * 自定义线程名称
     */
    static class SimplThreadFactory implements ThreadFactory {
        private final AtomicInteger threadIndex = new AtomicInteger(1);
        private final String threadNamePrefix;

        public SimplThreadFactory(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(@Nonnull Runnable runnable) {
            return new Thread(runnable, threadNamePrefix + threadIndex.getAndIncrement());
        }
    }

    /**
     * 数字类型
     */
    enum NumberType {
        /**
         * 偶数
         */
        EVEN,
        /**
         * 奇数
         */
        ODD
    }

    @Slf4j
    static class SimpleCallable implements Callable<Integer> {

        private final NumberType numberType;
        @Getter
        private Integer sum = 0;

        public SimpleCallable(NumberType numberType) {
            this.numberType = numberType;
        }

        @Override
        public Integer call() {
            log.info("{} 线程启动", Thread.currentThread().getName());
            for (int i = 1; i <= 10; i++) {
                if (match(i)) {
                    try {
                        // 偶数线程 sleep 1 秒
                        // 奇数线程 sleep 2 秒
                        TimeUnit.SECONDS.sleep((i & 1) + 1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    log.info("{} 正在运行, 累增:{}", Thread.currentThread().getName(), i);
                    sum += i;
                }
            }
            log.info("{} 线程运行完毕", Thread.currentThread().getName());
            return sum;
        }

        private boolean match(int number) {
            return numberType.ordinal() == (number & 1);
        }

    }
}