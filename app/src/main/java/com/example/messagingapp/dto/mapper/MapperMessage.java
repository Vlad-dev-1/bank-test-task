package com.example.messagingapp.dto.mapper;


import com.example.messagingapp.dto.MessageRequestDto;
import com.example.messagingapp.dto.MessageResponseDto;
import com.example.messagingapp.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface MapperMessage {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "processedAtToString", ignore = true)
    @Mapping(target = "timestampToString", source = "timestamp", qualifiedByName = "offsetDateTimeToString")
    Message mapperMessageRequestDto(MessageRequestDto messageRequestDto);

    @Named("offsetDateTimeToString")
    default String OffsetDateTimeToString(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }


    @Mapping(source = "id", target = "messageId")
    @Mapping(target = "processedAt", source = "processedAtToString", qualifiedByName = "stringToOffsetDateTime")
    MessageResponseDto mapperMessage(Message message);

    @Named("stringToOffsetDateTime")
    default OffsetDateTime stringToOffsetDateTime(String dateTimeString) {
        if (dateTimeString == null) {
            return null;
        }
        return OffsetDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    default List<MessageResponseDto> getAllMessage(List<Message> messages) {
        if (messages == null) {
            return Collections.emptyList();
        }
        return messages.stream()
                .map(this::mapperMessage)
                .collect(Collectors.toList());
    }
}


