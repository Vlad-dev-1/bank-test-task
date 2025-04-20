package com.example.messagingapp.service;

import com.example.messagingapp.entity.MessageStatus;

import java.util.Map;

public interface MessageStatisticService {

    Map<MessageStatus, Long> getMessageStatistic();
}
