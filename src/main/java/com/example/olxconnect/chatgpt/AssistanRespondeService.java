package com.example.olxconnect.chatgpt;

import com.example.olxconnect.dto.MessageDto;
import com.example.olxconnect.service.ChatService;
import com.example.olxconnect.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class AssistanRespondeService {

    private Logger logger = Logger.getLogger(AssistanRespondeService.class.getName());

    private Assistant assistant;

    @Autowired
    private ChatGptService chatGptService;

    @Autowired
    private MessageService messageService;


    public void answerMessage(String token, Long threadId){

        boolean isLastMessageFromUser = messageService.lastMessageIsFromUser(token, threadId);

        if (isLastMessageFromUser) {
            //Pobieranie wiadomosci z api
            List<MessageDto> messagesFromThreadOLX = messageService.getMessages(token, threadId);


            //Ustawianie ChatHistory z api
            ChatHistory chatHistory = new ChatHistory();
            chatHistory.loadExternalChatHistory(messagesFromThreadOLX);


            //Odpowiedz gpt
            String responseFromGpt = chatGptService.getResponse(chatHistory);

            //Wyslij wiadomosc
            messageService.sendMessage(token,threadId,responseFromGpt,new ArrayList<>());

            //Oznacz wiadomosc jako odczytana
            messageService.markThreadAsRead(token,threadId);

            //Pobierz watek


        }else {

        }

    }
}
