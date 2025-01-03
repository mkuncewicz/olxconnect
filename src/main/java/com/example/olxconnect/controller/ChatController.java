package com.example.olxconnect.controller;

import com.example.olxconnect.dto.MessageDto;
import com.example.olxconnect.dto.ThreadResponseDto;
import com.example.olxconnect.dto.UserDto;
import com.example.olxconnect.entity.ThreadResponse;
import com.example.olxconnect.repository.ThreadResponseRepository;
import com.example.olxconnect.repository.TokenRepository;
import com.example.olxconnect.service.ChatService;
import com.example.olxconnect.service.MessageService;
import com.example.olxconnect.service.ThreadResponseService;
import com.example.olxconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private static final Logger logger = Logger.getLogger(ChatController.class.getName());

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ThreadResponseService threadResponseService;

    @Autowired
    private TokenRepository tokenRepository;

    @GetMapping()
    public String chatPage( @RequestParam("token") String token, @RequestParam("threadId") Long threadId,@RequestParam("userId") Long userId,Model model) {

        UserDto userDto = userService.getUserById(token,userId);

        // Pobieramy wiadomości z serwisu
        List<MessageDto> messages = messageService.getMessages(token, threadId);
        logger.info("Pobrano dane użytkownika: " + userDto);

        String username = userDto.getName();

        // Przekazujemy je do widoku
        model.addAttribute("chatUserName", username);
        model.addAttribute("messages", messages);
        model.addAttribute("token", token);
        model.addAttribute("threadId", threadId);

        return "chat";
    }

    @GetMapping("/byRefreshToken")
    public String chatPageByRefresh(
            @RequestParam("refreshToken") String refreshToken,
            @RequestParam("threadId") Long threadId,
            @RequestParam("userId") Long userId,
            Model model) {

        // Pobierz accessToken na podstawie refreshToken
        String accessToken = tokenRepository.findAccessTokenByRefreshToken(refreshToken);

        if (accessToken == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Pobierz dane użytkownika na podstawie accessToken i userId
        UserDto userDto = userService.getUserById(accessToken, userId);

        // Pobieramy wiadomości z serwisu
        List<MessageDto> messages = messageService.getMessages(accessToken, threadId);
        logger.info("Pobrano dane użytkownika: " + userDto);

        String username = userDto.getName();

        // Przekazujemy je do widoku
        model.addAttribute("chatUserName", username);
        model.addAttribute("messages", messages);
        model.addAttribute("token", accessToken); // Przekazujemy accessToken
        model.addAttribute("threadId", threadId);

        return "chat";
    }

    @PostMapping("/sendMessage")
    @ResponseBody
    public ResponseEntity<String> sendMessage(
            @RequestParam("token") String token,
            @RequestParam("threadId") Long threadId,
            @RequestParam("text") String text,
            @RequestParam(value = "attachmentUrls", required = false) List<String> attachmentUrls) {

        try {
            messageService.sendMessage(token, threadId, text, attachmentUrls);

            ThreadResponseDto threadFromApi = messageService.getThreadFromApi(token, threadId);
            Optional<ThreadResponse> threadFromDB = threadResponseService.findByThreadId(threadId);

            if (threadFromDB.isPresent()) {
                ThreadResponse updatedThreadResponse = threadFromDB.get();
                updatedThreadResponse.setTotalCount(threadFromApi.getTotalCount());
                updatedThreadResponse.setUnreadCount(threadFromApi.getUnreadCount());
                threadResponseService.save(updatedThreadResponse);

                logger.info("Wątek ID " + threadId + " został zaktualizowany w bazie danych.");
            } else {
                logger.warning("Wątek ID " + threadId + " nie został znaleziony w bazie danych.");
            }

            return ResponseEntity.ok("Wiadomość została wysłana.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Nie udało się wysłać wiadomości.");
        }
    }


}

