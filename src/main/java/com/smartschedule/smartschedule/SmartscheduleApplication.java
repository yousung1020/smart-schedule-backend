package com.smartschedule.smartschedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SmartscheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartscheduleApplication.class, args);
	}

}
