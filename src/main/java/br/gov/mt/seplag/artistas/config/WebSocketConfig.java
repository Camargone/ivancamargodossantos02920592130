package br.gov.mt.seplag.artistas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefixo para os destinos de mensagens do broker
        registry.enableSimpleBroker("/topic");
        // Prefixo para destinos mapeados em @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Conexão WebSocket
        registry.addEndpoint("/ws/albuns")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Endpoint sem SockJS para clientes que suportam WebSocket nativo
        registry.addEndpoint("/ws/albuns")
                .setAllowedOriginPatterns("*");
    }
}
