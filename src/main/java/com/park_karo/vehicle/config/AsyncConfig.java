package com.park_karo.vehicle.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync // Enables Spring's asynchronous processing capability
public class AsyncConfig {

	@Bean(name = "threadPoolTaskExecutor") // Give the executor a specific name
	Executor threadPoolTaskExecutor() {
		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor(); // Added 'final'
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(25);
		executor.setThreadNamePrefix("Async-Task-");
		executor.initialize();
		return executor;
	}
}