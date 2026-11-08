package io.bard.presenza_service;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EntityScan(basePackages = {"io.bard.presenza_service.model", "io.bard.common.model"})
public class PresenzaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PresenzaServiceApplication.class, args);
	}

}
