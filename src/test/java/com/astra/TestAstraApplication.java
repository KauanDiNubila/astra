package com.astra;

import org.springframework.boot.SpringApplication;

public class TestAstraApplication {

	public static void main(String[] args) {
		SpringApplication.from(AstraApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
