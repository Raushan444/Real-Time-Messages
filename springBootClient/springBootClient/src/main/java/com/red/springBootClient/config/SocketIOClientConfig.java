package com.red.springBootClient.config;

import io.socket.client.IO;
import io.socket.client.Socket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;

@Configuration
public class SocketIOClientConfig {

    @Bean
    public Socket socketIOClient() throws URISyntaxException {
        Socket socket = IO.socket("http://localhost:3000");

        // Event listeners
        socket.on(Socket.EVENT_CONNECT, args ->
                System.out.println("Connected to Socket.IO server!"));

        socket.on("message", args -> {
            String message = (String) args[0];
            System.out.println("Received: " + message);
        });

        socket.on(Socket.EVENT_DISCONNECT, args ->
                System.out.println("Disconnected from server"));

        socket.connect();
        return socket;
    }
}