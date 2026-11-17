package io.bard.learning_service;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EntityScan(basePackages = {"io.bard.learning_service.model", "io.bard.common.model"})
@EnableFeignClients(basePackages = "io.bard.learning_service.service")
public class BalleriniServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BalleriniServiceApplication.class, args);
	}

}
