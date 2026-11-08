package io.bard.learning_service;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EntityScan(basePackages = {"io.bard.learning_service.model", "io.bard.common.model"})
public class BalleriniServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BalleriniServiceApplication.class, args);
	}

}
