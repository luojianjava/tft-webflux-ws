package com.tft.webflux.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName : WebSocketSessionFactory
 * @Description :
 * @Author : Luoj
 * @Date: 2020-11-19 16:25
 */
@Slf4j
@Component
public class WebSocketSessionFactory {
    /**
     * 存储session
     */
    private final ConcurrentMap<String, String> userSessionIds = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, WebSocketSession> clientInfoSessionIds = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> sessionIdUser = new ConcurrentHashMap<>();
    /**
     * 获取sessionId
     *
     * @param user
     * @return
     */
    private String getSessionIds(String user) {
        return Optional.of( this.userSessionIds.get( user ) ).orElse( null );
    }

    /**
     * 获取用户session
     *
     * @param user
     * @return
     */
    public WebSocketSession getSessionByUser(String user) {
        return Optional.ofNullable( getSessionIds( user ) )
                .filter( userId -> !StringUtils.isEmpty( userId ) )
                .map( clientInfoSessionIds::get ).orElse( null );
    }

    /**
     * 批量获取用户session
     *
     * @param users
     * @return
     */
    public Collection<WebSocketSession> getSessionByUsers(Collection<String> users) {
        return CollectionUtils.isEmpty( users ) ? null : users.stream()
                .map( this::getSessionByUser )
                .collect( Collectors.toSet() );
    }

    /**
     * 获取所有session
     *
     * @return Collection<WebSocketSession>
     */
    public Collection<WebSocketSession> getAllSessions() {
        return clientInfoSessionIds.values();
    }

    /**
     * 获取所有session
     *
     * @return
     */
    public ConcurrentMap<String, String> getAllSessionIds() {
        return this.userSessionIds;
    }

    /**
     * 获取所有session
     *
     * @return
     */
    public ConcurrentMap<String, WebSocketSession> getAllSessionWebSocketInfos() {
        return this.clientInfoSessionIds;
    }


    /**
     * 保存session
     *
     * @param session
     * @param userId
     */
    public void registerSession(WebSocketSession session, String userId) {
        Assert.isTrue( !StringUtils.isEmpty( userId ), "userId 不能为空" );
        String sessionId = session.getId();
        sessionIdUser.putIfAbsent( sessionId, userId );
        Assert.isTrue( null != sessionIdUser.putIfAbsent( sessionId, userId ), "用户已经存在" );
    }

    /**
     * 根据sessionId 获取socket链接信息
     *
     * @param sessionId
     * @return
     */
    public WebSocketSession getSessionBySessionId(String sessionId) {
        return this.clientInfoSessionIds.get( sessionId );
    }



    public String unregisterSession(WebSocketSession session) {
        String sessionId = session.getId();
        String user = sessionIdUser.get( sessionId );
        if (!StringUtils.isEmpty( user )) {
//            unregisterSessionId( sessionId );
//            unregisterSessionId( user, sessionId );
            sessionIdUser.remove( sessionId );
        }
        return user;
    }

    public void checkAndRemove(WebSocketSession session) {
        String sessionId = session.getId();
        if (!this.clientInfoSessionIds.containsKey( sessionId )) {
            log.info( "sessionId:{} 10秒内没有登陆,关掉它", sessionId );
            session.close( CloseStatus.REQUIRED_EXTENSION ).toProcessor();
        }
        log.info( "sessinId:{}已经登陆，是合法的", sessionId );
    }

}
