package com.flycode.lendingandrepaymentservice.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
@Profile("!integration-tests")
public class AsyncConfig extends AsyncConfigurerSupport {

    @Autowired
    Environment environment;

    @Bean("mainAsyncExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(environment.getProperty("threads.core.poolsize", Integer.class, 10));
        executor.setMaxPoolSize(environment.getProperty("threads.max.poolsize", Integer.class, 10));
        executor.setQueueCapacity(environment.getProperty("threads.queue.capacity", Integer.class, 500));
        executor.setThreadNamePrefix("async-" + environment.getProperty("spring.application.name") + "-");
        executor.initialize();

        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            // TODO: log
        };
    }
}
