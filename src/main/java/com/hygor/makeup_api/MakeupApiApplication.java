package com.hygor.makeup_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
public class MakeupApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MakeupApiApplication.class, args);
	}

}
