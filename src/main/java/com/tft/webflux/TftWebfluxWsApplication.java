package com.tft.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
@Slf4j
public class TftWebfluxWsApplication {

    public static void main(String[] args) {
        log.info( "线上bug修复版本" );
        SpringApplication.run(TftWebfluxWsApplication.class, args);
    }

}
