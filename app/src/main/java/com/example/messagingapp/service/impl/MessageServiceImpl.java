package com.example.messagingapp.service.impl;

import com.example.messagingapp.dto.MessageRequestDto;
import com.example.messagingapp.dto.MessageResponseDto;
import com.example.messagingapp.dto.mapper.MapperMessage;
import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;
import com.example.messagingapp.exception.MessageNotFound;
import com.example.messagingapp.exception.MessageNotFoundById;
import com.example.messagingapp.repository.MessageRepository;
import com.example.messagingapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    private final MessageRepository messageRepository;

    private final MapperMessage mapperMessage;


    @Override
    @Transactional
    public MessageResponseDto saveMessage(MessageRequestDto messageRequestDto) {

        Message message = mapperMessage.mapperMessageRequestDto(messageRequestDto);
        MessageStatus[] statuses = MessageStatus.values();
        Message messageByStatus = null;

        try {

            for (int i = 0; i < statuses.length - 1; i++) {
                messageByStatus = Message.builder().id(message.getId())
                        .content(message.getContent())
                        .status(statuses[i])
                        .timestampToString(message.getTimestampToString())
                        .processedAtToString(OffsetDateTime.now().format(DATE_TIME_FORMATTER))
                        .build();
                messageRepository.save(messageByStatus);
                Thread.sleep(5000);
            }

            return mapperMessage.mapperMessage(messageByStatus);

        } catch (Exception e) {
            message.setStatus(MessageStatus.FAILED);
            message.setProcessedAtToString(OffsetDateTime.now().format(DATE_TIME_FORMATTER));
            messageRepository.save(message);
            return mapperMessage.mapperMessage(message);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponseDto> getMessages() {

        List<MessageResponseDto> allMessage = mapperMessage.getAllMessage(messageRepository.findAll());
        if (Objects.nonNull(allMessage)) {
            return allMessage;
        } else {
            throw new MessageNotFound(MessageNotFound.MESSAGES_NOT_FOUND);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponseDto getMessageByID(UUID idMessage) {
        Message message = messageRepository.findById(idMessage).orElseThrow(() ->
                new MessageNotFoundById(MessageNotFoundById.MESSAGE_NOT_FOUND_BY_ID));
        return mapperMessage.mapperMessage(message);
    }
}
