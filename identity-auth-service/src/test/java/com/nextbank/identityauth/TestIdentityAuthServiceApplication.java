package com.nextbank.identityauth;

import org.springframework.boot.SpringApplication;

public class TestIdentityAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(IdentityAuthServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
