package com.tft.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class TftWebfluxWsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TftWebfluxWsApplication.class, args);
    }

}
