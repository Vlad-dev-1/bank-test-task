package com.example.messagingapp.kafka.producer;


import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class KafkaMessageProducer {

    @Value("${app.kafka.input-topic}")
    private String inputTopic;

    @Value("${app.kafka.output-topic}")
    private String outputTopic;

    private final KafkaTemplate<String, Object> requestkafkaTemplate;
    private final KafkaTemplate<String, Object> responseKafkaTemplate;

    public void sendMessageRequest(MessageRequest messageRequest) {
        requestkafkaTemplate.send(inputTopic, messageRequest.getId().toString(), messageRequest);
    }

    public void sendMessageResponse(MessageResponse messageResponse) {
        responseKafkaTemplate.send(outputTopic, messageResponse.getMessageId().toString(), messageResponse);
    }
}
