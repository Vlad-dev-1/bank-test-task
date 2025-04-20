package com.example.messagingapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message_info", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "message_id")
    private UUID id;

    @Column(name = "content")
    private String content;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status_message")
    private MessageStatus status;

    @Column(name = "message_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant timestamp;

    @Column(name = "message_time_status_changed", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant processedAt;

}
