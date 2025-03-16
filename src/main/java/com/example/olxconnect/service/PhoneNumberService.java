package com.example.olxconnect.service;

import com.example.olxconnect.dto.MessageDto;
import com.example.olxconnect.mail.NewMessageMail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PhoneNumberService {

    @Autowired
    private MessageService messageService;



    public List<NewMessageMail> getListWithNumber(List<NewMessageMail> newMessageMailList){

        List<NewMessageMail> listWithNumbers = new ArrayList<>();


        for (NewMessageMail newMessageMail : newMessageMailList) {

            boolean isNumberInThread = checkThreadForPhoneNumberr(newMessageMail.getAccToken(), newMessageMail.getThreadId());

            if (isNumberInThread){
                listWithNumbers.add(newMessageMail);
            }
        }


        return listWithNumbers;
    }


    private boolean checkThreadForPhoneNumberr(String token, Long threadID){

        List<MessageDto> messages = messageService.getMessages(token, threadID);


        return isPhoneNumberInTheMessage(messages);
    }



    private boolean isPhoneNumberInTheMessage(List<MessageDto> messageDtoList) {
        if (messageDtoList == null || messageDtoList.isEmpty()) {
            return false;
        }

        // Regex dla numerów telefonów:
        // - 9 cyfr bez spacji np. "512321321"
        // - 9 cyfr oddzielonych spacjami, myślnikami np. "512-321-321", "512 321 321"
        // - Opcjonalny prefix np. "+48 512321321", "+48 512-321-321"
        String phoneRegex = "\\+?(\\d{2,3})?[-.\\s]?\\d{9}|\\+?(\\d{2,3})?[-.\\s]?\\d{3}[-.\\s]?\\d{3}[-.\\s]?\\d{3}";
        Pattern pattern = Pattern.compile(phoneRegex);

        for (MessageDto message : messageDtoList) {
            if (message.getText() != null) {
                Matcher matcher = pattern.matcher(message.getText());
                if (matcher.find()) {
                    return true;
                }
            }
        }

        return false;
    }
}
