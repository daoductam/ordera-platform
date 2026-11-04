package com.ordera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OrderaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderaApplication.class, args);
    }
}

