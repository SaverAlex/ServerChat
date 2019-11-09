package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server{

    private ArrayList<User> allUsers = new ArrayList<User>();

    public Server() {
        try {
            ServerSocket serverSocket = new ServerSocket(1777);
            while (true) {
                Socket socket = serverSocket.accept();
                User user = new User(socket, this);
                allUsers.add(user);
                new Thread(user).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsgAllClients (String message, User currentUser){
        for (User user : allUsers) {
            if (currentUser != user && user.getSignIn()) user.sendMsg(message);
        }
    }
}
