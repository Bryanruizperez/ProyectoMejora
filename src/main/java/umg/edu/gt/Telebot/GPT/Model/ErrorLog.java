package umg.edu.gt.Telebot.GPT.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
    @Data
    @Entity
    public class ErrorLog {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "chat_message_id")
        private ChatMessage chatMessage;

        private String errorMessage;
        private String stackTrace;
        private LocalDateTime timestamp;


    }




