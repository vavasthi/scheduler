package com.avasthi.microservices.scheduler;

import com.avasthi.microservices.caching.SchedulerItemBeingProcessedCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

/**
 * This is the launcher entry point for the scheduler. The main is just a boilerplate function.
 * Most of the useful stuff for launcher is in the initialize method of the class. If you want to
 * integrate scheduler with another existing project, please use code in initialize method.
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan(basePackages = {"com.avasthi.microservices"})
public class SchedulerLauncher {

	@Autowired
	private SchedulerService schedulerService;

	public static void main(String[] args) {

		SpringApplication.run(SchedulerLauncher.class);
	}
	@PostConstruct
	public void initialize() {

		schedulerService.processPending();
	}
}
