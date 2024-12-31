package com.example.olxconnect.service;

import com.example.olxconnect.dto.MessageDto;
import com.example.olxconnect.mail.NewMessageMail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class NewMessageService {

    @Autowired
    private MessageService messageService;

    public List<NewMessageMail> getDateFromLastMessage(List<NewMessageMail> newMessageMailList) {

        List<NewMessageMail> result = new ArrayList<NewMessageMail>();

        for (NewMessageMail newMessageMail : newMessageMailList) {

            List<MessageDto> messages = messageService.getMessages(newMessageMail.getAccToken(), newMessageMail.getThreadId());

            if (isMessageFromToday(messages.get(0))){

                result.add(newMessageMail);
            }
        }
        return result;
    }

    private boolean isMessageFromToday(MessageDto message) {
        if (message == null || message.getCreatedAt() == null) {
            return false; // Jeśli wiadomość lub jej data utworzenia jest null, zwracamy false
        }

        // Porównujemy datę wiadomości z dzisiejszą datą
        LocalDate messageDate = message.getCreatedAt().plusHours(1).toLocalDate();
        LocalDate today = LocalDate.now();

        return messageDate.equals(today);
    }
}
