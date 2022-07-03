package com.persoff68.fatodo.config;

import com.persoff68.fatodo.client.ItemServiceClient;
import com.persoff68.fatodo.client.WsServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class ClientConfiguration {

    private final BeanFactory beanFactory;

    @Bean
    @Primary
    public ItemServiceClient itemClient() {
        return (ItemServiceClient) beanFactory.getBean("itemServiceClientWrapper");
    }

    @Bean
    @Primary
    public WsServiceClient wsClient() {
        boolean kafkaProducerExists = beanFactory.containsBean("wsProducer");
        return kafkaProducerExists
                ? (WsServiceClient) beanFactory.getBean("wsProducer")
                : (WsServiceClient) beanFactory.getBean("wsServiceClientWrapper");
    }

}
