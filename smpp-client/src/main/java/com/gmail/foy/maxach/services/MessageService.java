package com.gmail.foy.maxach.services;

import com.gmail.foy.maxach.models.Message;
import com.gmail.foy.maxach.repositories.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageService {

    private static final MessageRepository messageRepository = new MessageRepository();
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);


    public void create(Message message) {
        log.info("Create message: '{}'", message.getMessage());
        messageRepository.create(message);
    }


    public void updateStatus(String id) {
        log.info("Update delivery status of message with id: '{}'", id);
        messageRepository.updateStatus(id);
    }
}
