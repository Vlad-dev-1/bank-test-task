package com.example.messagingapp.service;

import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;

import java.util.List;
import java.util.Map;

public interface MessageStatisticService {

    Map<MessageStatus, Long> getMessageStatistic();
}
