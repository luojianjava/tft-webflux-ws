package com.tft.webflux.message;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName : RocketMqProductorHandler
 * @Description :
 * @Author : Luoj
 * @Date: 2020-11-24 22:41
 */
//@Service
//@RocketMQMessageListener(topic = "${tft-traffic-dispatch-consumer.topic}", consumerGroup = "${tft-traffic-dispatch-consumer.consumerGroup}", consumeThreadMax = 20)
@Slf4j
public class RocketMqProductorHandler implements RocketMQListener<Message> {

    @Override
    public void onMessage(Message message) {
        try {
            log.info( "消费数据：{}",message );

        } catch (Exception e) {
            log.error( "MQ消息消费异常：消息体{}",message, e );
            throw e;
        }
    }
}
