package com.nextbank.transfers;

import org.springframework.boot.SpringApplication;

public class TestTransfersServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(TransfersServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
