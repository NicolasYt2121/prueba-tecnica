package com.nexo.banking;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.math.BigDecimal;
import java.io.IOException;

@SpringBootApplication
public class BankingApplication {
    public static void main(String[] args) { SpringApplication.run(BankingApplication.class, args); }
    @Bean Clock clock() { return Clock.system(ZoneId.of("America/Bogota")); }
    @Bean Jackson2ObjectMapperBuilderCustomizer exactMoney() {
        return builder -> builder.serializerByType(BigDecimal.class,new JsonSerializer<BigDecimal>() {
            @Override public void serialize(BigDecimal value,JsonGenerator generator,SerializerProvider provider) throws IOException {
                generator.writeString(value.toPlainString());
            }
        });
    }
}
