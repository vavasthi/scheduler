package com.avasthi.microservices.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * This is the launcher entry point for the scheduler. The main is just a boilerplate function.
 * Most of the useful stuff for launcher is in the initialize method of the class. If you want to
 * integrate scheduler with another existing project, please use code in initialize method.
 */
@SpringBootApplication
public class SchedulerLauncher {

	public static void main(String[] args) {

		SpringApplication.run(SchedulerLauncher.class);
	}
	@PostConstruct
	public void initialize() {

	}
}
