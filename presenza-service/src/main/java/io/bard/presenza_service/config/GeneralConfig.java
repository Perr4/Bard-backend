package io.bard.presenza_service.config;

import io.bard.common.model.Presenza;
import io.bard.presenza_service.dto.PresenzaDto;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneralConfig {
    @Bean
    public ModelMapper mapper() {
        ModelMapper mapper = new ModelMapper();
        // il DTO usa ballerino_id (snake_case), l'entita' ballerinoId (camelCase)
        mapper.typeMap(PresenzaDto.class, Presenza.class)
                .addMappings(m -> m.map(PresenzaDto::getBallerino_id, Presenza::setBallerinoId));
        mapper.typeMap(Presenza.class, PresenzaDto.class)
                .addMappings(m -> m.map(Presenza::getBallerinoId, PresenzaDto::setBallerino_id));
        return mapper;
    }

}
