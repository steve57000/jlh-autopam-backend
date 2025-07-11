package com.jlh.jlhautopambackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JlhApplication {

    public static void main(String[] args) {

        SpringApplication.run(JlhApplication.class, args);

    }

}
