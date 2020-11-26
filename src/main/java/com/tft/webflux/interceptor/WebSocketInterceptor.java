package com.tft.webflux.interceptor;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


/**
 * @ClassName : WebSocketInterceptor
 * @Description : websocket 拦截器 建立链接时过滤
 * @Author : Luoj
 * @Date: 2020-11-25 21:21
 */
//@Configuration
class WebSocketFilter implements WebFilter {
    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request =  exchange.getRequest();
        //获取请求http头xttblog_token值
        String token = request.getHeaders().getFirst("xttblog_token");
        System.out.println("xttblog_token ＝" + token);
        //添加请求属性key和value
        exchange.getAttributes().put("url", "www.xttblog.com");
        /*过滤器链的概念都是类似的，调用过滤器链的filter方法将请求转到下一个filter，如果该filter是最后一  个filter,那就转到
        该请求对应的handler,也就是controller方法或函数式endpoint */
        return chain.filter(exchange);
    }
}
