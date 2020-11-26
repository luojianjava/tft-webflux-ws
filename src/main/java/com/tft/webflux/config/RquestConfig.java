package com.tft.webflux.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * @ClassName : RquestConfig
 * @Description : 请求配置类
 * @Author : Luoj
 * @Date: 2020-11-24 18:36
 */
@Configuration
public class RquestConfig {
    @Bean
    public RestTemplate requestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout( 10 );
        factory.setReadTimeout( 10 );
        return new RestTemplate( factory );
    }
}
