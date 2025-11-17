package com.protocol.supplychainx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SupplyChainXApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupplyChainXApplication.class, args);
    }
}
