package com.red.springBootClient.controller;

import io.socket.client.Socket;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class MessageController {
    private final List<String> messages = new CopyOnWriteArrayList<>();
    private final Map<LocalDateTime, Integer> messageCounts = new LinkedHashMap<>(); // Tracks counts per minute
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(Socket socket, SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;

        socket.on("message", args -> {
            String msg = (String) args[0];
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES); // Group by minute

            // Update messages list
            messages.add(msg);
            if (messages.size() > 10) messages.remove(0);

            // Update message frequency
            messageCounts.put(now, messageCounts.getOrDefault(now, 0) + 1);

            // Keep only the last 60 minutes of data
            if (messageCounts.size() > 60) {
                LocalDateTime oldestKey = messageCounts.keySet().iterator().next();
                messageCounts.remove(oldestKey);
            }

            // Broadcast messages AND frequency data
            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("messages", messages);
            broadcastData.put("frequency", getFormattedFrequencyData());

            messagingTemplate.convertAndSend("/topic/messages", broadcastData);
        });
    }

    // Format frequency data for Chart.js
    private Map<String, List<?>> getFormattedFrequencyData() {
        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();

        messageCounts.forEach((time, count) -> {
            labels.add(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            data.add(count);
        });

        return Map.of("labels", labels, "data", data);
    }
    // UI endpoint
    @GetMapping("/")
    public String getMessages(Model model) {
        model.addAttribute("messages", messages);
        return "messages"; // Matches messages.html
    }

    // API endpoint (optional)
    @GetMapping("/messages")
    public List<String> getMessagesApi() {
        return messages;
    }

}