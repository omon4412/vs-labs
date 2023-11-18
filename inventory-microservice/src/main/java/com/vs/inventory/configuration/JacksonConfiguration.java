package com.vs.inventory.configuration;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для Jackson.
 * Нужно для использования свого сериализатора и дессериализатора
 */
@Configuration
public class JacksonConfiguration {
    /**
     * ObjectMapper provides functionality for
     * reading and writing JSON, either to and from basic POJOs (Plain Old Java Objects).
     *
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //objectMapper.registerModule(new SimpleModule().addDeserializer(LocalDate.class, new LocalDateDeserializer()));
        //objectMapper.registerModule(new SimpleModule().addSerializer(LocalDate.class, new LocalDateSerializer()));
        return objectMapper;
    }
}

