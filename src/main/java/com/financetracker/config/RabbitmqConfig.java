package com.financetracker.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {
    public static final String TRANSACTIONS_EXCHANGE = "finance.transactions.exchange";
    public static final String GMAIL_INGESTED_QUEUE = "gmail.transactions.ingested.queue";
    public static final String GMAIL_INGESTED_ROUTING_KEY = "gmail.transaction.ingested";

    @Bean
    public TopicExchange transactionsExchange() {
        return new TopicExchange(TRANSACTIONS_EXCHANGE, true, false);
    }

    @Bean
    public Queue gmailIngestedQueue() {
        return new Queue(GMAIL_INGESTED_QUEUE, true);
    }

    @Bean
    public Binding gmailIngestedBinding() {
        return BindingBuilder.bind(gmailIngestedQueue())
                .to(transactionsExchange())
                .with(GMAIL_INGESTED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
