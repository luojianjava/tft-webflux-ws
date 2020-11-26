package com.tft.webflux.config;

import com.tft.webflux.handler.WebFluxWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName : WebSocketConfig
 * @Description :
 * @Author : Luoj
 * @Date: 2020-11-19 10:01
 */
@Configuration
public class WebSocketConfig {
    @Bean
    public WebFluxWebSocketHandler echoHandler() {
        return new WebFluxWebSocketHandler();
    }

    @Bean
    public HandlerMapping handlerMapping(WebFluxWebSocketHandler webFluxWebSocketHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put( "/ws", webFluxWebSocketHandler );
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap( map );
        mapping.setOrder( Ordered.HIGHEST_PRECEDENCE );
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter( webSocketService() );
    }

    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService( new ReactorNettyRequestUpgradeStrategy() );
    }
}
