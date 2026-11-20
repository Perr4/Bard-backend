package io.bard.uscite_service;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EntityScan(basePackages = {"io.bard.uscite_service.model", "io.bard.common.model"})
public class UsciteServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsciteServiceApplication.class, args);
	}

}
