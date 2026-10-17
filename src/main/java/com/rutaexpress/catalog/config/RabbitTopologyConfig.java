package com.rutaexpress.catalog.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topología de RabbitMQ que necesita catalog. Solo declara beans; no contiene
 * lógica de negocio. RabbitAdmin los crea en el broker al conectarse.
 *
 * - rutaexpress.commands (direct): recibe los comandos que shipments publica.
 * - rutaexpress.events (topic): donde catalog publica el resultado.
 * - rutaexpress.dlx (direct): recibe los mensajes rechazados, vencidos o desbordados.
 */
@Configuration
@EnableConfigurationProperties(MensajeriaProperties.class)
public class RabbitTopologyConfig {

    private final MensajeriaProperties props;

    public RabbitTopologyConfig(MensajeriaProperties props) {
        this.props = props;
    }

    // ------------------------------------------------------------- exchanges

    @Bean
    public DirectExchange comandosExchange() {
        return new DirectExchange(props.exchanges().comandos(), true, false);
    }

    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(props.exchanges().eventos(), true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(props.exchanges().dlx(), true, false);
    }

    // ----------------------------------------------------------------- colas

    /** Cola de comandos con retención (TTL y tamaño máximo) y DLQ asociada. */
    @Bean
    public Queue comandosQueue() {
        return QueueBuilder.durable(props.comandos().cola())
                .ttl(props.comandos().ttlMs())
                .maxLength(props.comandos().maxLength())
                .overflow(QueueBuilder.Overflow.dropHead)
                .deadLetterExchange(props.exchanges().dlx())
                .deadLetterRoutingKey(props.comandos().dlqRoutingKey())
                .build();
    }

    /** DLQ de comandos: con límites propios para que no crezca sin control. */
    @Bean
    public Queue comandosDlq() {
        return QueueBuilder.durable(props.comandos().dlq())
                .ttl(props.dlq().ttlMs())
                .maxLength(props.dlq().maxLength())
                .build();
    }

    // -------------------------------------------------------------- bindings

    @Bean
    public Binding comandosBinding() {
        return BindingBuilder.bind(comandosQueue())
                .to(comandosExchange())
                .with(props.comandos().routingKey());
    }

    @Bean
    public Binding comandosDlqBinding() {
        return BindingBuilder.bind(comandosDlq())
                .to(deadLetterExchange())
                .with(props.comandos().dlqRoutingKey());
    }
}
