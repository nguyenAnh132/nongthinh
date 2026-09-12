package com.nongthinh.notification_service.configuration;

import java.util.concurrent.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class RealtimeConfig {
    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService sseExecutor() {
        return new ThreadPoolExecutor(4, 16, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(256),
                Thread.ofPlatform().daemon().name("sse-send-", 0).factory(), new ThreadPoolExecutor.AbortPolicy());
    }
}
