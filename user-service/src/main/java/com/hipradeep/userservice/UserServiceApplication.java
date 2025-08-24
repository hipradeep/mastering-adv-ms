package com.hipradeep.userservice;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceApplication.class);

	@Value("${server.port:NOT_LOADED}")
	private String serverPort;

	@Value("${spring.application.name:NOT_LOADED}")
	private String appName;


	@PostConstruct
	public void checkEnvironment() {
		System.out.println("=== ENVIRONMENT VARIABLES ===");
		System.out.println("SERVER_PORT: " + System.getenv("SERVER_PORT"));
		System.out.println("SPRING_APPLICATION_NAME: " + System.getenv("SPRING_APPLICATION_NAME"));

		System.out.println("=== SYSTEM PROPERTIES ===");
		System.out.println("server.port: " + System.getProperty("server.port"));
		System.out.println("spring.application.name: " + System.getProperty("spring.application.name"));
	}
	@PostConstruct
	public void checkConfig() {
		logger.info("=== CONFIGURATION CHECK ===");
		logger.info("Server Port: {}", serverPort);
		logger.info("Application Name: {}", appName);
		logger.info("=== END CONFIG CHECK ===");

		if ("NOT_LOADED".equals(serverPort)) {
			logger.error("❌ YAML configuration is NOT being loaded!");
		} else {
			logger.info("✅ YAML configuration is working!");
		}
	}

	public static void main(String[] args) {
		//SpringApplication.run(UserServiceApplication.class, args);
		SpringApplication application = new SpringApplication(UserServiceApplication.class);
		//application.setWebEnvironment(false);
		application.setBannerMode(Banner.Mode.OFF);
		application.run(args);
	}

}
