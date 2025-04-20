package com.example.messagingapp.repository;

import com.example.messagingapp.entity.Message;
import com.example.messagingapp.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m.status, COUNT(m) FROM Message m GROUP BY m.status")
    Map<MessageStatus, Long> countMessagesByStatus();

}
