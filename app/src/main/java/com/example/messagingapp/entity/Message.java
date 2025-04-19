package com.example.messagingapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
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

    @Column(name = "message_time", columnDefinition = "VARCHAR(50)")
    private String timestampToString;

    @Column(name = "message_time_status_changed", columnDefinition = "VARCHAR(50)")
    private String processedAtToString;

    @Transient
    private OffsetDateTime timestamp;

    @Transient
    private OffsetDateTime processedAt;

}
