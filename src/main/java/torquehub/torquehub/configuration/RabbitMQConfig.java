package torquehub.torquehub.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "app.exchange";
    public static final String QUEUE_TICKETS = "tickets.queue";
    public static final String QUEUE_ACCOUNTS = "accounts.queue";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue ticketsQueue() {
        return new Queue(QUEUE_TICKETS);
    }

    @Bean
    public Queue accountsQueue() {
        return new Queue(QUEUE_ACCOUNTS);
    }

    @Bean
    public Binding ticketsBinding(Queue ticketsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(ticketsQueue).to(exchange).with("tickets.#");
    }

    @Bean
    public Binding accountsBinding(Queue accountsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(accountsQueue).to(exchange).with("accounts.#");
    }
}

