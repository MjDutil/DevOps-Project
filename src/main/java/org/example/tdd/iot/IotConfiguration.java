package org.example.tdd.iot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "iot.habilitado", havingValue = "true")
public class IotConfiguration {
    @Bean
    RegistroEventosIot registroEventosIot(@Value("${iot.capacidade:1024}") int capacidade) {
        return new RegistroEventosIot(capacidade);
    }
}
