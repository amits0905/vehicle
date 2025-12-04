package com.park_karo.vehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
@EnableAsync
public class VehicleApplication {

	private static final Logger logger = LoggerFactory.getLogger(VehicleApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Vehicle Application...");
		SpringApplication.run(VehicleApplication.class, args);
	}

	@Bean
	public CommandLineRunner findDuplicateRootMappings(ApplicationContext context) {
		return args -> {
			logger.info("\n🔍 ========== DUPLICATE ROOT MAPPING DETECTOR ==========");

			try {
				var handlerMapping = context.getBean(RequestMappingHandlerMapping.class);

				// Find ALL mappings to "/"
				handlerMapping.getHandlerMethods().forEach((mapping, handlerMethod) -> {
					if (mapping.getPatternValues().contains("/")) {
						String className = handlerMethod.getBeanType().getName();
						String methodName = handlerMethod.getMethod().getName();
						String beanName = handlerMethod.getBean().toString();

						logger.error("🚨 FOUND: {}#{}()", className, methodName);
						logger.error("   Bean: {}", beanName);

						// Print the actual source location if possible
						try {
							java.lang.StackTraceElement[] stackTrace = new Throwable().getStackTrace();
							for (StackTraceElement element : stackTrace) {
								if (element.getClassName().equals(className)) {
									logger.error("   Source: {}:{}", element.getFileName(), element.getLineNumber());
									break;
								}
							}
						} catch (Exception e) {
							// Ignore if can't get stack trace
						}
						logger.error("   ---");
					}
				});

				// Also list ALL controllers for reference
				logger.info("\n📋 ========== ALL REGISTERED CONTROLLERS ==========");
				String[] allBeans = context.getBeanDefinitionNames();
				for (String beanName : allBeans) {
					Object bean = context.getBean(beanName);
					Class<?> beanClass = bean.getClass();

					// Check if it's a controller by looking for @GetMapping methods
					java.lang.reflect.Method[] methods = beanClass.getDeclaredMethods();
					for (java.lang.reflect.Method method : methods) {
						if (method.isAnnotationPresent(org.springframework.web.bind.annotation.GetMapping.class)) {
							logger.info("✅ Controller Bean: {} (Class: {})", beanName, beanClass.getSimpleName());
							break;
						}
					}
				}

			} catch (Exception e) {
				logger.error("Debug error: {}", e.getMessage());
			}
		};
	}
}