package com.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.chatapp.repository")
@EntityScan(basePackages = "com.chatapp.model")
@ComponentScan({ "com.chatapp.*" })
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}