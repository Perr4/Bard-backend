package io.bard.learning_service.config;

import io.bard.learning_service.dto.BallerinoCreate;
import io.bard.learning_service.model.Ballerino;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GeneralConfig {
    @Bean
    public ModelMapper mapper (){
        ModelMapper mapper = new ModelMapper();
        // l'id del ballerino e' generato dal servizio: non deve essere copiato dallo userId
        mapper.typeMap(BallerinoCreate.class, Ballerino.class)
                .addMappings(m -> m.skip(Ballerino::setId));
        return mapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
