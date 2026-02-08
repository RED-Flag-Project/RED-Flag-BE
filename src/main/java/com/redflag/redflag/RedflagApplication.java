package com.redflag.redflag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.redflag.redflag.analysis.domain")
public class RedflagApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedflagApplication.class, args);
	}

}
