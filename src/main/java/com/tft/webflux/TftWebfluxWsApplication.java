package com.tft.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
public class TftWebfluxWsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TftWebfluxWsApplication.class, args);
    }

}
