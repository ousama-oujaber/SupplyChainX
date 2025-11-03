package com.protocol.supplychainx;

import org.springframework.boot.SpringApplication;

public class TestSupplyChainXApplication {

    public static void main(String[] args) {
        SpringApplication.from(SupplyChainXApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
