package it.chronos.auth.kafka;

import it.chronos.auth.model.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserRegisteredEventPublisher {

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;
    private final String topic;

    public UserRegisteredEventPublisher(
            KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate,
            @Value("${chronos.kafka.topics.user-registered}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(UserAccount user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getCreatedAt());

        kafkaTemplate.send(topic, user.getId().toString(), event);
    }
}
