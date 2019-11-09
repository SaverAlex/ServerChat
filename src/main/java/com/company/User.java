package com.company;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class User implements Runnable {
    private Server server;
    private PrintWriter printWriter;
    private Scanner scanner;
    private boolean signIn = false;
    private String name;
    private Properties properties;
    private JsonData jsonDataUser;

    public User(Socket socket, Server server) {
        this.server = server;
        try {
            this.printWriter = new PrintWriter(socket.getOutputStream(), true);
            this.scanner = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String message;
        Gson gson = new Gson();
        while (true) {
            if (scanner.hasNext()) {
                message = scanner.nextLine();
                jsonDataUser = gson.fromJson(message, JsonData.class);
                try {
                    InputStream input = new FileInputStream("registered.properties");
                    properties = new Properties();
                    properties.load(input);
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!signIn) {
                    name = jsonDataUser.login;
                    // Проверка доступа
                    if (!verificationOfAccess()) {
                        printWriter.println("** Отказано в доступе **");
                        break;
                    }
                    // Если пользователь не зарегестрирован, регистрирум его
                    if (properties.getProperty(jsonDataUser.login) == null && jsonDataUser.login != null) registration();
                    // Проверка авторизации пользователя
                    if (String.valueOf(jsonDataUser.message.hashCode()).equals(properties.getProperty(jsonDataUser.login))) {
                        printWriter.println("** Авторизация прошла успешно **");
                        signIn = true;
                        System.out.println("** " + name + " присоединился **");
                        server.sendMsgAllClients(name + " присоединился",this);
                        printWriter.println("Добро пожаловать: " + name + "!");
                        continue;
                    } else {
                        printWriter.println("** Ошибка авторизации **");
                        break;
                    }
                }
                System.out.println(name + ": " + jsonDataUser.message);
                server.sendMsgAllClients(name + ": " + jsonDataUser.message, this);
            }
        }

    }

    public void sendMsg(String message) {
        printWriter.println(message);
    }

    public boolean getSignIn() {
        return signIn;
    }

    private void registration() {
        try {
            OutputStream fileOutputStream = new FileOutputStream("registered.properties");
            properties.setProperty(name, String.valueOf(jsonDataUser.message.hashCode()));
            properties.store(fileOutputStream, null);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean verificationOfAccess() {
        try {
            InputStream input = new FileInputStream("Access.properties");
            Properties propertiesAccess = new Properties();
            propertiesAccess.load(input);
            input.close();
            if (propertiesAccess.getProperty(name) != null) return true;

        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

}
