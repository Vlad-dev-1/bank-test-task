package com.example.messagingapp.dto.mapper;


import com.example.messagingapp.dto.MessageRequest;
import com.example.messagingapp.dto.MessageResponse;
import com.example.messagingapp.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface MapperMessage {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    Message mapperMessageRequestDto(MessageRequest messageRequest);

    @Mapping(source = "id", target = "messageId")
    MessageResponse mapperMessage(Message message);

    default List<MessageResponse> getAllMessage(List<Message> messages) {
        if (messages == null) {
            return Collections.emptyList();
        }
        return messages.stream()
                .map(this::mapperMessage)
                .collect(Collectors.toList());
    }
}


