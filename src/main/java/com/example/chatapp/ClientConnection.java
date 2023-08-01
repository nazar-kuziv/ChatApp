package com.example.chatapp;

import java.io.*;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientConnection extends Thread {
    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private final String youGetFileKey = "Vj1l7FY^7^6$pUQ^NDWw";
    private final String successfulLoginKey = "^yus764y1kcy1l72T5xU";
    private final String failedLoginKey = "%363D5F7GH*CICkaDxp@";
    private final String listOfUsersOnlineKey = "KE6aG20#N*k1M3Y5m!X1";
    private ChatViewController chatViewController;
    private LoginViewController loginViewController;
    public ClientConnection(String address, int port) throws IOException {
        socket = new Socket(address, port);
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        writer = new PrintWriter(output, true);
        reader = new BufferedReader(new InputStreamReader(input));
    }

    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            String message;
            while ((!socket.isClosed()) && (message = reader.readLine()) != null) {
                if(message.equals(successfulLoginKey)){
                    loginViewController.successfulLogin();
                } else if (message.equals(failedLoginKey)) {
                    loginViewController.failedLogin();
                } else if(hasNewUserConnected(message) || hasUserDisconnected(message)){
                    chatViewController.addNewMessageIntoTextArea(message);
                    writer.println("/online");
                    chatViewController.updateParticipantList(getUsersLogins(reader.readLine()));
                }else if (isThisMessageWithFile(message)) {
                    sendFileToServer(message);
                } else if (message.startsWith("YouGetFile" + youGetFileKey)) {
                    receiveFileFromServer(dataInputStream, message);
                } else if(message.startsWith(listOfUsersOnlineKey)){
                    chatViewController.updateParticipantList(getUsersLogins(message));
                }else{
                    chatViewController.addNewMessageIntoTextArea(message);
                }
            }
        }catch (IOException e) {
            if (!socket.isClosed()) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendFileToServer(String message) {
        try {
            String[] messageParts = message.substring(6).split(" ", 2);
            String recipient = messageParts[0];
            String filePath = messageParts[1];

            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                long fileSize = file.length();

                writer.println("/file " + recipient + " " + file.getName());
                writer.println(fileSize);
                writer.flush();

                FileInputStream fileInputStream = new FileInputStream(file);

                byte[] buffer = new byte[4 * 1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    socket.getOutputStream().write(buffer, 0, bytesRead);
                }
                fileInputStream.close();
            } else {
                System.out.println("File not found: " + filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void receiveFileFromServer(DataInputStream dataInputStream, String message) {
        try {
            String folderPath = "received/";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                if(!folder.mkdirs()){
                    throw new RuntimeException("Failed to create directory: " + folderPath);
                }
            }
            File file = new File(message.split(" ", 2)[1]);
            String fileName = file.getName();
            FileOutputStream fileOutputStream = new FileOutputStream(folderPath + fileName);
            String sFileSize = dataInputStream.readLine();
            chatViewController.addNewMessageIntoTextArea("You receive a file from another user, its size is "+ sFileSize + " b");

            long fileSize = Long.parseLong(sFileSize);
            byte[] buffer = new byte[4 * 1024];
            int bytesRead;
            long bytesRemaining = fileSize;

            while (bytesRemaining > 0) {
                bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining));
                if (bytesRead == -1) {
                    break;
                }
                fileOutputStream.write(buffer, 0, bytesRead);
                bytesRemaining -= bytesRead;
            }

            fileOutputStream.flush();
            fileOutputStream.close();
            chatViewController.addNewMessageIntoTextArea("File saved at: "+ folderPath + fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }
    private boolean isThisMessageWithFile(String message) {
        return message.startsWith("/file");
    }
    private boolean hasNewUserConnected(String message) {
        String pattern = "(?i)User\\s+\\w+\\s+joined\\s+the\\s+chat!";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(message);
        return matcher.find();
    }
    private boolean hasUserDisconnected(String message) {
        String pattern = "(?i)User\\s+\\w+\\s+left\\s+the\\s+chat!";
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(message);
        return matcher.find();
    }
    public List<String> getUsersLogins(String message) {
        if(message.equals(listOfUsersOnlineKey)){
            return Collections.singletonList("");
        }else{
            String[] users = message.substring(21).split(" ");
            return List.of(users);
        }
    }
    public void setChatViewController(ChatViewController chatViewController) {
        this.chatViewController = chatViewController;
    }
    public void setLoginViewController(LoginViewController loginViewController) {
        this.loginViewController = loginViewController;
    }
    public void updateListOfUsersOnline() {
        Runnable waitingToControllerConfiguration = () -> {
            while (chatViewController == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            writer.println("/online");
        };
        Thread updateThread = new Thread(waitingToControllerConfiguration);
        updateThread.start();
    }
    public void disconnect() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}