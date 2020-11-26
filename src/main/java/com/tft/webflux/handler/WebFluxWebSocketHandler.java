package com.tft.webflux.handler;

import com.tft.webflux.factory.WebSocketSessionFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.reactive.socket.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * @ClassName : EchoHandler
 * @Description : 参数 ws?userId={userId}
 * @Author : Luoj
 * @Date: 2020-11-19 09:59
 */
@Slf4j
@Component
@ConfigurationProperties("spring.websocket")
@Setter
@Getter
public class WebFluxWebSocketHandler implements WebSocketHandler, CorsConfigurationSource {
    private static final Map<String, WebSocketSession> userMap = new ConcurrentHashMap<>();
    @Autowired
    private WebSocketSessionFactory webSocketSessionFactory;
    private Integer loginInterval = 10;
    private Integer pingInterval = 10;

    @SneakyThrows
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        HttpHeaders headers = handshakeInfo.getHeaders();
        //接收参数
        String queryParams = handshakeInfo.getUri().getQuery();
        Map<String, String> queryMap = getUrlParams( queryParams );
        String userId = null != queryMap ? queryMap.get( "userId" ) : null;
        if (StringUtils.isEmpty( queryParams )
                || StringUtils.isEmpty( userId )) {
            //参数为空直接关闭链接
            CloseStatus requiredExtension = CloseStatus.REQUIRED_EXTENSION;
            requiredExtension.withReason( "请求参数或userId不能为空" );
            log.warn( "请求参数或userId不能为空" );
            return session.close( requiredExtension );
        } else {
            log.info( "请求userId:[{}]", userId );
            /**
             * 防止相同用户同时占用链接，
             */
//            return checkUser( userId ) ? session
//                    .send( session.receive()
//                            .map( msg -> {
//                                log.info( "接收到用户：{}，发送消息：{}", userId, msg.getPayloadAsText() );
//                                return "收到发送消息:: " + msg.getPayloadAsText();
//
//                            } )
//                            .map( session::textMessage )
//                    ).then().doFinally( signal -> userMap.remove( userId ) ) : session.close( CloseStatus.REQUIRED_EXTENSION );
            return connection( session, userId );
        }
    }

    /**
     * 构建守护线程池
     *
     * @param threadSum
     * @return
     */
    private ScheduledThreadPoolExecutor newExecutor(int threadSum) {
        return new ScheduledThreadPoolExecutor( threadSum, r -> {
            Thread thread = new Thread( r );
            thread.setDaemon( true );
            thread.setName( this.getClass().getName() + "-websocket:" );
            return thread;
        } );
    }

    private Mono connection(WebSocketSession session, String userId) {
        ScheduledThreadPoolExecutor executor = newExecutor( 3 );
        return session.receive()
                .doOnSubscribe( subscription -> {
                    log.info( "------>:开始建立链接" );
                    /**
                     * 你有10秒时间登陆，不登陆就关掉连接;并且不给任何错误信息
                     */
//            executor.schedule( () ->
//                    {
//                        log.info( "执行登陆删除" );
//                        webSocketSessionFactory.checkAndRemove( session );
//                    }
//                    , loginInterval
//                    , TimeUnit.SECONDS );

                    //服务端定时发送ping消息
                    executor.scheduleWithFixedDelay( () ->
                            {
                                log.info( "执行ping发送-----" );
                                session.send( Flux.just( session.pingMessage( DataBufferFactory::allocateBuffer ) ) )
                                        .toProcessor();
                                //模拟发ping内容
//                                session.send( Flux.just( session.textMessage( WebSocketMessage.Type.PING.name().toLowerCase() ) ) )
//                                        .toProcessor();
//

                            }
                            , pingInterval
                            , pingInterval
                            , TimeUnit.SECONDS );

                    webSocketSessionFactory.registerSession( session, userId );
                } ).doOnTerminate( () -> {
                    /**
                     * 开始断开链接时执行
                     */
                    String user = webSocketSessionFactory.unregisterSession( session );
                    log.info( "------>:用户[{}]开始断开链接", user );
                } ).doOnComplete( () -> {
                    /**
                     * 完成断开链接时执行
                     */
                    executor.shutdown();
                    log.info( "------>:完成链接断开" );
                } ).doOnCancel( () -> {
                    log.info( "doOnCancel" );

                } ).doOnNext( message -> {
                    if (message.getType().equals( WebSocketMessage.Type.BINARY )) {
                        log.info( "收到二进制消息" );
                        ByteBuffer byteBuffer = message.getPayload().asByteBuffer();

                    } else if (message.getType().equals( WebSocketMessage.Type.TEXT )) {

                        String content = message.getPayloadAsText();
                        log.info( "收到文本消息:{}", content );
                        if (WebSocketMessage.Type.PING.name().equalsIgnoreCase( content )) {
                            session.send( Flux.just( session.textMessage( WebSocketMessage.Type.PONG.name().toLowerCase() ) ) )
                                    .toProcessor();
                        }
                        if (WebSocketMessage.Type.PONG.name().equalsIgnoreCase( content )) {
                            session.send( Flux.just( session.textMessage( WebSocketMessage.Type.PING.name().toLowerCase() ) ) )
                                    .toProcessor();
                        }


//                Message msg = JacksonUtils.wrapObject( content, Message.class );
//                System.out.println( msg.toString() );
                    } else if (message.getType().equals( WebSocketMessage.Type.PING )) {
                        //前端发起的心跳检测
                        session.send( Flux.just( session.pongMessage( s -> s.wrap( new byte[256] ) ) ) );
                        log.info( "收到ping消息" );
                    } else if (message.getType().equals( WebSocketMessage.Type.PONG )) {
                        log.info( "收到pong消息" );
//                pingInterval = Objects.isNull( pingInterval ) ? 10 : pingInterval;
//                executor.schedule( () -> session.send( Flux.just( session.pingMessage( DataBufferFactory::allocateBuffer ) ) )
//                        .toProcessor(), pingInterval, TimeUnit.SECONDS );
                    }
                } ).doOnError( e -> {
                    /**
                     * 所有异常都会执行
                     */
                    log.error( "doOnError", e );
                    //异常情况关闭socket链接
                    session.close( CloseStatus.REQUIRED_EXTENSION ).toProcessor();
                } ).doOnRequest( r -> {
                    /**
                     * 建立链接时执行
                     */
                    log.info( "doOnRequest" );

                } ).then().doFinally( signal -> {
                    log.info( "执行操作类型:[{}]", signal.toString() );
                } );
    }

    /**
     * 用户检查是否有效
     *
     * @param userId
     * @return
     */
    private boolean checkUser(String userId) {
        return true;
    }

    private void saveSession(String userId, WebSocketSession session) {
        if (userMap.containsKey( userId )) {
            log.info( "用户[{}]已经在线，踢出老用户", userId );
            userMap.get( userId ).close();
        }
        userMap.put( userId, session );
        log.info( "在线用户总数为:[{}]", userMap.size() );
    }

    private Map<String, String> getUrlParams(String queryParams) throws Throwable {
        return StringUtils.isEmpty( queryParams ) ? null : Optional.of( URLDecoder.decode( queryParams, "UTF-8" ) )
                //去掉？
                .map( parames -> parames.replace( "?", ";" ) )
                .map( params -> {
                    //参数列表
                    String[] paramArrays = StringUtils.tokenizeToStringArray( params, "&" );
                    Map<String, String> paramMap = new HashMap<>();
                    for (String param : paramArrays) {
                        if (param.contains( "=" )) {
                            String key = param.split( "=" )[0];
                            String value = param.split( "=" )[1];
                            paramMap.put( key, value );
                        }
                    }
                    return CollectionUtils.isEmpty( paramMap ) ? null : paramMap;
                } )
                .orElseThrow( (Supplier<Throwable>) () -> new IllegalArgumentException( "参数传递格式异常，需满足 key=value方式" ) );
    }

    /**
     * 跨域设置
     *
     * @param serverWebExchange
     * @return
     */
    @Override
    public CorsConfiguration getCorsConfiguration(ServerWebExchange serverWebExchange) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin( "**" );
        return corsConfiguration;
    }
}
