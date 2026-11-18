package com.Project.projectApi.Controller;

import com.Project.projectApi.List.LinkedList;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JsonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer listasPropiasComoArreglos() {
        return builder -> builder.serializerByType(LinkedList.class, new JsonSerializer<LinkedList<?>>() {
            @Override
            public void serialize(LinkedList<?> lista, JsonGenerator json, SerializerProvider proveedor)
                    throws IOException {
                json.writeStartArray();
                for (Object elemento : lista) {
                    proveedor.defaultSerializeValue(elemento, json);
                }
                json.writeEndArray();
            }
        });
    }
}
