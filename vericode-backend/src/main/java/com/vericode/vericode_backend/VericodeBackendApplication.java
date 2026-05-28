package com.vericode.vericode_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class VericodeBackendApplication {

	public static void main(String[] args) {

        SpringApplication.run(VericodeBackendApplication.class, args);
	}

}
