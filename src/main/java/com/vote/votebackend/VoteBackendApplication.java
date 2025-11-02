package com.vote.votebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class VoteBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoteBackendApplication.class, args);
    }

}
